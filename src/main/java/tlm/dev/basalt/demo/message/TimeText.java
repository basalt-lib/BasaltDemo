package tlm.dev.basalt.demo.message;

import java.time.Duration;

public final class TimeText {

    private TimeText() {
    }

    public static String clock(Duration duration) {
        long seconds = Math.max(0, duration.toSeconds());
        long hours = seconds / 3600;
        long minutes = seconds / 60 % 60;
        long rest = seconds % 60;
        return hours > 0
                ? hours + ":" + twoDigits(minutes) + ":" + twoDigits(rest)
                : minutes + ":" + twoDigits(rest);
    }

    private static String twoDigits(long value) {
        return value < 10 ? "0" + value : Long.toString(value);
    }
}
