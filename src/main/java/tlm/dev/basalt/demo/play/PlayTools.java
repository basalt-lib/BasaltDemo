package tlm.dev.basalt.demo.play;

import tlm.dev.basalt.demo.config.RunSettings;
import tlm.dev.basalt.demo.message.Messages;

import java.time.Clock;
import java.util.Objects;
import java.util.logging.Logger;

public record PlayTools(Messages messages, Clock clock, Logger logger, RunSettings settings) {

    public PlayTools {
        Objects.requireNonNull(messages, "messages");
        Objects.requireNonNull(clock, "clock");
        Objects.requireNonNull(logger, "logger");
        Objects.requireNonNull(settings, "settings");
    }
}
