package tlm.dev.basalt.demo.message;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessagesTest {

    private static final Map<String, String> SHIPPED = new LinkedHashMap<>();

    @BeforeAll
    static void readShippedMessages() throws IOException, InvalidConfigurationException {
        YamlConfiguration yaml = new YamlConfiguration();
        try (InputStream in = Objects.requireNonNull(MessagesTest.class.getResourceAsStream("/messages.yml"))) {
            yaml.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        for (String key : yaml.getKeys(true)) {
            if (yaml.isString(key)) {
                SHIPPED.put(key, yaml.getString(key));
            }
        }
    }

    @Test
    void everyKeyHasAShippedTextAndEveryTextAKey() {
        Set<String> keys = Arrays.stream(MessageKey.values()).map(MessageKey::path).collect(Collectors.toCollection(TreeSet::new));
        assertEquals(keys, new TreeSet<>(SHIPPED.keySet()));
    }

    @Test
    void everyTextParsesStrictlyAndNamesItsPlaceholders() {
        MiniMessage strict = MiniMessage.builder().strict(true).build();
        List<String> faults = new ArrayList<>();
        for (MessageKey key : MessageKey.values()) {
            String text = SHIPPED.get(key.path());
            TagResolver[] resolvers = key.placeholders().stream()
                    .map(name -> Placeholder.unparsed(name, "x"))
                    .toArray(TagResolver[]::new);
            try {
                strict.deserialize(text, resolvers);
            } catch (RuntimeException error) {
                faults.add(key.path() + ": " + error.getMessage());
            }
            for (String placeholder : key.placeholders()) {
                if (!text.contains("<" + placeholder + ">")) {
                    faults.add(key.path() + " does not show <" + placeholder + ">");
                }
            }
        }
        assertEquals(List.of(), faults);
    }

    @Test
    void renderFillsThePlaceholdersAndFallsBackOnThePath() {
        Messages messages = new Messages(Map.of("model.failed", "<red>Failed: <reason></red>"));
        String rendered = PlainTextComponentSerializer.plainText()
                .serialize(messages.render(MessageKey.MODEL_FAILED, Placeholder.unparsed("reason", "<b>too big")));
        assertEquals("Failed: <b>too big", rendered);
        assertEquals("model.visit", PlainTextComponentSerializer.plainText().serialize(messages.render(MessageKey.MODEL_VISIT)));
    }
}
