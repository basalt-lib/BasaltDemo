package tlm.dev.basalt.demo.position;

public record Spot(double x, double y, double z, float yaw) {

    public Spot {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z) || !Float.isFinite(yaw)) {
            throw new IllegalArgumentException("a spot is finite: " + x + " " + y + " " + z + " " + yaw);
        }
    }

    public BlockPoint feet() {
        return new BlockPoint((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    public Spot shift(BlockPoint by) {
        return new Spot(x + by.x(), y + by.y(), z + by.z(), yaw);
    }
}
