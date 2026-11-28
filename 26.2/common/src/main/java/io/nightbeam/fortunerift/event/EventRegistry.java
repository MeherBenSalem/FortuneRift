package io.nightbeam.fortunerift.event;

import io.nightbeam.fortunerift.Constants;
import io.nightbeam.fortunerift.config.FortuneConfig;
import io.nightbeam.fortunerift.event.json.EventJsonLoader;
import io.nightbeam.fortunerift.fortune.FortuneState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class EventRegistry {
    private static final Map<String, EventDefinition> BY_ID = new LinkedHashMap<>();
    private static boolean loaded;

    private EventRegistry() {
    }

    public static void loadDefaults() {
        if (loaded) {
            return;
        }
        InputStream index = EventRegistry.class.getResourceAsStream("/data/fortunerift/events/index.txt");
        if (index == null) {
            Constants.LOG.error("FortuneRift event index is missing");
            loaded = true;
            return;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(index, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String id = line.split("\t", 2)[0];
                try (InputStream json = EventRegistry.class.getResourceAsStream("/data/fortunerift/events/" + id + ".json")) {
                    if (json == null) {
                        continue;
                    }
                    EventDefinition event = EventJsonLoader.parse(new String(json.readAllBytes(), StandardCharsets.UTF_8));
                    BY_ID.put(event.id, event);
                }
            }
        } catch (IOException exception) {
            Constants.LOG.error("Failed to load FortuneRift events", exception);
        }
        loaded = true;
        Constants.LOG.info("Loaded {} FortuneRift events", BY_ID.size());
    }

    public static List<EventDefinition> definitions() {
        loadDefaults();
        return List.copyOf(BY_ID.values());
    }

    public static EventDefinition byId(String id) {
        loadDefaults();
        return BY_ID.get(id);
    }

    public static void add(EventDefinition event) {
        loadDefaults();
        BY_ID.put(event.id, event);
    }

    public static EventDefinition roll(RiftVariant variant, String dimension, FortuneState fortune, FortuneConfig config, Random random) {
        loadDefaults();
        SelectorRules rules = new SelectorRules(
                config.allowDestructiveEvents,
                config.allowTerrainModification,
                config.allowTntEvents,
                config.allowBossEvents,
                config.allowStructureEvents,
                config.enableMythicEvents,
                config.fortuneSystemEnabled,
                dimension,
                java.util.Set.of()
        );
        return EventSelector.select(new ArrayList<>(BY_ID.values()), variant, config.rarityWeights, fortune, rules, random);
    }
}
