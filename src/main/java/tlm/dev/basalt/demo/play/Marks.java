package tlm.dev.basalt.demo.play;

import tlm.dev.basalt.demo.position.BlockBox;
import tlm.dev.basalt.demo.position.BlockPoint;
import tlm.dev.basalt.demo.position.Spot;

import java.util.List;
import java.util.Objects;

public record Marks(Spot spawn, List<Spot> monsters, BlockBox door, BlockPoint exit) {

    public Marks {
        Objects.requireNonNull(spawn, "spawn");
        Objects.requireNonNull(door, "door");
        Objects.requireNonNull(exit, "exit");
        monsters = List.copyOf(monsters);
    }
}
