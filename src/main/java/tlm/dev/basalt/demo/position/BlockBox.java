package tlm.dev.basalt.demo.position;

import java.util.Objects;

public record BlockBox(BlockPoint min, BlockPoint max) {

    public BlockBox {
        Objects.requireNonNull(min, "min");
        Objects.requireNonNull(max, "max");
        if (min.x() > max.x() || min.y() > max.y() || min.z() > max.z()) {
            throw new IllegalArgumentException("the corner " + min + " is above the corner " + max);
        }
    }

    public static BlockBox of(BlockPoint first, BlockPoint last) {
        return new BlockBox(
                new BlockPoint(Math.min(first.x(), last.x()), Math.min(first.y(), last.y()), Math.min(first.z(), last.z())),
                new BlockPoint(Math.max(first.x(), last.x()), Math.max(first.y(), last.y()), Math.max(first.z(), last.z())));
    }

    public static BlockBox single(BlockPoint point) {
        return new BlockBox(point, point);
    }

    public static BlockBox sized(BlockPoint min, BlockPoint size) {
        if (size.x() < 1 || size.y() < 1 || size.z() < 1) {
            throw new IllegalArgumentException("a size is at least one block on each axis: " + size);
        }
        return new BlockBox(min, new BlockPoint(min.x() + size.x() - 1, min.y() + size.y() - 1, min.z() + size.z() - 1));
    }

    public boolean contains(BlockPoint point) {
        return point.x() >= min.x() && point.x() <= max.x()
                && point.y() >= min.y() && point.y() <= max.y()
                && point.z() >= min.z() && point.z() <= max.z();
    }

    public boolean contains(BlockBox box) {
        return contains(box.min) && contains(box.max);
    }

    public boolean overlaps(BlockBox other) {
        return min.x() <= other.max.x() && other.min.x() <= max.x()
                && min.y() <= other.max.y() && other.min.y() <= max.y()
                && min.z() <= other.max.z() && other.min.z() <= max.z();
    }

    public BlockBox shift(BlockPoint by) {
        return new BlockBox(min.plus(by), max.plus(by));
    }

    public BlockPoint size() {
        return new BlockPoint(max.x() - min.x() + 1, max.y() - min.y() + 1, max.z() - min.z() + 1);
    }
}
