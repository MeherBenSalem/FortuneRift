package io.nightbeam.fortunerift.event;

public enum Rarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    MYTHIC;

    public static Rarity parse(String raw) {
        return Rarity.valueOf(raw.trim().toUpperCase());
    }

    public Rarity lower() {
        return switch (this) {
            case MYTHIC -> EPIC;
            case EPIC -> RARE;
            case RARE -> UNCOMMON;
            case UNCOMMON, COMMON -> COMMON;
        };
    }

    public Rarity higher() {
        return switch (this) {
            case COMMON -> UNCOMMON;
            case UNCOMMON -> RARE;
            case RARE -> EPIC;
            case EPIC, MYTHIC -> MYTHIC;
        };
    }
}
