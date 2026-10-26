package tlm.dev.basalt.demo;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import tlm.dev.basalt.api.Basalt;
import tlm.dev.basalt.api.BasaltProvider;
import tlm.dev.basalt.demo.command.DungeonCommand;
import tlm.dev.basalt.demo.command.ModelOrders;
import tlm.dev.basalt.demo.command.RunOrders;
import tlm.dev.basalt.demo.config.DemoFiles;
import tlm.dev.basalt.demo.config.DemoSettings;
import tlm.dev.basalt.demo.host.HostWorld;
import tlm.dev.basalt.demo.host.ModelCommands;
import tlm.dev.basalt.demo.host.ModelPlan;
import tlm.dev.basalt.demo.host.ModelService;
import tlm.dev.basalt.demo.layout.DefaultLayout;
import tlm.dev.basalt.demo.layout.DungeonLayout;
import tlm.dev.basalt.demo.lifecycle.LifecycleManager;
import tlm.dev.basalt.demo.message.Messages;
import tlm.dev.basalt.demo.play.Arena;
import tlm.dev.basalt.demo.play.BlockBreakListener;
import tlm.dev.basalt.demo.play.BlockPlaceListener;
import tlm.dev.basalt.demo.play.ExitPath;
import tlm.dev.basalt.demo.play.Marks;
import tlm.dev.basalt.demo.play.MonsterDeathListener;
import tlm.dev.basalt.demo.play.PlayTools;
import tlm.dev.basalt.demo.play.PlayerJoinListener;
import tlm.dev.basalt.demo.play.ReturnPoints;
import tlm.dev.basalt.demo.play.RunProgress;
import tlm.dev.basalt.demo.play.RunService;
import tlm.dev.basalt.demo.play.RunWorlds;
import tlm.dev.basalt.demo.play.ViewerRemoveListener;
import tlm.dev.basalt.demo.run.RunRegistry;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

public final class DemoPlugin extends JavaPlugin {

    private LifecycleManager lifecycle;

    @Override
    public void onEnable() {
        Basalt basalt = BasaltProvider.get();
        DemoFiles files = new DemoFiles(getDataPath(), this::getResource, getLogger());
        files.reload();
        DemoSettings settings = files.settings();
        Messages messages = new Messages(files.messages());
        DungeonLayout layout = DefaultLayout.create();
        ModelPlan plan = new ModelPlan(layout.size(), point -> layout.blockAt(point).key(), layout.exit(), layout.spawn());

        RunRegistry book = new RunRegistry(settings.run(), UUID::randomUUID);
        HostWorld host = new HostWorld(settings.host(), plan, this);
        ModelService model = new ModelService(basalt, host, plan, getLogger());
        ModelCommands modelCommands = new ModelCommands(model, messages, book);

        Arena arena = new Arena(host::world, settings.host().playOrigin(), model::blueprint,
                new Marks(layout.spawn(), layout.monsters(), layout.door(), layout.exit()));
        PlayTools tools = new PlayTools(messages, Clock.systemUTC(), getLogger(), settings.run());
        RunWorlds worlds = new RunWorlds(basalt, this, arena);
        ReturnPoints returns = new ReturnPoints(host::world);
        RunService runs = new RunService(book, worlds, returns, tools);
        ExitPath exits = new ExitPath(book, worlds, returns, messages);
        RunProgress progress = new RunProgress(book, worlds, tools);

        lifecycle = new LifecycleManager(getLogger());
        lifecycle.add(host);
        lifecycle.add(model);
        lifecycle.add(worlds);
        lifecycle.add(exits);
        if (!lifecycle.enableAll()) {
            getLogger().severe("BasaltDemo could not start, see the errors above");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        List<Listener> listeners = List.of(
                new ViewerRemoveListener(exits),
                new MonsterDeathListener(progress),
                new BlockBreakListener(progress),
                new BlockPlaceListener(progress),
                new PlayerJoinListener(host::world, returns));
        listeners.forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));

        DungeonCommand command = new DungeonCommand(
                new RunOrders(runs::start, runs::join, exits::request, runs::list, runs::status),
                new ModelOrders(modelCommands::visit, modelCommands::capture, modelCommands::rebuild),
                messages);
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> event.registrar().register(command.build()));
        getLogger().info("BasaltDemo " + getPluginMeta().getVersion() + " runs on Basalt " + basalt.version());
    }

    @Override
    public void onDisable() {
        if (lifecycle != null) {
            lifecycle.disableAll();
        }
    }
}
