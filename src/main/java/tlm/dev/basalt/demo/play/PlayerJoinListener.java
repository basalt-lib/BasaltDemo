package tlm.dev.basalt.demo.play;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.function.Supplier;

public final class PlayerJoinListener implements Listener {

    private final Supplier<World> host;
    private final ReturnPoints returns;

    public PlayerJoinListener(Supplier<World> host, ReturnPoints returns) {
        this.host = host;
        this.returns = returns;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld().equals(host.get())) {
            player.teleport(returns.takeKept(player.getUniqueId()));
        }
    }
}
