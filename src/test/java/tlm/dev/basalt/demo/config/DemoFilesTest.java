package tlm.dev.basalt.demo.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tlm.dev.basalt.demo.position.BlockPoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DemoFilesTest {

    private static final DemoSettings SHIPPED = new DemoSettings(
            new HostSettings("basalt_demo", new BlockPoint(0, 64, 0), new BlockPoint(1024, 64, 0)),
            new RunSettings(8, 4, 6, 8));

    @TempDir
    Path folder;

    private final List<String> warnings = new ArrayList<>();
    private DemoFiles files;

    @BeforeEach
    void createFiles() {
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                warnings.add(record.getMessage());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        });
        files = new DemoFiles(folder, name -> DemoFilesTest.class.getResourceAsStream("/" + name), logger);
    }

    @Test
    void theFirstReadWritesTheShippedFilesAndReadsThem() throws IOException {
        files.reload();
        assertEquals(SHIPPED, files.settings());
        assertEquals(List.of(), warnings);
        assertEquals(shipped("config.yml"), Files.readString(folder.resolve("config.yml")));
        assertEquals(shipped("messages.yml"), Files.readString(folder.resolve("messages.yml")));
        assertEquals("<red>Name a player: the console is not one.</red>", files.messages().get("player.needed"));
    }

    @Test
    void aServerTextWinsAndAMissingOneFallsBack() throws IOException {
        Files.writeString(folder.resolve("messages.yml"), shippedWithLf("messages.yml")
                .replace("<red>Name a player: the console is not one.</red>", "<blue>Who?</blue>")
                .replaceAll("(?m)^  in-run: .*\\n", ""), StandardCharsets.UTF_8);
        files.reload();
        assertEquals("<blue>Who?</blue>", files.messages().get("player.needed"));
        assertEquals("<red>Leave your dungeon run before you visit the model.</red>", files.messages().get("model.in-run"));
        assertEquals(List.of("messages.yml: model.in-run is missing, using the shipped text"), warnings);
    }

    @Test
    void theServerFileWinsWhenItsValuesAreInBounds() throws IOException {
        write("""
                host:
                  world: dungeons
                  model-chunk-x: -3
                  model-chunk-z: 2
                  play-chunk-x: 100
                  play-chunk-z: -100
                  floor-y: 100
                run:
                  max-runs: 2
                  max-players: 1
                  monster-health: 2.5
                  return-delay-seconds: 0
                """);
        files.reload();
        assertEquals(new DemoSettings(
                new HostSettings("dungeons", new BlockPoint(-48, 100, 32), new BlockPoint(1600, 100, -1600)),
                new RunSettings(2, 1, 2.5, 0)), files.settings());
        assertEquals(List.of(), warnings);
    }

    @Test
    void aMissingKeyFallsBackOnTheShippedValue() throws IOException {
        write(shippedWithLf("config.yml").replace("  max-players: 4\n", ""));
        files.reload();
        assertEquals(SHIPPED, files.settings());
        assertEquals(List.of("config.yml: run.max-players is missing, expected a whole number from 1 to 100, using the shipped value 4"), warnings);
    }

    @Test
    void aValueOutOfBoundsOrOfTheWrongTypeFallsBack() throws IOException {
        write(shipped("config.yml")
                .replace("max-runs: 8", "max-runs: 0")
                .replace("monster-health: 6", "monster-health: lots")
                .replace("world: basalt_demo", "world: ../world")
                .replace("floor-y: 64", "floor-y: 400"));
        files.reload();
        assertEquals(SHIPPED, files.settings());
        assertEquals(4, warnings.size(), warnings.toString());
        assertTrue(warnings.contains("config.yml: run.max-runs is 0, expected a whole number from 1 to 64, using the shipped value 8"));
        assertTrue(warnings.contains("config.yml: host.world is ../world, expected a name matching [a-z0-9_-]{1,32}, using the shipped value basalt_demo"));
        assertTrue(warnings.stream().anyMatch(line -> line.startsWith("config.yml: run.monster-health is lots")));
        assertTrue(warnings.stream().anyMatch(line -> line.startsWith("config.yml: host.floor-y is 400")));
    }

    @Test
    void invalidYamlKeepsTheValuesInUse() throws IOException {
        write(shipped("config.yml").replace("max-runs: 8", "max-runs: 3"));
        files.reload();
        write("host: [unclosed\n");
        files.reload();
        assertEquals(3, files.settings().run().maxRuns());
        assertEquals(1, warnings.size(), warnings.toString());
        assertTrue(warnings.getFirst().contains("is not valid YAML, keeping the values in use"));
    }

    @Test
    void invalidYamlAtTheFirstReadUsesTheShippedFile() throws IOException {
        write(": : :\n");
        files.reload();
        assertEquals(SHIPPED, files.settings());
    }

    @Test
    void theSettingsWaitForTheFirstRead() {
        assertThrows(IllegalStateException.class, files::settings);
    }

    private void write(String text) throws IOException {
        Files.writeString(folder.resolve("config.yml"), text, StandardCharsets.UTF_8);
    }

    private static String shipped(String name) throws IOException {
        try (var in = DemoFilesTest.class.getResourceAsStream("/" + name)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String shippedWithLf(String name) throws IOException {
        return shipped(name).replace("\r\n", "\n");
    }
}
