package tlm.dev.basalt.demo.play;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import tlm.dev.basalt.api.event.VirtualBlockBreakEvent;

public final class BlockBreakListener implements Listener {

    private final RunProgress progress;

    public BlockBreakListener(RunProgress progress) {
        this.progress = progress;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(VirtualBlockBreakEvent event) {
        if (!progress.allowsBreak(event.world(), event.player(), event.location())) {
            event.setCancelled(true);
        }
    }
}
