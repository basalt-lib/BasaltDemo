package tlm.dev.basalt.demo.message;

import java.util.List;

public enum MessageKey {
    PLAYER_NEEDED("player.needed"),
    MODEL_VISIT("model.visit"),
    MODEL_VISIT_REFUSED("model.visit-refused"),
    MODEL_IN_RUN("model.in-run"),
    MODEL_CAPTURED("model.captured", "blocks", "time"),
    MODEL_REBUILT("model.rebuilt", "blocks", "time"),
    MODEL_FAILED("model.failed", "reason");

    private final String path;
    private final List<String> placeholders;

    MessageKey(String path, String... placeholders) {
        this.path = path;
        this.placeholders = List.of(placeholders);
    }

    public String path() {
        return path;
    }

    public List<String> placeholders() {
        return placeholders;
    }
}
