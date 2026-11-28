package io.nightbeam.fortunerift;

import io.nightbeam.fortunerift.event.ActivationGuard;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActivationGuardTest {
    @Test
    void samePositionIsLockedForTenTicks() {
        ActivationGuard guard = new ActivationGuard(40, 0, 3);
        long pos = ActivationGuard.pack(8, 64, 8);
        assertTrue(guard.tryClaim("overworld", pos, UUID.randomUUID(), 100));
        assertFalse(guard.tryClaim("overworld", pos, UUID.randomUUID(), 109));
        assertTrue(guard.tryClaim("overworld", pos, UUID.randomUUID(), 110));
    }

    @Test
    void playerCooldownBlocksASecondBreak() {
        ActivationGuard guard = new ActivationGuard(40, 0, 3);
        UUID player = UUID.randomUUID();
        assertTrue(guard.tryClaim("overworld", ActivationGuard.pack(0, 64, 0), player, 0));
        assertFalse(guard.tryClaim("overworld", ActivationGuard.pack(32, 64, 0), player, 39));
    }

    @Test
    void fourthEventInAChunkInsideOneMinuteFails() {
        ActivationGuard guard = new ActivationGuard(0, 0, 3);
        for (int i = 0; i < 3; i++) {
            assertTrue(guard.tryClaim("overworld", ActivationGuard.pack(i, 64, 0), UUID.randomUUID(), i));
        }
        assertFalse(guard.tryClaim("overworld", ActivationGuard.pack(4, 64, 0), UUID.randomUUID(), 4));
    }
}
