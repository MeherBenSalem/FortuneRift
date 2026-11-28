package io.nightbeam.fortunerift.event;

public enum EventCategory {
    REWARD,
    MOB,
    TRAP,
    STRUCTURE,
    WORLD_EVENT,
    CHAOS,
    JACKPOT,
    MINIGAME,
    NPC,
    SECRET;

    public static EventCategory parse(String raw) {
        return EventCategory.valueOf(raw.trim().toUpperCase());
    }
}
