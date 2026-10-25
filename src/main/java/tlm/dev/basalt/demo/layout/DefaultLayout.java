package tlm.dev.basalt.demo.layout;

import tlm.dev.basalt.demo.position.BlockBox;
import tlm.dev.basalt.demo.position.BlockPoint;
import tlm.dev.basalt.demo.position.Spot;

import java.util.List;

public final class DefaultLayout {

    private static final float FACING_EAST = -90f;
    private static final float FACING_WEST = 90f;

    private DefaultLayout() {
    }

    public static DungeonLayout create() {
        BlockBox door = box(22, 1, 6, 22, 3, 9);
        BlockPoint exit = new BlockPoint(27, 2, 7);
        return new DungeonLayout(
                new BlockPoint(32, 8, 16),
                List.of(
                        new BlockFill(box(0, 0, 0, 31, 7, 15), BlockKind.STONE_BRICKS),
                        new BlockFill(box(1, 1, 1, 8, 6, 14), BlockKind.AIR),
                        new BlockFill(box(9, 1, 6, 9, 3, 9), BlockKind.AIR),
                        new BlockFill(box(10, 1, 1, 21, 6, 14), BlockKind.AIR),
                        new BlockFill(door, BlockKind.IRON_BLOCK),
                        new BlockFill(box(23, 1, 1, 30, 6, 14), BlockKind.AIR),
                        new BlockFill(BlockBox.single(exit.below()), BlockKind.STONE_BRICKS),
                        new BlockFill(BlockBox.single(exit), BlockKind.GOLD_BLOCK)),
                new Spot(3.5, 1, 7.5, FACING_EAST),
                List.of(
                        new Spot(14.5, 1, 4.5, FACING_WEST),
                        new Spot(17.5, 1, 10.5, FACING_WEST),
                        new Spot(20.5, 1, 6.5, FACING_WEST)),
                door,
                exit);
    }

    private static BlockBox box(int x1, int y1, int z1, int x2, int y2, int z2) {
        return BlockBox.of(new BlockPoint(x1, y1, z1), new BlockPoint(x2, y2, z2));
    }
}
