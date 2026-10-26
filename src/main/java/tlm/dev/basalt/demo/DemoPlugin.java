package tlm.dev.basalt.demo;

import org.bukkit.plugin.java.JavaPlugin;
import tlm.dev.basalt.api.Basalt;
import tlm.dev.basalt.api.BasaltProvider;
import tlm.dev.basalt.demo.config.DemoFiles;
import tlm.dev.basalt.demo.config.DemoSettings;
import tlm.dev.basalt.demo.host.HostWorld;
import tlm.dev.basalt.demo.host.ModelPlan;
import tlm.dev.basalt.demo.host.ModelService;
import tlm.dev.basalt.demo.layout.DefaultLayout;
import tlm.dev.basalt.demo.layout.DungeonLayout;
import tlm.dev.basalt.demo.lifecycle.LifecycleManager;

public final class DemoPlugin extends JavaPlugin {

    private LifecycleManager lifecycle;

    @Override
    public void onEnable() {
        Basalt basalt = BasaltProvider.get();
        DemoFiles files = new DemoFiles(getDataPath(), this::getResource, getLogger());
        files.reload();
        DemoSettings settings = files.settings();
        DungeonLayout layout = DefaultLayout.create();
        ModelPlan plan = new ModelPlan(layout.size(), point -> layout.blockAt(point).key(), layout.exit(), layout.spawn());

        HostWorld host = new HostWorld(settings.host(), plan, getLogger());
        ModelService model = new ModelService(basalt, host, plan, getLogger());

        lifecycle = new LifecycleManager(getLogger());
        lifecycle.add(host);
        lifecycle.add(model);
        if (!lifecycle.enableAll()) {
            getLogger().severe("BasaltDemo could not start, see the errors above");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("BasaltDemo " + getPluginMeta().getVersion() + " runs on Basalt " + basalt.version());
    }

    @Override
    public void onDisable() {
        if (lifecycle != null) {
            lifecycle.disableAll();
        }
    }
}
