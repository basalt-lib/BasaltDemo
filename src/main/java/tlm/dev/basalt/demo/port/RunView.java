package tlm.dev.basalt.demo.port;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record RunView(UUID id, List<UUID> members, int monstersLeft, boolean doorOpen, boolean finished, Instant startedAt) {

    public RunView {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(startedAt, "startedAt");
        members = List.copyOf(members);
    }

    public UUID leader() {
        return members.getFirst();
    }

    public boolean empty() {
        return members.isEmpty();
    }
}
