package tlm.dev.basalt.demo;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tlm.dev.basalt.demo.command.DungeonCommand;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginDescriptorTest {

    private static YamlConfiguration descriptor;

    @BeforeAll
    static void readDescriptor() throws IOException, InvalidConfigurationException {
        try (InputStream in = Objects.requireNonNull(PluginDescriptorTest.class.getResourceAsStream("/paper-plugin.yml"));
             Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            descriptor = new YamlConfiguration();
            descriptor.options().pathSeparator('/');
            descriptor.load(reader);
        }
    }

    @Test
    void namesThePluginAndItsMainClass() {
        assertEquals("BasaltDemo", descriptor.getString("name"));
        assertEquals(DemoPlugin.class.getName(), descriptor.getString("main"));
        assertEquals("1.21.11", descriptor.getString("api-version"));
        assertEquals("0.1.0-SNAPSHOT", descriptor.getString("version"));
    }

    @Test
    void needsBasaltCoreLoadedBefore() {
        assertEquals("BEFORE", descriptor.getString("dependencies/server/BasaltCore/load"));
        assertTrue(descriptor.getBoolean("dependencies/server/BasaltCore/required"));
        assertTrue(descriptor.getBoolean("dependencies/server/BasaltCore/join-classpath"));
        assertEquals(1, descriptor.getConfigurationSection("dependencies/server").getKeys(false).size());
    }

    @Test
    void declaresThePermissionsTheCommandChecks() {
        assertEquals("true", descriptor.getString("permissions/" + DungeonCommand.PLAY + "/default"));
        assertEquals("op", descriptor.getString("permissions/" + DungeonCommand.ADMIN + "/default"));
        assertEquals(2, descriptor.getConfigurationSection("permissions").getKeys(false).size());
    }
}
