package io.nightbeam.fortunerift.config;

import io.nightbeam.fortunerift.Constants;
import io.nightbeam.fortunerift.event.json.EventJsonLoader;
import io.nightbeam.fortunerift.platform.Services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class FortuneConfig {
    public boolean riftGenerationEnabled = true;
    public double riftGenerationChance = 0.08;
    public boolean fortuneSystemEnabled = true;
    public int eventRadius = 24;
    public boolean allowStructureEvents = true;
    public boolean allowDestructiveEvents = true;
    public boolean allowTerrainModification = true;
    public boolean allowTntEvents = true;
    public boolean allowBossEvents = true;
    public boolean enableMythicEvents = true;
    public boolean allowSilkTouch = false;
    public int globalEventCooldown = 0;
    public int playerEventCooldown = 40;
    public int chunkEventLimit = 3;
    public RarityWeights rarityWeights = RarityWeights.defaults();

    private static FortuneConfig current = new FortuneConfig();

    private FortuneConfig() {
    }

    public static FortuneConfig get() {
        return current;
    }

    public static FortuneConfig load() {
        FortuneConfig config = new FortuneConfig();
        Path file = Services.PLATFORM.configDirectory().resolve("fortunerift.json");
        try {
            if (Files.notExists(file)) {
                Files.createDirectories(file.getParent());
                Files.writeString(file, defaults(), StandardCharsets.UTF_8);
            }
            config.read(EventJsonLoader.parseObject(Files.readString(file, StandardCharsets.UTF_8)));
        } catch (IOException | RuntimeException exception) {
            Constants.LOG.error("Could not read config/fortunerift.json, using defaults", exception);
        }
        current = config;
        return config;
    }

    private void read(Map<String, Object> map) {
        riftGenerationEnabled = bool(map, "riftGenerationEnabled", riftGenerationEnabled);
        riftGenerationChance = number(map, "riftGenerationChance", riftGenerationChance);
        fortuneSystemEnabled = bool(map, "fortuneSystemEnabled", fortuneSystemEnabled);
        eventRadius = (int) number(map, "eventRadius", eventRadius);
        allowStructureEvents = bool(map, "allowStructureEvents", allowStructureEvents);
        allowDestructiveEvents = bool(map, "allowDestructiveEvents", allowDestructiveEvents);
        allowTerrainModification = bool(map, "allowTerrainModification", allowTerrainModification);
        allowTntEvents = bool(map, "allowTNTEvents", allowTntEvents);
        allowBossEvents = bool(map, "allowBossEvents", allowBossEvents);
        enableMythicEvents = bool(map, "enableMythicEvents", enableMythicEvents);
        allowSilkTouch = bool(map, "allowSilkTouch", allowSilkTouch);
        globalEventCooldown = (int) number(map, "globalEventCooldown", globalEventCooldown);
        playerEventCooldown = (int) number(map, "playerEventCooldown", playerEventCooldown);
        chunkEventLimit = (int) number(map, "chunkEventLimit", chunkEventLimit);
        if (map.get("rarityWeights") instanceof Map<?, ?> weights) {
            rarityWeights = new RarityWeights(
                    (int) number(weights, "common", 45),
                    (int) number(weights, "uncommon", 28),
                    (int) number(weights, "rare", 17),
                    (int) number(weights, "epic", 8),
                    (int) number(weights, "mythic", 2)
            );
        }
    }

    private static boolean bool(Map<?, ?> map, String key, boolean fallback) {
        Object value = map.get(key);
        return value instanceof Boolean bool ? bool : fallback;
    }

    private static double number(Map<?, ?> map, String key, double fallback) {
        Object value = map.get(key);
        return value instanceof Number number ? number.doubleValue() : fallback;
    }

    private static String defaults() {
        return """
                {
                  "riftGenerationEnabled": true,
                  "riftGenerationChance": 0.08,
                  "fortuneSystemEnabled": true,
                  "eventRadius": 24,
                  "allowStructureEvents": true,
                  "allowDestructiveEvents": true,
                  "allowTerrainModification": true,
                  "allowTNTEvents": true,
                  "allowBossEvents": true,
                  "enableMythicEvents": true,
                  "allowSilkTouch": false,
                  "globalEventCooldown": 0,
                  "playerEventCooldown": 40,
                  "chunkEventLimit": 3,
                  "rarityWeights": {"common": 45, "uncommon": 28, "rare": 17, "epic": 8, "mythic": 2}
                }
                """;
    }
}
