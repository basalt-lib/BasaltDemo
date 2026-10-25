package tlm.dev.basalt.demo.lifecycle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class LifecycleManager {

    private final Logger logger;
    private final List<Lifecycle> components = new ArrayList<>();
    private final List<Lifecycle> enabled = new ArrayList<>();

    public LifecycleManager(Logger logger) {
        this.logger = logger;
    }

    public void add(Lifecycle component) {
        components.add(component);
    }

    public boolean enableAll() {
        components.sort(Comparator.comparingInt(Lifecycle::priority));
        boolean all = true;
        for (Lifecycle component : components) {
            try {
                component.onEnable();
                enabled.add(component);
            } catch (RuntimeException error) {
                logger.log(Level.SEVERE, "Could not enable " + component.getClass().getSimpleName(), error);
                all = false;
            }
        }
        return all;
    }

    public void disableAll() {
        for (Lifecycle component : enabled.reversed()) {
            try {
                component.onDisable();
            } catch (RuntimeException error) {
                logger.log(Level.SEVERE, "Could not disable " + component.getClass().getSimpleName(), error);
            }
        }
        enabled.clear();
    }
}
