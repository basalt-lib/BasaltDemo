package tlm.dev.basalt.demo.run;

import org.junit.jupiter.api.Test;
import tlm.dev.basalt.demo.config.RunSettings;
import tlm.dev.basalt.demo.port.Entry;
import tlm.dev.basalt.demo.port.Progress;
import tlm.dev.basalt.demo.port.Refusal;
import tlm.dev.basalt.demo.port.RunView;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunRegistryTest {

    private static final Instant START = Instant.parse("2026-10-26T12:00:00Z");
    private static final UUID ANNA = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final UUID BEN = UUID.fromString("00000000-0000-0000-0000-00000000000b");
    private static final UUID CARL = UUID.fromString("00000000-0000-0000-0000-00000000000c");
    private static final UUID DORA = UUID.fromString("00000000-0000-0000-0000-00000000000d");

    private final RunRegistry registry = new RunRegistry(new RunSettings(2, 3, 6, 8), UUID::randomUUID);

    @Test
    void aStartOpensARunLedByThePlayer() {
        RunView run = admitted(registry.open(ANNA, 3, START));
        assertEquals(List.of(ANNA), run.members());
        assertEquals(ANNA, run.leader());
        assertEquals(3, run.monstersLeft());
        assertFalse(run.doorOpen());
        assertFalse(run.finished());
        assertEquals(Optional.of(run), registry.runOf(ANNA));
        assertEquals(List.of(run), registry.runs());
    }

    @Test
    void aPlayerIsInOneRunAtMost() {
        RunView run = admitted(registry.open(ANNA, 3, START));
        assertEquals(Refusal.ALREADY_IN_RUN, refused(registry.open(ANNA, 3, START)));
        admitted(registry.open(BEN, 3, START));
        assertEquals(Refusal.ALREADY_IN_RUN, refused(registry.join(ANNA, BEN)));
        assertEquals(Refusal.ALREADY_IN_RUN, refused(registry.join(ANNA, ANNA)));
        assertEquals(List.of(ANNA), registry.run(run.id()).orElseThrow().members());
    }

    @Test
    void theRunLimitRefusesOneMore() {
        admitted(registry.open(ANNA, 3, START));
        admitted(registry.open(BEN, 3, START));
        assertEquals(Refusal.TOO_MANY_RUNS, refused(registry.open(CARL, 3, START)));
        assertEquals(Optional.empty(), registry.runOf(CARL));
        registry.leave(BEN);
        admitted(registry.open(CARL, 3, START));
    }

    @Test
    void aJoinNeedsAMemberOfAnOpenRunWithRoom() {
        assertEquals(Refusal.NOT_IN_RUN, refused(registry.join(ANNA, BEN)));
        admitted(registry.open(ANNA, 3, START));
        RunView joined = admitted(registry.join(ANNA, BEN));
        assertEquals(List.of(ANNA, BEN), joined.members());
        assertEquals(List.of(ANNA, BEN, CARL), admitted(registry.join(BEN, CARL)).members());
        assertEquals(Refusal.RUN_FULL, refused(registry.join(ANNA, DORA)));
    }

    @Test
    void aFinishedRunTakesNobodyElse() {
        RunView run = admitted(registry.open(ANNA, 1, START));
        registry.monsterDied(run.id());
        registry.finish(run.id(), START.plusSeconds(30));
        assertEquals(Refusal.RUN_FINISHED, refused(registry.join(ANNA, BEN)));
    }

    @Test
    void theDoorOpensOnceWhenTheLastMonsterDies() {
        RunView run = admitted(registry.open(ANNA, 3, START));
        assertEquals(Progress.COUNTED, registry.monsterDied(run.id()));
        assertEquals(Progress.COUNTED, registry.monsterDied(run.id()));
        assertEquals(1, registry.run(run.id()).orElseThrow().monstersLeft());
        assertEquals(Progress.DOOR_OPENED, registry.monsterDied(run.id()));
        assertEquals(Progress.IGNORED, registry.monsterDied(run.id()));
        RunView after = registry.run(run.id()).orElseThrow();
        assertEquals(0, after.monstersLeft());
        assertTrue(after.doorOpen());
    }

    @Test
    void aDeathInAnUnknownRunIsIgnored() {
        assertEquals(Progress.IGNORED, registry.monsterDied(UUID.randomUUID()));
    }

    @Test
    void theRunFinishesOnceAndOnlyBehindAnOpenDoor() {
        RunView run = admitted(registry.open(ANNA, 1, START));
        assertEquals(Optional.empty(), registry.finish(run.id(), START.plusSeconds(10)));
        registry.monsterDied(run.id());
        assertEquals(Optional.of(Duration.ofSeconds(95)), registry.finish(run.id(), START.plusSeconds(95)));
        assertEquals(Optional.empty(), registry.finish(run.id(), START.plusSeconds(96)));
        assertTrue(registry.run(run.id()).orElseThrow().finished());
        assertEquals(Optional.empty(), registry.finish(UUID.randomUUID(), START));
    }

    @Test
    void theLeaderLeavesThenTheOthers() {
        RunView run = admitted(registry.open(ANNA, 3, START));
        registry.join(ANNA, BEN);
        registry.join(BEN, CARL);
        RunView afterLeader = registry.leave(ANNA).orElseThrow();
        assertEquals(List.of(BEN, CARL), afterLeader.members());
        assertEquals(BEN, afterLeader.leader());
        assertEquals(Refusal.NOT_IN_RUN, refused(registry.join(ANNA, DORA)));
        assertEquals(List.of(BEN, CARL, DORA), admitted(registry.join(CARL, DORA)).members());
        registry.leave(BEN);
        registry.leave(DORA);
        RunView last = registry.leave(CARL).orElseThrow();
        assertTrue(last.empty());
        assertEquals(Optional.empty(), registry.run(run.id()));
        assertEquals(List.of(), registry.runs());
    }

    @Test
    void leavingWithoutARunChangesNothing() {
        admitted(registry.open(ANNA, 3, START));
        assertEquals(Optional.empty(), registry.leave(BEN));
        assertEquals(1, registry.runs().size());
    }

    @Test
    void aCancelledRunFreesItsMembers() {
        RunView run = admitted(registry.open(ANNA, 3, START));
        registry.join(ANNA, BEN);
        registry.cancel(run.id());
        assertEquals(Optional.empty(), registry.runOf(ANNA));
        assertEquals(Optional.empty(), registry.runOf(BEN));
        assertEquals(List.of(), registry.runs());
        admitted(registry.open(ANNA, 3, START));
    }

    @Test
    void aRunWithoutMonsterIsAProgrammingError() {
        assertThrows(IllegalArgumentException.class, () -> registry.open(ANNA, 0, START));
    }

    private static RunView admitted(Entry entry) {
        return assertInstanceOf(Entry.Admitted.class, entry).run();
    }

    private static Refusal refused(Entry entry) {
        return assertInstanceOf(Entry.Refused.class, entry).reason();
    }
}
