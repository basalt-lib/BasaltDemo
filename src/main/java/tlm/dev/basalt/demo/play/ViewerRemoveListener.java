package tlm.dev.basalt.demo.play;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import tlm.dev.basalt.api.event.VirtualViewerRemoveEvent;

public final class ViewerRemoveListener implements Listener {

    private final ExitPath exits;

    public ViewerRemoveListener(ExitPath exits) {
        this.exits = exits;
    }

    @EventHandler
    public void onViewerRemove(VirtualViewerRemoveEvent event) {
        exits.exit(event.player(), event.world(), event.reason());
    }
}
