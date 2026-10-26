package tlm.dev.basalt.demo.play;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import tlm.dev.basalt.api.event.VirtualEntityDeathEvent;

public final class MonsterDeathListener implements Listener {

    private final RunProgress progress;

    public MonsterDeathListener(RunProgress progress) {
        this.progress = progress;
    }

    @EventHandler
    public void onDeath(VirtualEntityDeathEvent event) {
        progress.monsterDied(event.world());
    }
}
