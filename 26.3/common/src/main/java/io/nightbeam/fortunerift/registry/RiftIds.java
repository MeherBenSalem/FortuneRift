package io.nightbeam.fortunerift.registry;

import io.nightbeam.fortunerift.Constants;
import net.minecraft.resources.Identifier;

public final class RiftIds {
    private RiftIds() {
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, path);
    }

    public static Identifier parse(String id) {
        return Identifier.parse(id);
    }
}
