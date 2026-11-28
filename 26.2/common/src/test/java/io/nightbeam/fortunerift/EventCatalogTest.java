package io.nightbeam.fortunerift;

import io.nightbeam.fortunerift.event.EventCategory;
import io.nightbeam.fortunerift.event.EventDefinition;
import io.nightbeam.fortunerift.event.Rarity;
import io.nightbeam.fortunerift.event.json.EventJsonLoader;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventCatalogTest {
    @Test
    void shipsOneHundredDistinctEvents() throws Exception {
        InputStream index = EventJsonLoader.class.getResourceAsStream("/data/fortunerift/events/index.txt");
        assertNotNull(index, "event index missing");
        Set<String> ids = new HashSet<>();
        Set<Rarity> rarities = EnumSet.noneOf(Rarity.class);
        Set<EventCategory> categories = EnumSet.noneOf(EventCategory.class);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(index, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String id = line.split("\t", 2)[0];
                try (InputStream json = EventJsonLoader.class.getResourceAsStream("/data/fortunerift/events/" + id + ".json")) {
                    assertNotNull(json, id);
                    EventDefinition event = EventJsonLoader.parse(new String(json.readAllBytes(), StandardCharsets.UTF_8));
                    assertEquals(id, event.id);
                    assertTrue(ids.add(id), id);
                    rarities.add(event.rarity);
                    categories.add(event.category);
                }
            }
        }
        assertEquals(100, ids.size());
        assertEquals(EnumSet.allOf(Rarity.class), rarities);
        assertEquals(EnumSet.allOf(EventCategory.class), categories);
    }
}
