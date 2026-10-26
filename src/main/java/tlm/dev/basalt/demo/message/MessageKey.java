package tlm.dev.basalt.demo.message;

import java.util.List;

public enum MessageKey {
    PLAYER_NEEDED("player.needed"),
    PLAYER_INVALID("player.invalid", "name"),
    PLAYER_UNKNOWN("player.unknown", "name"),
    RUN_ALREADY("run.already", "player"),
    START_STARTED("start.started", "zombies"),
    START_DONE("start.done", "player"),
    START_TOO_MANY("start.too-many", "max"),
    START_FAILED("start.failed", "reason"),
    ENTRY_REFUSED("start.entry-refused", "player"),
    JOIN_JOINED("join.joined", "leader"),
    JOIN_ARRIVED("join.arrived", "player"),
    JOIN_DONE("join.done", "player", "leader"),
    JOIN_NO_RUN("join.no-run", "player"),
    JOIN_FULL("join.full", "leader", "max"),
    JOIN_FINISHED("join.finished", "leader"),
    LEAVE_LEFT("leave.left"),
    LEAVE_GONE("leave.gone", "player"),
    LEAVE_DONE("leave.done", "player"),
    LEAVE_NOT_IN_RUN("leave.not-in-run", "player"),
    RETURN_BACK("return.back"),
    MONSTER_NAME("monster.name"),
    MONSTER_DOWN("monster.down", "zombies"),
    DOOR_OPENED("door.opened"),
    EXIT_LOCKED("exit.locked"),
    BUILD_REFUSED("build.refused"),
    FINISH_TITLE("finish.title"),
    FINISH_SUBTITLE("finish.subtitle", "time"),
    FINISH_MESSAGE("finish.message", "time", "delay"),
    LIST_HEADER("list.header", "runs", "worlds"),
    LIST_ENTRY("list.entry", "leader", "players", "zombies", "door", "finished"),
    LIST_EMPTY("list.empty", "worlds"),
    STATUS_RUN("status.run", "player", "world", "leader", "players", "zombies", "door", "finished"),
    STATUS_NONE("status.none", "player", "world"),
    WORD_OPEN("words.open"),
    WORD_CLOSED("words.closed"),
    WORD_YES("words.yes"),
    WORD_NO("words.no"),
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
