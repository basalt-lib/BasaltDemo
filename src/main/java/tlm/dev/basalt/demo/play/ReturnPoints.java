package tlm.dev.basalt.demo.play;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public final class ReturnPoints {

    private final Supplier<World> host;
    private final Map<UUID, Location> before = new HashMap<>();
    private final Map<UUID, Location> kept = new HashMap<>();

    public ReturnPoints(Supplier<World> host) {
        this.host = host;
    }

    public void remember(Player player) {
        before.put(player.getUniqueId(), player.getLocation());
    }

    public Location take(UUID player) {
        return safe(before.remove(player));
    }

    public void keepForReturn(UUID player, Location location) {
        kept.put(player, location);
    }

    public Location takeKept(UUID player) {
        return safe(kept.remove(player));
    }

    private Location safe(Location location) {
        if (location == null || !location.isWorldLoaded() || location.getWorld().equals(host.get())) {
            return Bukkit.getWorlds().getFirst().getSpawnLocation().add(0.5, 0, 0.5);
        }
        return location;
    }
}
