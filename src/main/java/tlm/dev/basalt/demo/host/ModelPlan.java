package tlm.dev.basalt.demo.host;

import tlm.dev.basalt.demo.position.BlockPoint;
import tlm.dev.basalt.demo.position.Spot;

import java.util.Objects;
import java.util.function.Function;

public record ModelPlan(BlockPoint size, Function<BlockPoint, String> blocks, BlockPoint marker, Spot entry) {

    public ModelPlan {
        Objects.requireNonNull(size, "size");
        Objects.requireNonNull(blocks, "blocks");
        Objects.requireNonNull(marker, "marker");
        Objects.requireNonNull(entry, "entry");
    }
}
