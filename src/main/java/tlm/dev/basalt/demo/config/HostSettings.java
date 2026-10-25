package tlm.dev.basalt.demo.config;

import tlm.dev.basalt.demo.position.BlockPoint;

import java.util.Objects;

public record HostSettings(String world, BlockPoint modelOrigin, BlockPoint playOrigin) {

    public HostSettings {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(modelOrigin, "modelOrigin");
        Objects.requireNonNull(playOrigin, "playOrigin");
    }
}
