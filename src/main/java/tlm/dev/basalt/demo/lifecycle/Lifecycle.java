package tlm.dev.basalt.demo.lifecycle;

public interface Lifecycle {

    default void onEnable() {
    }

    default void onDisable() {
    }

    default int priority() {
        return 0;
    }
}
