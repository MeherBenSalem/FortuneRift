package io.nightbeam.fortunerift;

import io.nightbeam.fortunerift.fortune.FortuneState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FortuneStateTest {
    @Test
    void positiveOutcomeDropsFortuneByEight() {
        assertEquals(42, FortuneState.adjust(50, true));
    }

    @Test
    void negativeOutcomeRaisesFortuneByTen() {
        assertEquals(60, FortuneState.adjust(50, false));
    }

    @Test
    void fortuneClampsAtBothEnds() {
        assertEquals(0, FortuneState.adjust(5, true));
        assertEquals(100, FortuneState.adjust(95, false));
        assertEquals(0, FortuneState.clamp(-4));
        assertEquals(100, FortuneState.clamp(140));
    }
}
