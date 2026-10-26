package tlm.dev.basalt.demo.position;

public record BlockPoint(int x, int y, int z) {

    public static final BlockPoint ORIGIN = new BlockPoint(0, 0, 0);

    public BlockPoint plus(BlockPoint other) {
        return new BlockPoint(x + other.x, y + other.y, z + other.z);
    }

    public BlockPoint below() {
        return new BlockPoint(x, y - 1, z);
    }

    public BlockPoint above() {
        return new BlockPoint(x, y + 1, z);
    }

    @Override
    public String toString() {
        return x + " " + y + " " + z;
    }
}
