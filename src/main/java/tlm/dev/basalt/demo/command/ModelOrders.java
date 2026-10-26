package tlm.dev.basalt.demo.command;

import java.util.Objects;

public record ModelOrders(PlayerOrder visit, SenderOrder capture, SenderOrder rebuild) {

    public ModelOrders {
        Objects.requireNonNull(visit, "visit");
        Objects.requireNonNull(capture, "capture");
        Objects.requireNonNull(rebuild, "rebuild");
    }
}
