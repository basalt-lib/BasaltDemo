package tlm.dev.basalt.demo.layout;

public enum BlockKind {
    AIR("minecraft:air"),
    STONE_BRICKS("minecraft:stone_bricks"),
    CRACKED_STONE_BRICKS("minecraft:cracked_stone_bricks"),
    IRON_BLOCK("minecraft:iron_block"),
    GOLD_BLOCK("minecraft:gold_block");

    private final String key;

    BlockKind(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public boolean solid() {
        return this != AIR;
    }
}
