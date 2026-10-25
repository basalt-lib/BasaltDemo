package tlm.dev.basalt.demo.host;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import tlm.dev.basalt.demo.position.Spot;

import java.util.Random;

final class VoidGenerator extends ChunkGenerator {

    private final Spot spawn;

    VoidGenerator(Spot spawn) {
        this.spawn = spawn;
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return new Location(world, spawn.x(), spawn.y(), spawn.z(), spawn.yaw(), 0f);
    }
}
