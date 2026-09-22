package io.nightbeam.fortunerift.event;

public interface RiftEvent {
    String id();

    String name();

    int weight();

    Rarity rarity();

    EventCategory category();
}
