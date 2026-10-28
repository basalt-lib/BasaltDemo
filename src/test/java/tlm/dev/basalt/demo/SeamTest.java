package tlm.dev.basalt.demo;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeamTest {

    private static final Path ROOT = Path.of(System.getProperty("demo.root", "."));
    private static final Set<String> FAMILIES = Set.of("layout", "run", "host", "play", "command");
    private static final Set<String> PLUMBING = Set.of("config", "message", "lifecycle", "position", "port");
    private static final Set<String> PURE = Set.of("layout", "run", "position", "port");
    private static final Pattern IMPORT = Pattern.compile("^import (?:static )?([\\w.]+)", Pattern.MULTILINE);
    private static final Pattern OWN = Pattern.compile("^tlm[.]dev[.]basalt[.]demo[.](\\w+)[.]");
    private static final Pattern FORBIDDEN = Pattern.compile(
            "net[.]minecraft[.]|org[.]bukkit[.]craftbukkit[.]|tlm[.]dev[.]basalt[.](?:core|nms|plugin)[.]|com[.]mojang[.](?!brigadier[.])");
    private static final Pattern PLATFORM = Pattern.compile("^(?:org[.]bukkit|io[.]papermc|net[.]kyori|com[.]mojang|tlm[.]dev[.]basalt[.]api)[.]");
    private static final Pattern REFLECTION = Pattern.compile("Class[.]forName|[.]getDeclared(?:Field|Method|Constructor)|[.]setAccessible");
    private static final Pattern CODE_COMMENT = Pattern.compile("(^|[^:\"'])/{2}|/[*]", Pattern.MULTILINE);
    private static final Pattern YAML_COMMENT = Pattern.compile("(^|\\s)#", Pattern.MULTILINE);
    private static final Pattern UNFINISHED = Pattern.compile("T[O]DO|F[I]XME|X{3}|Unsupported[O]perationException");

    @Test
    void familiesImportOnlyThemselvesAndThePlumbing() {
        List<String> faults = new ArrayList<>();
        for (Path file : javaFiles()) {
            String owner = packageOf(file);
            for (String imported : imports(file)) {
                Matcher own = OWN.matcher(imported);
                if (!own.find()) {
                    if (imported.startsWith("tlm.dev.basalt.demo.")) {
                        faults.add(file + " imports the composition root: " + imported);
                    }
                    continue;
                }
                String target = own.group(1);
                boolean allowed = target.equals(owner) || PLUMBING.contains(target);
                if (PLUMBING.contains(owner) && FAMILIES.contains(target)) {
                    allowed = false;
                }
                if (!allowed && !owner.isEmpty()) {
                    faults.add(file + " in " + owner + " imports " + imported);
                }
            }
        }
        assertEquals(List.of(), faults);
    }

    @Test
    void pureFamiliesKnowNeitherPaperNorBasalt() {
        List<String> faults = new ArrayList<>();
        for (Path file : javaFiles()) {
            if (PURE.contains(packageOf(file))) {
                for (String imported : imports(file)) {
                    if (PLATFORM.matcher(imported).find()) {
                        faults.add(file + " imports " + imported);
                    }
                }
            }
        }
        assertEquals(List.of(), faults);
    }

    @Test
    void noServerInternalsAndNoReflection() {
        List<String> faults = new ArrayList<>();
        for (Path file : javaFiles()) {
            String text = read(file);
            if (FORBIDDEN.matcher(text).find()) {
                faults.add(file + " names a server internal or another Basalt module");
            }
            if (REFLECTION.matcher(text).find()) {
                faults.add(file + " uses reflection");
            }
        }
        assertEquals(List.of(), faults);
    }

    @Test
    void noCommentsAnywhere() {
        List<String> faults = new ArrayList<>();
        for (Path file : sources()) {
            String name = file.getFileName().toString();
            Pattern comment = name.endsWith(".java") || name.endsWith(".kts") ? CODE_COMMENT : YAML_COMMENT;
            if (comment.matcher(read(file)).find()) {
                faults.add(file.toString());
            }
        }
        assertEquals(List.of(), faults);
    }

    @Test
    void noUnfinishedWork() {
        List<String> faults = new ArrayList<>();
        for (Path file : sources()) {
            if (UNFINISHED.matcher(read(file)).find()) {
                faults.add(file.toString());
            }
        }
        assertEquals(List.of(), faults);
    }

    @Test
    void theBuildUsesOnlyThePublishedApi() {
        String build = read(ROOT.resolve("build.gradle.kts"));
        assertTrue(build.contains("io.github.basalt-lib:basalt-api:"));
        assertTrue(!build.contains("BasaltCore") && !build.contains("project("), build);
    }

    private static List<Path> javaFiles() {
        return sources().stream().filter(file -> file.toString().endsWith(".java")).toList();
    }

    private static List<Path> sources() {
        List<Path> files = new ArrayList<>();
        files.add(ROOT.resolve("build.gradle.kts"));
        files.add(ROOT.resolve("settings.gradle.kts"));
        for (String folder : List.of("src/main", "src/test")) {
            try (Stream<Path> walk = Files.walk(ROOT.resolve(folder))) {
                walk.filter(Files::isRegularFile)
                        .filter(file -> file.toString().matches(".*[.](java|kts|yml|yaml|properties)$"))
                        .forEach(files::add);
            } catch (IOException error) {
                throw new UncheckedIOException(error);
            }
        }
        assertTrue(files.size() > 2, "no source found under " + ROOT.toAbsolutePath());
        return files;
    }

    private static String packageOf(Path file) {
        Path base = ROOT.resolve(file.startsWith(ROOT.resolve("src/main")) ? "src/main/java" : "src/test/java")
                .resolve("tlm/dev/basalt/demo");
        Path relative = base.relativize(file);
        return relative.getNameCount() > 1 ? relative.getName(0).toString() : "";
    }

    private static List<String> imports(Path file) {
        List<String> found = new ArrayList<>();
        Matcher matcher = IMPORT.matcher(read(file));
        while (matcher.find()) {
            found.add(matcher.group(1));
        }
        return found;
    }

    private static String read(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }
}
