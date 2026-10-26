package tlm.dev.basalt.demo.play;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import tlm.dev.basalt.api.event.VirtualBlockPlaceEvent;

public final class BlockPlaceListener implements Listener {

    private final RunProgress progress;

    public BlockPlaceListener(RunProgress progress) {
        this.progress = progress;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(VirtualBlockPlaceEvent event) {
        if (!progress.allowsPlace(event.world(), event.player())) {
            event.setCancelled(true);
        }
    }
}
