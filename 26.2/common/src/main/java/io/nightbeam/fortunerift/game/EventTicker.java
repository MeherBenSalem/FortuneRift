package io.nightbeam.fortunerift.game;

import io.nightbeam.fortunerift.config.FortuneConfig;
import io.nightbeam.fortunerift.event.ActivationGuard;
import io.nightbeam.fortunerift.registry.RiftIds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class EventTicker {
    private static final List<EventSession> SESSIONS = new ArrayList<>();
    private static ActivationGuard guard;

    private EventTicker() {
    }

    public static ActivationGuard guard() {
        FortuneConfig config = FortuneConfig.get();
        if (guard == null) {
            guard = new ActivationGuard(config.playerEventCooldown, config.globalEventCooldown, config.chunkEventLimit);
        }
        return guard;
    }

    public static void resetGuard() {
        guard = null;
    }

    public static void track(EventSession session) {
        SESSIONS.add(session);
    }

    public static void tick(MinecraftServer server) {
        long time = server.overworld().getGameTime();
        for (EventSession session : List.copyOf(SESSIONS)) {
            session.tick(time);
            if (session.ended()) {
                SESSIONS.remove(session);
            }
        }
        if (time % 20L == 0L) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (holdingHeart(player)) {
                    player.addEffect(new MobEffectInstance(MobEffects.LUCK, 40, 0, true, false));
                }
            }
        }
    }

    private static boolean holdingHeart(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(RiftIds.id("rift_heart"))) {
                return true;
            }
        }
        return false;
    }
}
