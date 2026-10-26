package tlm.dev.basalt.demo.message;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Map;

public final class Messages {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<String, String> texts;

    public Messages(Map<String, String> texts) {
        this.texts = Map.copyOf(texts);
    }

    public Component render(MessageKey key, TagResolver... resolvers) {
        return miniMessage.deserialize(texts.getOrDefault(key.path(), key.path()), resolvers);
    }

    public void send(Audience audience, MessageKey key, TagResolver... resolvers) {
        audience.sendMessage(render(key, resolvers));
    }
}
