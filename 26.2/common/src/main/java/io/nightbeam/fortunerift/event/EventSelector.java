package io.nightbeam.fortunerift.event;

import io.nightbeam.fortunerift.config.RarityWeights;
import io.nightbeam.fortunerift.fortune.FortuneState;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class EventSelector {
    private EventSelector() {
    }

    public static EventDefinition select(
            List<EventDefinition> events,
            RiftVariant variant,
            RarityWeights weights,
            FortuneState fortune,
            SelectorRules rules,
            Random random
    ) {
        List<EventDefinition> legal = new ArrayList<>();
        for (EventDefinition event : events) {
            if (isLegal(event, variant, rules)) {
                legal.add(event);
            }
        }
        if (legal.isEmpty()) {
            return null;
        }

        Set<Rarity> pool = EnumSet.allOf(Rarity.class);
        if (variant == RiftVariant.MYTHIC) {
            pool.retainAll(EnumSet.of(Rarity.EPIC, Rarity.MYTHIC));
        }
        if (!rules.enableMythic) {
            pool.remove(Rarity.MYTHIC);
        }

        int total = 0;
        for (Rarity rarity : pool) {
            int weight = weights.weight(rarity);
            if (weight > 0) {
                total += weight;
            }
        }
        if (total <= 0) {
            return null;
        }

        int roll = random.nextInt(total);
        Rarity chosen = Rarity.COMMON;
        for (Rarity rarity : Rarity.values()) {
            if (!pool.contains(rarity) || weights.weight(rarity) <= 0) {
                continue;
            }
            roll -= weights.weight(rarity);
            if (roll < 0) {
                chosen = rarity;
                break;
            }
        }

        Rarity floor = variant == RiftVariant.MYTHIC ? Rarity.EPIC : Rarity.COMMON;
        Rarity ceiling = variant == RiftVariant.MYTHIC ? Rarity.MYTHIC : Rarity.MYTHIC;
        while (ofRarity(legal, chosen).isEmpty() && chosen != floor) {
            chosen = chosen.lower();
        }
        while (ofRarity(legal, chosen).isEmpty() && chosen != ceiling) {
            chosen = chosen.higher();
        }
        List<EventDefinition> bucket = ofRarity(legal, chosen);
        if (bucket.isEmpty()) {
            return null;
        }
        return weighted(bucket, variant, fortune.value(), rules.fortuneEnabled, random);
    }

    public static boolean isLegal(EventDefinition event, RiftVariant variant, SelectorRules rules) {
        if (!event.enabled || event.weight <= 0 || !event.variants.contains(variant)) {
            return false;
        }
        if (rules.dimension != null && !event.dimensions.isEmpty() && !event.dimensions.contains(rules.dimension)) {
            return false;
        }
        if (event.tnt && !rules.allowTnt) {
            return false;
        }
        if (event.destructive && !rules.allowDestructive) {
            return false;
        }
        if (event.terrain && !rules.allowTerrain) {
            return false;
        }
        if (event.boss && !rules.allowBoss) {
            return false;
        }
        if (event.structure && !rules.allowStructure) {
            return false;
        }
        if (event.rarity == Rarity.MYTHIC && !rules.enableMythic) {
            return false;
        }
        return !rules.cooldownIds.contains(event.id);
    }

    public static double variantBias(EventDefinition event, RiftVariant variant) {
        double bias = 1.0;
        switch (variant) {
            case BLESSED -> {
                if (event.category == EventCategory.REWARD || event.category == EventCategory.JACKPOT || event.category == EventCategory.NPC) {
                    bias *= 2.5;
                }
                if (event.category == EventCategory.TRAP || event.category == EventCategory.MOB) {
                    bias *= 0.35;
                }
            }
            case CURSED -> {
                if (event.category == EventCategory.TRAP || event.category == EventCategory.MOB || event.category == EventCategory.CHAOS) {
                    bias *= 2.2;
                }
                if (event.category == EventCategory.REWARD) {
                    bias *= 0.6;
                }
                if (event.rarity == Rarity.EPIC || event.rarity == Rarity.MYTHIC) {
                    bias *= 1.8;
                }
            }
            case ANCIENT -> {
                if (event.category == EventCategory.SECRET || event.category == EventCategory.STRUCTURE) {
                    bias *= 3.0;
                }
            }
            case VOID -> {
                if (event.destructive || event.boss) {
                    bias *= 3.0;
                }
                if (event.rarity == Rarity.COMMON && event.positive) {
                    bias *= 0.4;
                }
            }
            default -> {
            }
        }
        return bias;
    }

    private static List<EventDefinition> ofRarity(List<EventDefinition> events, Rarity rarity) {
        List<EventDefinition> bucket = new ArrayList<>();
        for (EventDefinition event : events) {
            if (event.rarity == rarity) {
                bucket.add(event);
            }
        }
        return bucket;
    }

    private static EventDefinition weighted(List<EventDefinition> bucket, RiftVariant variant, int fortune, boolean fortuneEnabled, Random random) {
        double total = 0.0;
        double[] scores = new double[bucket.size()];
        for (int i = 0; i < bucket.size(); i++) {
            EventDefinition event = bucket.get(i);
            double fortuneBias = event.positive
                    ? FortuneState.positiveMultiplier(fortune, fortuneEnabled)
                    : FortuneState.negativeMultiplier(fortune, fortuneEnabled);
            scores[i] = event.weight * variantBias(event, variant) * fortuneBias;
            if (scores[i] < 0) {
                scores[i] = 0;
            }
            total += scores[i];
        }
        if (total <= 0) {
            return bucket.get(random.nextInt(bucket.size()));
        }
        double roll = random.nextDouble() * total;
        for (int i = 0; i < bucket.size(); i++) {
            roll -= scores[i];
            if (roll <= 0) {
                return bucket.get(i);
            }
        }
        return bucket.get(bucket.size() - 1);
    }
}
