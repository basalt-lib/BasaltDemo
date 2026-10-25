package tlm.dev.basalt.demo;

import org.bukkit.plugin.java.JavaPlugin;
import tlm.dev.basalt.api.Basalt;
import tlm.dev.basalt.api.BasaltProvider;

public final class DemoPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        Basalt basalt = BasaltProvider.get();
        getLogger().info("BasaltDemo " + getPluginMeta().getVersion() + " runs on Basalt " + basalt.version());
    }
}
