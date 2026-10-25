package tlm.dev.basalt.demo.config;

import java.util.Objects;

public record DemoSettings(HostSettings host, RunSettings run) {

    public DemoSettings {
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(run, "run");
    }
}
