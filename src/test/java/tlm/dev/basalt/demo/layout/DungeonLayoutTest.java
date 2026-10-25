package tlm.dev.basalt.demo.layout;

import org.junit.jupiter.api.Test;
import tlm.dev.basalt.demo.position.BlockBox;
import tlm.dev.basalt.demo.position.BlockPoint;
import tlm.dev.basalt.demo.position.Spot;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DungeonLayoutTest {

    private static final BlockPoint SIZE = new BlockPoint(4, 3, 4);
    private static final Spot SPOT = new Spot(1.5, 1, 1.5, 0);
    private static final BlockBox ALL = BlockBox.sized(BlockPoint.ORIGIN, SIZE);

    @Test
    void theLastFillThatHoldsABlockWins() {
        DungeonLayout layout = layout(List.of(
                new BlockFill(ALL, BlockKind.IRON_BLOCK),
                new BlockFill(BlockBox.of(new BlockPoint(1, 1, 1), new BlockPoint(2, 2, 2)), BlockKind.AIR),
                new BlockFill(BlockBox.single(new BlockPoint(2, 1, 2)), BlockKind.GOLD_BLOCK)));
        assertEquals(BlockKind.IRON_BLOCK, layout.blockAt(BlockPoint.ORIGIN));
        assertEquals(BlockKind.AIR, layout.blockAt(new BlockPoint(1, 1, 1)));
        assertEquals(BlockKind.GOLD_BLOCK, layout.blockAt(new BlockPoint(2, 1, 2)));
    }

    @Test
    void aBlockNoFillHoldsIsAir() {
        DungeonLayout layout = layout(List.of(new BlockFill(BlockBox.single(BlockPoint.ORIGIN), BlockKind.IRON_BLOCK)));
        assertEquals(BlockKind.AIR, layout.blockAt(new BlockPoint(3, 2, 3)));
    }

    @Test
    void oneStoneBrickInFourIsCrackedByItsCoordinates() {
        DungeonLayout layout = layout(List.of(new BlockFill(ALL, BlockKind.STONE_BRICKS)));
        int cracked = 0;
        for (int x = 0; x < SIZE.x(); x++) {
            for (int y = 0; y < SIZE.y(); y++) {
                for (int z = 0; z < SIZE.z(); z++) {
                    BlockKind kind = layout.blockAt(new BlockPoint(x, y, z));
                    if (kind == BlockKind.CRACKED_STONE_BRICKS) {
                        cracked++;
                    }
                    assertEquals(kind, layout.blockAt(new BlockPoint(x, y, z)));
                }
            }
        }
        assertEquals(SIZE.x() * SIZE.y() * SIZE.z() / 4, cracked);
    }

    @Test
    void aBlockOutsideTheVolumeIsRefused() {
        DungeonLayout layout = layout(List.of());
        assertThrows(IllegalArgumentException.class, () -> layout.blockAt(new BlockPoint(4, 0, 0)));
        assertThrows(IllegalArgumentException.class, () -> layout.blockAt(new BlockPoint(0, -1, 0)));
    }

    @Test
    void aFillOutsideTheVolumeIsRefused() {
        BlockFill tooWide = new BlockFill(BlockBox.sized(BlockPoint.ORIGIN, new BlockPoint(5, 1, 1)), BlockKind.AIR);
        assertThrows(IllegalArgumentException.class, () -> layout(List.of(tooWide)));
    }

    @Test
    void aDungeonWithoutMonsterIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> new DungeonLayout(SIZE, List.of(), SPOT, List.of(),
                BlockBox.single(BlockPoint.ORIGIN), BlockPoint.ORIGIN));
    }

    private static DungeonLayout layout(List<BlockFill> fills) {
        return new DungeonLayout(SIZE, fills, SPOT, List.of(SPOT), BlockBox.single(BlockPoint.ORIGIN), BlockPoint.ORIGIN);
    }
}
