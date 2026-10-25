package tlm.dev.basalt.demo.lifecycle;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LifecycleManagerTest {

    private final List<String> calls = new ArrayList<>();
    private final LifecycleManager manager = new LifecycleManager(silentLogger());

    @Test
    void componentsStartByPriorityAndStopTheOtherWay() {
        manager.add(component("late", 2, false));
        manager.add(component("early", 0, false));
        manager.add(component("middle", 1, false));
        assertTrue(manager.enableAll());
        manager.disableAll();
        assertEquals(List.of("enable early", "enable middle", "enable late",
                "disable late", "disable middle", "disable early"), calls);
    }

    @Test
    void aFailingComponentDoesNotStopTheOthers() {
        manager.add(component("first", 0, false));
        manager.add(component("broken", 1, true));
        manager.add(component("last", 2, false));
        assertFalse(manager.enableAll());
        manager.disableAll();
        assertEquals(List.of("enable first", "enable broken", "enable last",
                "disable last", "disable first"), calls);
    }

    @Test
    void aSecondStopDoesNothing() {
        manager.add(component("only", 0, false));
        manager.enableAll();
        manager.disableAll();
        manager.disableAll();
        assertEquals(List.of("enable only", "disable only"), calls);
    }

    private Lifecycle component(String name, int priority, boolean fails) {
        return new Lifecycle() {
            @Override
            public void onEnable() {
                calls.add("enable " + name);
                if (fails) {
                    throw new IllegalStateException(name);
                }
            }

            @Override
            public void onDisable() {
                calls.add("disable " + name);
                if (fails) {
                    throw new IllegalStateException(name);
                }
            }

            @Override
            public int priority() {
                return priority;
            }
        };
    }

    private static Logger silentLogger() {
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        return logger;
    }
}
