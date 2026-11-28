package io.nightbeam.fortunerift.event;

public enum RiftVariant {
    FORTUNE("fortune_rift"),
    BLESSED("blessed_rift"),
    CURSED("cursed_rift"),
    ANCIENT("ancient_rift"),
    VOID("void_rift"),
    MYTHIC("mythic_rift");

    private final String id;

    RiftVariant(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static RiftVariant parse(String raw) {
        String id = raw.trim().toLowerCase();
        for (RiftVariant variant : values()) {
            if (variant.id.equals(id) || variant.name().equalsIgnoreCase(id)) {
                return variant;
            }
        }
        throw new IllegalArgumentException("Unknown rift variant: " + raw);
    }
}
