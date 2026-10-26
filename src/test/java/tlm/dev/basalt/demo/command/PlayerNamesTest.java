package tlm.dev.basalt.demo.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerNamesTest {

    @Test
    void namesOfOneToSixteenLettersDigitsOrUnderscores() {
        assertTrue(PlayerNames.valid("tlmdotjar"));
        assertTrue(PlayerNames.valid("Steve"));
        assertTrue(PlayerNames.valid("A"));
        assertTrue(PlayerNames.valid("Alt_2026_xyzABCD"));
    }

    @Test
    void anythingElseIsRefused() {
        assertFalse(PlayerNames.valid(null));
        assertFalse(PlayerNames.valid(""));
        assertFalse(PlayerNames.valid("Seventeen_letters"));
        assertFalse(PlayerNames.valid("bad-name"));
        assertFalse(PlayerNames.valid("a.b"));
        assertFalse(PlayerNames.valid("<red>x"));
        assertFalse(PlayerNames.valid("é"));
    }
}
