package io.nightbeam.fortunerift;

import io.nightbeam.fortunerift.config.RarityWeights;
import io.nightbeam.fortunerift.event.EventCategory;
import io.nightbeam.fortunerift.event.EventDefinition;
import io.nightbeam.fortunerift.event.EventSelector;
import io.nightbeam.fortunerift.event.Rarity;
import io.nightbeam.fortunerift.event.RiftVariant;
import io.nightbeam.fortunerift.event.SelectorRules;
import io.nightbeam.fortunerift.fortune.FortuneState;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventSelectorTest {
    @Test
    void rarityDistributionMatchesDefaults() {
        List<EventDefinition> events = List.of(
                event("c", Rarity.COMMON, EventCategory.REWARD, true, false),
                event("u", Rarity.UNCOMMON, EventCategory.REWARD, true, false),
                event("r", Rarity.RARE, EventCategory.REWARD, true, false),
                event("e", Rarity.EPIC, EventCategory.REWARD, true, false),
                event("m", Rarity.MYTHIC, EventCategory.REWARD, true, false)
        );
        Map<Rarity, Integer> counts = new EnumMap<>(Rarity.class);
        for (Rarity rarity : Rarity.values()) {
            counts.put(rarity, 0);
        }
        Random random = new Random(1);
        int rolls = 10_000;
        for (int i = 0; i < rolls; i++) {
            EventDefinition picked = EventSelector.select(events, RiftVariant.FORTUNE, RarityWeights.defaults(), new FortuneState(50), SelectorRules.open(), random);
            assertNotNull(picked);
            counts.merge(picked.rarity, 1, Integer::sum);
        }
        assertClose(counts.get(Rarity.COMMON), rolls, 45);
        assertClose(counts.get(Rarity.UNCOMMON), rolls, 28);
        assertClose(counts.get(Rarity.RARE), rolls, 17);
        assertClose(counts.get(Rarity.EPIC), rolls, 8);
        assertClose(counts.get(Rarity.MYTHIC), rolls, 2);
    }

    @Test
    void mythicRiftNeverRollsBelowEpic() {
        List<EventDefinition> events = List.of(
                event("c", Rarity.COMMON, EventCategory.REWARD, true, false),
                event("u", Rarity.UNCOMMON, EventCategory.REWARD, true, false),
                event("r", Rarity.RARE, EventCategory.REWARD, true, false),
                event("e", Rarity.EPIC, EventCategory.REWARD, true, false),
                event("m", Rarity.MYTHIC, EventCategory.REWARD, true, false)
        );
        Random random = new Random(1);
        for (int i = 0; i < 500; i++) {
            EventDefinition picked = EventSelector.select(events, RiftVariant.MYTHIC, RarityWeights.defaults(), new FortuneState(50), SelectorRules.open(), random);
            assertNotNull(picked);
            assertTrue(picked.rarity == Rarity.EPIC || picked.rarity == Rarity.MYTHIC, picked.id);
        }
    }

    @Test
    void disabledTntIsNeverChosen() {
        List<EventDefinition> events = List.of(
                event("boom", Rarity.UNCOMMON, EventCategory.TRAP, false, true),
                event("safe", Rarity.UNCOMMON, EventCategory.REWARD, true, false)
        );
        Random random = new Random(1);
        for (int i = 0; i < 100; i++) {
            EventDefinition picked = EventSelector.select(events, RiftVariant.FORTUNE, RarityWeights.defaults(), new FortuneState(50), SelectorRules.open().withTnt(false), random);
            assertEquals("safe", picked.id);
        }
    }

    @Test
    void blessedBiasPrefersRewardOverTrap() {
        List<EventDefinition> events = List.of(
                event("gift", Rarity.COMMON, EventCategory.REWARD, true, false),
                event("snare", Rarity.COMMON, EventCategory.TRAP, false, false)
        );
        int rewards = 0;
        Random random = new Random(1);
        for (int i = 0; i < 2_000; i++) {
            EventDefinition picked = EventSelector.select(events, RiftVariant.BLESSED, RarityWeights.defaults(), new FortuneState(50), SelectorRules.open(), random);
            if ("gift".equals(picked.id)) {
                rewards++;
            }
        }
        assertTrue(rewards > 1_400, "rewards=" + rewards);
    }

    @Test
    void highFortuneIncreasesPositiveRate() {
        List<EventDefinition> events = List.of(
                event("gift", Rarity.COMMON, EventCategory.REWARD, true, false),
                event("snare", Rarity.COMMON, EventCategory.TRAP, false, false)
        );
        int high = countPositive(events, 100);
        int low = countPositive(events, 0);
        assertTrue(high > low, "high=" + high + " low=" + low);
    }

    @Test
    void disabledEventIsRemoved() {
        List<EventDefinition> events = new ArrayList<>();
        events.add(event("gift", Rarity.COMMON, EventCategory.REWARD, true, false).toBuilder().enabled(false).build());
        events.add(event("other", Rarity.COMMON, EventCategory.REWARD, true, false));
        Random random = new Random(1);
        for (int i = 0; i < 50; i++) {
            EventDefinition picked = EventSelector.select(events, RiftVariant.FORTUNE, RarityWeights.defaults(), new FortuneState(50), SelectorRules.open(), random);
            assertEquals("other", picked.id);
        }
    }

    private static int countPositive(List<EventDefinition> events, int fortune) {
        int positive = 0;
        Random random = new Random(1);
        for (int i = 0; i < 2_000; i++) {
            EventDefinition picked = EventSelector.select(events, RiftVariant.FORTUNE, RarityWeights.defaults(), new FortuneState(fortune), SelectorRules.open(), random);
            if (picked.positive) {
                positive++;
            }
        }
        return positive;
    }

    private static void assertClose(int count, int rolls, int percent) {
        double actual = count * 100.0 / rolls;
        assertTrue(Math.abs(actual - percent) <= 2.0, "expected " + percent + " got " + actual);
    }

    private static EventDefinition event(String id, Rarity rarity, EventCategory category, boolean positive, boolean tnt) {
        return EventDefinition.builder(id)
                .rarity(rarity)
                .category(category)
                .positive(positive)
                .tnt(tnt)
                .weight(10)
                .build();
    }
}
