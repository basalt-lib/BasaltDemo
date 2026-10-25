package tlm.dev.basalt.demo.position;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PositionTest {

    @Test
    void boxOfTwoCornersInAnyOrder() {
        BlockBox box = BlockBox.of(new BlockPoint(5, 1, -2), new BlockPoint(-3, 4, 7));
        assertEquals(new BlockPoint(-3, 1, -2), box.min());
        assertEquals(new BlockPoint(5, 4, 7), box.max());
        assertEquals(new BlockPoint(9, 4, 10), box.size());
    }

    @Test
    void upsideDownBoxIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> new BlockBox(new BlockPoint(1, 0, 0), BlockPoint.ORIGIN));
        assertThrows(IllegalArgumentException.class, () -> BlockBox.sized(BlockPoint.ORIGIN, new BlockPoint(0, 1, 1)));
    }

    @Test
    void sizedBoxHoldsExactlyItsBlocks() {
        BlockBox box = BlockBox.sized(new BlockPoint(16, 64, 0), new BlockPoint(32, 8, 16));
        assertEquals(new BlockPoint(47, 71, 15), box.max());
        assertTrue(box.contains(new BlockPoint(16, 64, 0)));
        assertTrue(box.contains(new BlockPoint(47, 71, 15)));
        assertFalse(box.contains(new BlockPoint(48, 71, 15)));
        assertFalse(box.contains(new BlockPoint(47, 72, 15)));
        assertFalse(box.contains(new BlockPoint(16, 64, -1)));
    }

    @Test
    void boxesOverlapWhenTheyShareABlock() {
        BlockBox box = BlockBox.sized(BlockPoint.ORIGIN, new BlockPoint(32, 8, 16));
        assertTrue(box.overlaps(BlockBox.single(new BlockPoint(31, 7, 15))));
        assertFalse(box.overlaps(BlockBox.single(new BlockPoint(32, 7, 15))));
        assertTrue(box.contains(BlockBox.single(new BlockPoint(31, 7, 15))));
        assertFalse(box.contains(BlockBox.of(new BlockPoint(31, 0, 0), new BlockPoint(32, 0, 0))));
    }

    @Test
    void shiftMovesBothCorners() {
        BlockBox box = BlockBox.single(new BlockPoint(1, 2, 3)).shift(new BlockPoint(1024, 64, -16));
        assertEquals(BlockBox.single(new BlockPoint(1025, 66, -13)), box);
    }

    @Test
    void spotStandsInTheBlockUnderItsFeet() {
        assertEquals(new BlockPoint(3, 1, 7), new Spot(3.5, 1, 7.5, -90).feet());
        assertEquals(new BlockPoint(-1, -1, -8), new Spot(-0.5, -0.25, -7.5, 0).feet());
        assertEquals(new Spot(1027.5, 65, 7.5, -90), new Spot(3.5, 1, 7.5, -90).shift(new BlockPoint(1024, 64, 0)));
    }

    @Test
    void spotIsFinite() {
        assertThrows(IllegalArgumentException.class, () -> new Spot(Double.NaN, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Spot(0, Double.POSITIVE_INFINITY, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Spot(0, 0, 0, Float.NaN));
    }
}
