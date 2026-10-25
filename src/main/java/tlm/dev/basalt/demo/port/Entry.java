package tlm.dev.basalt.demo.port;

public sealed interface Entry {

    record Admitted(RunView run) implements Entry {
    }

    record Refused(Refusal reason) implements Entry {
    }
}
