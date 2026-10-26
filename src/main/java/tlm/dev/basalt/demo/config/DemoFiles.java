package tlm.dev.basalt.demo.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import tlm.dev.basalt.demo.position.BlockPoint;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public final class DemoFiles {

    private static final String CONFIG = "config.yml";
    private static final String MESSAGES = "messages.yml";
    private static final Pattern WORLD_NAME = Pattern.compile("[a-z0-9_-]{1,32}");
    private static final int CHUNK_LIMIT = 100_000;
    private static final int CHUNK_SIZE = 16;

    private final Path folder;
    private final Function<String, InputStream> shipped;
    private final Logger logger;
    private DemoSettings settings;
    private Map<String, String> messages;

    public DemoFiles(Path folder, Function<String, InputStream> shipped, Logger logger) {
        this.folder = folder;
        this.shipped = shipped;
        this.logger = logger;
    }

    public void reload() {
        YamlConfiguration shippedConfig = readShipped(CONFIG);
        Optional<YamlConfiguration> serverConfig = readServer(CONFIG);
        if (serverConfig.isPresent() || settings == null) {
            settings = parse(new YamlValues(CONFIG, serverConfig.orElse(shippedConfig), shippedConfig, logger));
        }
        YamlConfiguration shippedMessages = readShipped(MESSAGES);
        Optional<YamlConfiguration> serverMessages = readServer(MESSAGES);
        if (serverMessages.isPresent() || messages == null) {
            messages = texts(shippedMessages, serverMessages.orElse(shippedMessages));
        }
    }

    public DemoSettings settings() {
        if (settings == null) {
            throw new IllegalStateException("the files are not read yet, call reload first");
        }
        return settings;
    }

    public Map<String, String> messages() {
        if (messages == null) {
            throw new IllegalStateException("the files are not read yet, call reload first");
        }
        return messages;
    }

    private Map<String, String> texts(YamlConfiguration shipped, YamlConfiguration server) {
        Map<String, String> texts = new LinkedHashMap<>();
        for (String key : shipped.getKeys(true)) {
            if (!shipped.isString(key)) {
                continue;
            }
            if (server.isString(key)) {
                texts.put(key, server.getString(key));
            } else {
                logger.warning(MESSAGES + ": " + key + " is missing, using the shipped text");
                texts.put(key, shipped.getString(key));
            }
        }
        return Map.copyOf(texts);
    }

    private static DemoSettings parse(YamlValues values) {
        int floorY = values.integer("host.floor-y", -64, 300);
        HostSettings host = new HostSettings(
                values.word("host.world", WORLD_NAME),
                chunkOrigin(values, "host.model-chunk-x", "host.model-chunk-z", floorY),
                chunkOrigin(values, "host.play-chunk-x", "host.play-chunk-z", floorY));
        RunSettings run = new RunSettings(
                values.integer("run.max-runs", 1, 64),
                values.integer("run.max-players", 1, 100),
                values.decimal("run.monster-health", 1, 1024),
                values.integer("run.return-delay-seconds", 0, 300));
        return new DemoSettings(host, run);
    }

    private static BlockPoint chunkOrigin(YamlValues values, String keyX, String keyZ, int floorY) {
        int chunkX = values.integer(keyX, -CHUNK_LIMIT, CHUNK_LIMIT);
        int chunkZ = values.integer(keyZ, -CHUNK_LIMIT, CHUNK_LIMIT);
        return new BlockPoint(chunkX * CHUNK_SIZE, floorY, chunkZ * CHUNK_SIZE);
    }

    private YamlConfiguration readShipped(String name) {
        try (InputStream in = shipped.apply(name)) {
            if (in == null) {
                throw new IllegalStateException("the plugin jar holds no " + name);
            }
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            return yaml;
        } catch (IOException | InvalidConfigurationException error) {
            throw new IllegalStateException("the shipped " + name + " cannot be read", error);
        }
    }

    private Optional<YamlConfiguration> readServer(String name) {
        Path file = folder.resolve(name);
        try {
            if (Files.notExists(file)) {
                Files.createDirectories(folder);
                try (InputStream in = shipped.apply(name)) {
                    Files.copy(in, file);
                }
            }
            YamlConfiguration yaml = new YamlConfiguration();
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                yaml.load(reader);
            }
            return Optional.of(yaml);
        } catch (InvalidConfigurationException error) {
            logger.warning(file + " is not valid YAML, keeping the values in use: " + firstLine(error.getMessage()));
            return Optional.empty();
        } catch (IOException error) {
            throw new UncheckedIOException("cannot read " + file, error);
        }
    }

    private static String firstLine(String message) {
        return message == null ? "" : message.lines().findFirst().orElse("");
    }
}
