package tlm.dev.basalt.demo.message;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeTextTest {

    @Test
    void minutesAndSecondsUnderAnHour() {
        assertEquals("0:00", TimeText.clock(Duration.ZERO));
        assertEquals("0:09", TimeText.clock(Duration.ofMillis(9_999)));
        assertEquals("1:05", TimeText.clock(Duration.ofSeconds(65)));
        assertEquals("59:59", TimeText.clock(Duration.ofSeconds(3599)));
    }

    @Test
    void hoursWhenTheRunIsLong() {
        assertEquals("1:00:00", TimeText.clock(Duration.ofHours(1)));
        assertEquals("2:03:04", TimeText.clock(Duration.ofSeconds(2 * 3600 + 3 * 60 + 4)));
    }

    @Test
    void aNegativeDurationReadsAsZero() {
        assertEquals("0:00", TimeText.clock(Duration.ofSeconds(-5)));
    }
}
