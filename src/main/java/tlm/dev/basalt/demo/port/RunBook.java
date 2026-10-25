package tlm.dev.basalt.demo.port;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RunBook {

    Entry open(UUID leader, int monsters, Instant now);

    void cancel(UUID run);

    Entry join(UUID member, UUID player);

    Optional<RunView> leave(UUID player);

    Progress monsterDied(UUID run);

    Optional<Duration> finish(UUID run, Instant now);

    Optional<RunView> runOf(UUID player);

    Optional<RunView> run(UUID run);

    List<RunView> runs();
}
