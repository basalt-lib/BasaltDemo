package tlm.dev.basalt.demo.command;

import java.util.Objects;

public record RunOrders(PlayerOrder start, JoinOrder join, PlayerOrder leave, SenderOrder list, PlayerOrder status) {

    public RunOrders {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(join, "join");
        Objects.requireNonNull(leave, "leave");
        Objects.requireNonNull(list, "list");
        Objects.requireNonNull(status, "status");
    }
}
