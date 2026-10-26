package tlm.dev.basalt.demo.host;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BoundingBox;
import tlm.dev.basalt.api.Basalt;
import tlm.dev.basalt.api.Blueprint;
import tlm.dev.basalt.demo.lifecycle.Lifecycle;
import tlm.dev.basalt.demo.position.BlockBox;
import tlm.dev.basalt.demo.position.BlockPoint;
import tlm.dev.basalt.demo.position.Spot;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class ModelService implements Lifecycle {

    private final Basalt basalt;
    private final HostWorld host;
    private final ModelPlan plan;
    private final Logger logger;
    private Blueprint blueprint;

    public ModelService(Basalt basalt, HostWorld host, ModelPlan plan, Logger logger) {
        this.basalt = basalt;
        this.host = host;
        this.plan = plan;
        this.logger = logger;
    }

    @Override
    public void onEnable() {
        if (markerInPlace()) {
            Blueprint found = capture();
            logger.info("Captured the dungeon model found in " + host.world().getName() + ": " + found.blockCount() + " blocks");
        } else {
            Blueprint built = rebuild();
            logger.info("Built the default dungeon model in " + host.world().getName() + ": " + built.blockCount() + " blocks");
        }
    }

    @Override
    public int priority() {
        return 1;
    }

    public Blueprint blueprint() {
        if (blueprint == null) {
            throw new IllegalStateException("the dungeon model is not captured");
        }
        return blueprint;
    }

    public Blueprint capture() {
        BlockBox box = host.modelBox();
        blueprint = basalt.captureBlueprint(host.world(), new BoundingBox(
                box.min().x(), box.min().y(), box.min().z(),
                box.max().x() + 1, box.max().y() + 1, box.max().z() + 1));
        return blueprint;
    }

    public Blueprint rebuild() {
        World world = host.world();
        BlockPoint origin = host.modelBox().min();
        Map<String, BlockData> data = new HashMap<>();
        for (int x = 0; x < plan.size().x(); x++) {
            for (int y = 0; y < plan.size().y(); y++) {
                for (int z = 0; z < plan.size().z(); z++) {
                    BlockData block = data.computeIfAbsent(plan.blocks().apply(new BlockPoint(x, y, z)), Bukkit::createBlockData);
                    world.getBlockAt(origin.x() + x, origin.y() + y, origin.z() + z).setBlockData(block, false);
                }
            }
        }
        return capture();
    }

    public Location entry() {
        Spot spot = plan.entry().shift(host.modelBox().min());
        return new Location(host.world(), spot.x(), spot.y(), spot.z(), spot.yaw(), 0f);
    }

    private boolean markerInPlace() {
        BlockPoint marker = plan.marker().plus(host.modelBox().min());
        BlockData expected = Bukkit.createBlockData(plan.blocks().apply(plan.marker()));
        return host.world().getBlockAt(marker.x(), marker.y(), marker.z()).getType() == expected.getMaterial();
    }
}
