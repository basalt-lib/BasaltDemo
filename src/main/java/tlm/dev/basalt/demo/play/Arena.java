package tlm.dev.basalt.demo.play;

import org.bukkit.World;
import tlm.dev.basalt.api.Blueprint;
import tlm.dev.basalt.demo.position.BlockPoint;

import java.util.Objects;
import java.util.function.Supplier;

public record Arena(Supplier<World> world, BlockPoint origin, Supplier<Blueprint> model, Marks marks) {

    public Arena {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(marks, "marks");
    }
}
