package io.nightbeam.fortunerift.data;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ProfileStore {
    private static final Map<UUID, PlayerRiftProfile> CACHE = new ConcurrentHashMap<>();

    private ProfileStore() {
    }

    public static PlayerRiftProfile get(MinecraftServer server, UUID player) {
        return CACHE.computeIfAbsent(player, id -> read(server, id));
    }

    public static void save(MinecraftServer server, UUID player) {
        PlayerRiftProfile profile = CACHE.get(player);
        if (profile == null) {
            return;
        }
        try {
            Path file = file(server, player);
            Files.createDirectories(file.getParent());
            Files.writeString(file, profile.save(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    private static PlayerRiftProfile read(MinecraftServer server, UUID player) {
        Path file = file(server, player);
        if (Files.notExists(file)) {
            return new PlayerRiftProfile();
        }
        try {
            return PlayerRiftProfile.load(Files.readString(file, StandardCharsets.UTF_8));
        } catch (IOException | RuntimeException exception) {
            return new PlayerRiftProfile();
        }
    }

    private static Path file(MinecraftServer server, UUID player) {
        return server.getWorldPath(LevelResource.ROOT).resolve("fortunerift").resolve(player + ".txt");
    }
}
