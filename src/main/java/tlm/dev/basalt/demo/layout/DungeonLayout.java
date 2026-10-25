package tlm.dev.basalt.demo.layout;

import tlm.dev.basalt.demo.position.BlockBox;
import tlm.dev.basalt.demo.position.BlockPoint;
import tlm.dev.basalt.demo.position.Spot;

import java.util.List;
import java.util.Objects;

public record DungeonLayout(BlockPoint size, List<BlockFill> fills, Spot spawn, List<Spot> monsters, BlockBox door, BlockPoint exit) {

    public DungeonLayout {
        Objects.requireNonNull(size, "size");
        Objects.requireNonNull(spawn, "spawn");
        Objects.requireNonNull(door, "door");
        Objects.requireNonNull(exit, "exit");
        fills = List.copyOf(fills);
        monsters = List.copyOf(monsters);
        if (monsters.isEmpty()) {
            throw new IllegalArgumentException("a dungeon holds at least one monster");
        }
        BlockBox volume = BlockBox.sized(BlockPoint.ORIGIN, size);
        for (BlockFill fill : fills) {
            if (!volume.contains(fill.box())) {
                throw new IllegalArgumentException("the fill " + fill + " leaves the volume " + volume);
            }
        }
    }

    public BlockBox volume() {
        return BlockBox.sized(BlockPoint.ORIGIN, size);
    }

    public BlockKind blockAt(BlockPoint point) {
        if (!volume().contains(point)) {
            throw new IllegalArgumentException(point + " is outside the volume " + volume());
        }
        BlockKind kind = BlockKind.AIR;
        for (BlockFill fill : fills) {
            if (fill.box().contains(point)) {
                kind = fill.kind();
            }
        }
        return kind == BlockKind.STONE_BRICKS && cracked(point) ? BlockKind.CRACKED_STONE_BRICKS : kind;
    }

    private static boolean cracked(BlockPoint point) {
        return Math.floorMod(3 * point.x() + 5 * point.y() + 7 * point.z(), 4) == 0;
    }
}
