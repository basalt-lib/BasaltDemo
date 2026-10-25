package tlm.dev.basalt.demo.layout;

import org.junit.jupiter.api.Test;
import tlm.dev.basalt.demo.position.BlockBox;
import tlm.dev.basalt.demo.position.BlockPoint;
import tlm.dev.basalt.demo.position.Spot;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultLayoutTest {

    private static final DungeonLayout LAYOUT = DefaultLayout.create();
    private static final List<BlockPoint> NEIGHBOURS = List.of(
            new BlockPoint(1, 0, 0), new BlockPoint(-1, 0, 0),
            new BlockPoint(0, 1, 0), new BlockPoint(0, -1, 0),
            new BlockPoint(0, 0, 1), new BlockPoint(0, 0, -1));

    @Test
    void everythingStandsInsideTheVolume() {
        BlockBox volume = LAYOUT.volume();
        assertEquals(new BlockPoint(32, 8, 16), volume.size());
        assertTrue(volume.contains(LAYOUT.door()));
        assertTrue(volume.contains(LAYOUT.exit()));
        assertTrue(volume.contains(LAYOUT.spawn().feet().above()));
        for (Spot monster : LAYOUT.monsters()) {
            assertTrue(volume.contains(monster.feet().above()), monster.toString());
        }
    }

    @Test
    void theOuterShellIsClosed() {
        BlockBox volume = LAYOUT.volume();
        forEach(volume, point -> {
            boolean onShell = point.x() == 0 || point.x() == 31 || point.y() == 0 || point.y() == 7
                    || point.z() == 0 || point.z() == 15;
            if (onShell) {
                assertTrue(LAYOUT.blockAt(point).solid(), point.toString());
            }
        });
    }

    @Test
    void noOneAppearsInsideASolidBlock() {
        List<Spot> spots = new ArrayList<>(LAYOUT.monsters());
        spots.add(LAYOUT.spawn());
        for (Spot spot : spots) {
            BlockPoint feet = spot.feet();
            assertEquals(BlockKind.AIR, LAYOUT.blockAt(feet), spot.toString());
            assertEquals(BlockKind.AIR, LAYOUT.blockAt(feet.above()), spot.toString());
            assertTrue(LAYOUT.blockAt(feet.below()).solid(), spot.toString());
        }
    }

    @Test
    void theDoorIsMadeOfIronAndTheExitOfGoldOnAPedestal() {
        forEach(LAYOUT.door(), point -> assertEquals(BlockKind.IRON_BLOCK, LAYOUT.blockAt(point), point.toString()));
        assertEquals(BlockKind.GOLD_BLOCK, LAYOUT.blockAt(LAYOUT.exit()));
        assertTrue(LAYOUT.blockAt(LAYOUT.exit().below()).solid());
        assertEquals(BlockKind.AIR, LAYOUT.blockAt(LAYOUT.exit().above()));
    }

    @Test
    void theClosedDoorShutsTheWholePassage() {
        Set<BlockPoint> reached = walk(kind -> kind == BlockKind.AIR);
        assertFalse(reached.contains(LAYOUT.exit().above()));
        assertTrue(LAYOUT.monsters().stream().allMatch(monster -> reached.contains(monster.feet())));
        forEach(LAYOUT.door(), point -> assertFalse(reached.contains(point)));
    }

    @Test
    void theOpenDoorLeadsToTheExit() {
        Set<BlockPoint> reached = walk(kind -> kind == BlockKind.AIR || kind == BlockKind.IRON_BLOCK);
        assertTrue(reached.contains(LAYOUT.exit().above()));
    }

    @Test
    void theMonstersWaitBetweenTheArchAndTheDoor() {
        for (Spot monster : LAYOUT.monsters()) {
            assertTrue(monster.x() > 10 && monster.x() < LAYOUT.door().min().x(), monster.toString());
        }
        assertTrue(LAYOUT.spawn().x() < 9);
        assertTrue(LAYOUT.exit().x() > LAYOUT.door().max().x());
    }

    private static Set<BlockPoint> walk(Predicate<BlockKind> open) {
        Set<BlockPoint> reached = new HashSet<>();
        Deque<BlockPoint> next = new ArrayDeque<>();
        next.add(LAYOUT.spawn().feet());
        reached.add(LAYOUT.spawn().feet());
        while (!next.isEmpty()) {
            BlockPoint point = next.poll();
            for (BlockPoint step : NEIGHBOURS) {
                BlockPoint neighbour = point.plus(step);
                if (LAYOUT.volume().contains(neighbour) && open.test(LAYOUT.blockAt(neighbour)) && reached.add(neighbour)) {
                    next.add(neighbour);
                }
            }
        }
        return reached;
    }

    private static void forEach(BlockBox box, Consumer<BlockPoint> action) {
        for (int x = box.min().x(); x <= box.max().x(); x++) {
            for (int y = box.min().y(); y <= box.max().y(); y++) {
                for (int z = box.min().z(); z <= box.max().z(); z++) {
                    action.accept(new BlockPoint(x, y, z));
                }
            }
        }
    }
}
