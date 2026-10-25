package tlm.dev.basalt.demo.run;

import tlm.dev.basalt.demo.port.RunView;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.UUID;

final class Run {

    private final UUID id;
    private final Instant startedAt;
    private final SequencedSet<UUID> members = new LinkedHashSet<>();
    private int monstersLeft;
    private boolean doorOpen;
    private boolean finished;

    Run(UUID id, UUID leader, int monsters, Instant startedAt) {
        this.id = id;
        this.startedAt = startedAt;
        this.monstersLeft = monsters;
        members.add(leader);
    }

    UUID id() {
        return id;
    }

    int size() {
        return members.size();
    }

    boolean finished() {
        return finished;
    }

    void add(UUID player) {
        members.add(player);
    }

    void remove(UUID player) {
        members.remove(player);
    }

    boolean empty() {
        return members.isEmpty();
    }

    boolean monsterDied() {
        if (monstersLeft == 0) {
            return false;
        }
        monstersLeft--;
        return true;
    }

    boolean openDoorIfClear() {
        if (doorOpen || monstersLeft > 0) {
            return false;
        }
        doorOpen = true;
        return true;
    }

    Optional<Duration> finish(Instant now) {
        if (!doorOpen || finished) {
            return Optional.empty();
        }
        finished = true;
        return Optional.of(Duration.between(startedAt, now));
    }

    RunView view() {
        return new RunView(id, List.copyOf(members), monstersLeft, doorOpen, finished, startedAt);
    }
}
