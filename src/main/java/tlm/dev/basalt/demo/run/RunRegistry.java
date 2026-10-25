package tlm.dev.basalt.demo.run;

import tlm.dev.basalt.demo.config.RunSettings;
import tlm.dev.basalt.demo.port.Entry;
import tlm.dev.basalt.demo.port.Progress;
import tlm.dev.basalt.demo.port.Refusal;
import tlm.dev.basalt.demo.port.RunBook;
import tlm.dev.basalt.demo.port.RunView;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public final class RunRegistry implements RunBook {

    private final RunSettings settings;
    private final Supplier<UUID> ids;
    private final Map<UUID, Run> runs = new LinkedHashMap<>();
    private final Map<UUID, Run> runOfPlayer = new HashMap<>();

    public RunRegistry(RunSettings settings, Supplier<UUID> ids) {
        this.settings = settings;
        this.ids = ids;
    }

    @Override
    public Entry open(UUID leader, int monsters, Instant now) {
        if (monsters < 1) {
            throw new IllegalArgumentException("a run starts with at least one monster: " + monsters);
        }
        if (runOfPlayer.containsKey(leader)) {
            return new Entry.Refused(Refusal.ALREADY_IN_RUN);
        }
        if (runs.size() >= settings.maxRuns()) {
            return new Entry.Refused(Refusal.TOO_MANY_RUNS);
        }
        Run run = new Run(ids.get(), leader, monsters, now);
        runs.put(run.id(), run);
        runOfPlayer.put(leader, run);
        return new Entry.Admitted(run.view());
    }

    @Override
    public void cancel(UUID id) {
        Run run = runs.remove(id);
        if (run != null) {
            runOfPlayer.values().removeIf(run::equals);
        }
    }

    @Override
    public Entry join(UUID member, UUID player) {
        if (runOfPlayer.containsKey(player)) {
            return new Entry.Refused(Refusal.ALREADY_IN_RUN);
        }
        Run run = runOfPlayer.get(member);
        if (run == null) {
            return new Entry.Refused(Refusal.NOT_IN_RUN);
        }
        if (run.finished()) {
            return new Entry.Refused(Refusal.RUN_FINISHED);
        }
        if (run.size() >= settings.maxPlayers()) {
            return new Entry.Refused(Refusal.RUN_FULL);
        }
        run.add(player);
        runOfPlayer.put(player, run);
        return new Entry.Admitted(run.view());
    }

    @Override
    public Optional<RunView> leave(UUID player) {
        Run run = runOfPlayer.remove(player);
        if (run == null) {
            return Optional.empty();
        }
        run.remove(player);
        if (run.empty()) {
            runs.remove(run.id());
        }
        return Optional.of(run.view());
    }

    @Override
    public Progress monsterDied(UUID id) {
        Run run = runs.get(id);
        if (run == null || !run.monsterDied()) {
            return Progress.IGNORED;
        }
        return run.openDoorIfClear() ? Progress.DOOR_OPENED : Progress.COUNTED;
    }

    @Override
    public Optional<Duration> finish(UUID id, Instant now) {
        Run run = runs.get(id);
        return run == null ? Optional.empty() : run.finish(now);
    }

    @Override
    public Optional<RunView> runOf(UUID player) {
        return Optional.ofNullable(runOfPlayer.get(player)).map(Run::view);
    }

    @Override
    public Optional<RunView> run(UUID id) {
        return Optional.ofNullable(runs.get(id)).map(Run::view);
    }

    @Override
    public List<RunView> runs() {
        return runs.values().stream().map(Run::view).toList();
    }
}
