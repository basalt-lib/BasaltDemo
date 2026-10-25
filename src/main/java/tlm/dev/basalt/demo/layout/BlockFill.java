package tlm.dev.basalt.demo.layout;

import tlm.dev.basalt.demo.position.BlockBox;

import java.util.Objects;

public record BlockFill(BlockBox box, BlockKind kind) {

    public BlockFill {
        Objects.requireNonNull(box, "box");
        Objects.requireNonNull(kind, "kind");
    }
}
