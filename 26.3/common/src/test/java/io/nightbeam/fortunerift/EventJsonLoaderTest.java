package io.nightbeam.fortunerift;

import io.nightbeam.fortunerift.event.EventDefinition;
import io.nightbeam.fortunerift.event.json.EventJsonLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class EventJsonLoaderTest {
    @Test
    void parsesLootBurst() {
        EventDefinition event = EventJsonLoader.parse("""
                {
                  "id": "food_feast",
                  "name": "Food Feast",
                  "description": "A stack of food.",
                  "weight": 10,
                  "rarity": "COMMON",
                  "category": "REWARD",
                  "type": "loot_burst",
                  "loot_table": "fortunerift:common",
                  "positive": true,
                  "enabled": true
                }
                """);
        assertEquals("food_feast", event.id);
        assertEquals("loot_burst", event.type);
        assertEquals("fortunerift:common", event.lootTable);
    }

    @Test
    void overrideChangesWeightWithoutReplacingScript() {
        EventDefinition builtin = EventDefinition.builder("diamond_rain")
                .script("diamond_rain")
                .weight(10)
                .enabled(true)
                .build();
        EventDefinition override = EventJsonLoader.parse("""
                {
                  "id": "diamond_rain",
                  "weight": 1,
                  "enabled": false
                }
                """);
        EventDefinition merged = EventJsonLoader.merge(builtin, override);
        assertEquals(1, merged.weight);
        assertFalse(merged.enabled);
        assertEquals("diamond_rain", merged.script);
    }
}
