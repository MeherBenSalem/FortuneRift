package io.nightbeam.fortunerift.api;

import io.nightbeam.fortunerift.event.EventDefinition;
import io.nightbeam.fortunerift.event.EventRegistry;
import io.nightbeam.fortunerift.event.RiftEvent;
import io.nightbeam.fortunerift.event.RiftVariant;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public final class FortuneRiftApi {
    private static final List<RiftActivationListener> ACTIVATION = new ArrayList<>();
    private static final List<RiftCompletionListener> COMPLETION = new ArrayList<>();

    private FortuneRiftApi() {
    }

    public static void register(RiftEvent event) {
        EventDefinition definition = EventDefinition.builder(event.id())
                .name(event.name())
                .weight(event.weight())
                .rarity(event.rarity())
                .category(event.category())
                .script(event.id())
                .build();
        EventRegistry.add(definition);
    }

    public static void addActivationListener(RiftActivationListener listener) {
        ACTIVATION.add(listener);
    }

    public static void addCompletionListener(RiftCompletionListener listener) {
        COMPLETION.add(listener);
    }

    public static boolean allow(ServerPlayer player, RiftVariant variant) {
        for (RiftActivationListener listener : ACTIVATION) {
            if (!listener.allow(player, variant)) {
                return false;
            }
        }
        return true;
    }

    public interface RiftActivationListener {
        boolean allow(ServerPlayer player, RiftVariant variant);
    }

    public interface RiftCompletionListener {
        void onComplete(ServerPlayer player, String eventId, boolean positive);
    }
}
