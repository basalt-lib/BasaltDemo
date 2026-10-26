package tlm.dev.basalt.demo.play;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import tlm.dev.basalt.api.Basalt;
import tlm.dev.basalt.api.Blueprint;
import tlm.dev.basalt.api.ChunkArea;
import tlm.dev.basalt.api.VirtualEntity;
import tlm.dev.basalt.api.VirtualWorld;
import tlm.dev.basalt.demo.lifecycle.Lifecycle;
import tlm.dev.basalt.demo.position.BlockBox;
import tlm.dev.basalt.demo.position.BlockPoint;
import tlm.dev.basalt.demo.position.Spot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class RunWorlds implements Lifecycle {

    private final Basalt basalt;
    private final Plugin owner;
    private final Arena arena;
    private final Map<UUID, VirtualWorld> worldOfRun = new HashMap<>();
    private final Map<UUID, UUID> runOfWorld = new HashMap<>();
    private final Map<UUID, BukkitTask> closings = new HashMap<>();

    public RunWorlds(Basalt basalt, Plugin owner, Arena arena) {
        this.basalt = basalt;
        this.owner = owner;
        this.arena = arena;
    }

    @Override
    public int priority() {
        return 2;
    }

    @Override
    public void onDisable() {
        for (UUID run : List.copyOf(worldOfRun.keySet())) {
            close(run);
        }
    }

    public VirtualWorld create(UUID run) {
        Blueprint model = arena.model().get();
        BlockBox box = BlockBox.sized(arena.origin(), new BlockPoint(model.sizeX(), model.sizeY(), model.sizeZ()));
        VirtualWorld world = basalt.createWorld(owner, ChunkArea.ofBlocks(arena.world().get(), boundingBox(box)));
        worldOfRun.put(run, world);
        runOfWorld.put(world.id(), run);
        return world;
    }

    public void place(VirtualWorld world) {
        world.place(arena.model().get(), arena.origin().x(), arena.origin().y(), arena.origin().z());
    }

    public void spawnMonsters(VirtualWorld world, double health, Component name) {
        for (Spot spot : arena.marks().monsters()) {
            VirtualEntity zombie = world.spawnEntity(EntityType.ZOMBIE, location(spot));
            zombie.setCustomName(name);
            zombie.setMaxHealth(health);
        }
    }

    public Location spawn() {
        return location(arena.marks().spawn());
    }

    public int monsterCount() {
        return arena.marks().monsters().size();
    }

    public Optional<UUID> runOf(VirtualWorld world) {
        return Optional.ofNullable(runOfWorld.get(world.id()));
    }

    public Optional<VirtualWorld> world(UUID run) {
        return Optional.ofNullable(worldOfRun.get(run));
    }

    public boolean isExit(Location block) {
        BlockPoint exit = arena.marks().exit().plus(arena.origin());
        return block.getBlockX() == exit.x() && block.getBlockY() == exit.y() && block.getBlockZ() == exit.z();
    }

    public void openDoor(UUID run) {
        world(run).ifPresent(world -> world.fill(boundingBox(arena.marks().door().shift(arena.origin())),
                Material.AIR.createBlockData()));
    }

    public void closeLater(UUID run, long ticks) {
        BukkitTask previous = closings.put(run, owner.getServer().getScheduler().runTaskLater(owner, () -> close(run), ticks));
        if (previous != null) {
            previous.cancel();
        }
    }

    public void close(UUID run) {
        BukkitTask closing = closings.remove(run);
        if (closing != null) {
            closing.cancel();
        }
        VirtualWorld world = worldOfRun.remove(run);
        if (world != null) {
            runOfWorld.remove(world.id());
            world.close();
        }
    }

    public long openWorlds() {
        return basalt.worlds().stream().filter(world -> world.owner().equals(owner)).count();
    }

    private Location location(Spot relative) {
        Spot spot = relative.shift(arena.origin());
        World world = arena.world().get();
        return new Location(world, spot.x(), spot.y(), spot.z(), spot.yaw(), 0f);
    }

    private static BoundingBox boundingBox(BlockBox box) {
        return new BoundingBox(box.min().x(), box.min().y(), box.min().z(),
                box.max().x() + 1, box.max().y() + 1, box.max().z() + 1);
    }
}
