package io.nightbeam.fortunerift.event;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ActivationGuard {
    public static final int POSITION_LOCK_TICKS = 10;
    public static final int CHUNK_WINDOW_TICKS = 20 * 60;

    private final int playerCooldown;
    private final int globalCooldown;
    private final int chunkLimit;
    private final Map<String, Long> positionClaims = new HashMap<>();
    private final Map<UUID, Long> playerClaims = new HashMap<>();
    private final Map<String, Deque<Long>> chunkClaims = new HashMap<>();
    private long lastGlobal = Long.MIN_VALUE;

    public ActivationGuard(int playerCooldown, int globalCooldown, int chunkLimit) {
        this.playerCooldown = playerCooldown;
        this.globalCooldown = globalCooldown;
        this.chunkLimit = chunkLimit;
    }

    public boolean tryClaim(String level, long pos, UUID player, long gameTime) {
        String positionKey = level + ":" + pos;
        Long positionAt = positionClaims.get(positionKey);
        if (positionAt != null && gameTime - positionAt < POSITION_LOCK_TICKS) {
            return false;
        }
        Long playerAt = playerClaims.get(player);
        if (playerCooldown > 0 && playerAt != null && gameTime - playerAt < playerCooldown) {
            return false;
        }
        if (globalCooldown > 0 && lastGlobal != Long.MIN_VALUE && gameTime - lastGlobal < globalCooldown) {
            return false;
        }
        String chunkKey = level + ":" + chunkX(pos) + ":" + chunkZ(pos);
        Deque<Long> times = chunkClaims.computeIfAbsent(chunkKey, key -> new ArrayDeque<>());
        while (!times.isEmpty() && gameTime - times.peekFirst() >= CHUNK_WINDOW_TICKS) {
            times.removeFirst();
        }
        if (chunkLimit > 0 && times.size() >= chunkLimit) {
            return false;
        }

        positionClaims.put(positionKey, gameTime);
        playerClaims.put(player, gameTime);
        times.addLast(gameTime);
        lastGlobal = gameTime;
        return true;
    }

    public static long pack(int x, int y, int z) {
        return ((long) x & 0x3FFFFFFL) << 38 | ((long) y & 0xFFFL) << 26 | ((long) z & 0x3FFFFFFL);
    }

    public static int chunkX(long packed) {
        return ((int) (packed >> 38)) >> 4;
    }

    public static int chunkZ(long packed) {
        return ((int) (packed << 38 >> 38)) >> 4;
    }
}
