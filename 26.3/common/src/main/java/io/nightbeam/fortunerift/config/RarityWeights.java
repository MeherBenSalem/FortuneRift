package io.nightbeam.fortunerift.config;

import io.nightbeam.fortunerift.event.Rarity;

public final class RarityWeights {
    public final int common;
    public final int uncommon;
    public final int rare;
    public final int epic;
    public final int mythic;

    public RarityWeights(int common, int uncommon, int rare, int epic, int mythic) {
        this.common = common;
        this.uncommon = uncommon;
        this.rare = rare;
        this.epic = epic;
        this.mythic = mythic;
    }

    public static RarityWeights defaults() {
        return new RarityWeights(45, 28, 17, 8, 2);
    }

    public int weight(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> common;
            case UNCOMMON -> uncommon;
            case RARE -> rare;
            case EPIC -> epic;
            case MYTHIC -> mythic;
        };
    }
}
