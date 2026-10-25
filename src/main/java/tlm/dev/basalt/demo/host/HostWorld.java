package tlm.dev.basalt.demo.host;

import org.bukkit.Bukkit;
import org.bukkit.GameRules;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import tlm.dev.basalt.demo.config.HostSettings;
import tlm.dev.basalt.demo.lifecycle.Lifecycle;
import tlm.dev.basalt.demo.position.BlockBox;

import java.util.logging.Logger;

public final class HostWorld implements Lifecycle {

    private static final long NOON = 6000;

    private final HostSettings settings;
    private final ModelPlan plan;
    private final Logger logger;
    private World world;

    public HostWorld(HostSettings settings, ModelPlan plan, Logger logger) {
        this.settings = settings;
        this.plan = plan;
        this.logger = logger;
    }

    @Override
    public void onEnable() {
        if (modelBox().overlaps(playBox())) {
            throw new IllegalStateException("host.model-chunk-x and z put the model on the play area "
                    + playBox().min() + " to " + playBox().max());
        }
        if (Bukkit.getWorld(settings.world()) != null) {
            throw new IllegalStateException("the world " + settings.world()
                    + " is already loaded, host.world must name a world of the demo alone");
        }
        world = new WorldCreator(settings.world())
                .environment(World.Environment.NORMAL)
                .generator(new VoidGenerator(plan.entry().shift(settings.modelOrigin())))
                .generateStructures(false)
                .createWorld();
        if (world == null) {
            throw new IllegalStateException("the server did not create the world " + settings.world());
        }
        world.setGameRule(GameRules.ADVANCE_TIME, false);
        world.setGameRule(GameRules.ADVANCE_WEATHER, false);
        world.setGameRule(GameRules.SPAWN_MOBS, false);
        world.setTime(NOON);
        world.setStorm(false);
        world.setThundering(false);
        logger.info("Host world " + world.getName() + " ready: model from " + modelBox().min()
                + ", runs from " + playBox().min());
    }

    @Override
    public int priority() {
        return 0;
    }

    public World world() {
        if (world == null) {
            throw new IllegalStateException("the host world is not created");
        }
        return world;
    }

    public BlockBox modelBox() {
        return BlockBox.sized(settings.modelOrigin(), plan.size());
    }

    public BlockBox playBox() {
        return BlockBox.sized(settings.playOrigin(), plan.size());
    }
}
