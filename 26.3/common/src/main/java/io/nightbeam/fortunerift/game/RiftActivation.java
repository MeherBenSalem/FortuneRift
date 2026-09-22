package io.nightbeam.fortunerift.game;

import io.nightbeam.fortunerift.Constants;
import io.nightbeam.fortunerift.api.FortuneRiftApi;
import io.nightbeam.fortunerift.block.FortuneRiftBlock;
import io.nightbeam.fortunerift.config.FortuneConfig;
import io.nightbeam.fortunerift.data.PlayerRiftProfile;
import io.nightbeam.fortunerift.data.ProfileStore;
import io.nightbeam.fortunerift.event.ActivationGuard;
import io.nightbeam.fortunerift.event.EventDefinition;
import io.nightbeam.fortunerift.event.EventRegistry;
import io.nightbeam.fortunerift.event.RiftVariant;
import io.nightbeam.fortunerift.fortune.FortuneState;
import io.nightbeam.fortunerift.registry.RiftIds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public final class RiftActivation {
    private static final Random RANDOM = new Random();

    private RiftActivation() {
    }

    public static void activate(Level level, BlockPos pos, BlockState state, Player player, RiftVariant variant) {
        if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer owner)) {
            return;
        }
        FortuneConfig config = FortuneConfig.get();
        if (state.getValue(FortuneRiftBlock.PLAYER_PLACED) && config.allowSilkTouch && silk(server, owner)) {
            ItemStack stack = new ItemStack(state.getBlock());
            RiftActions.give(server, owner, pos, stack);
            return;
        }
        long packed = ActivationGuard.pack(pos.getX(), pos.getY(), pos.getZ());
        String dimension = server.dimension().identifier().toString();
        if (!EventTicker.guard().tryClaim(dimension, packed, owner.getUUID(), server.getGameTime())) {
            return;
        }
        if (!FortuneRiftApi.allow(owner, variant)) {
            return;
        }
        PlayerRiftProfile profile = ProfileStore.get(server.getServer(), owner.getUUID());
        EventDefinition event = EventRegistry.roll(variant, dimension, new FortuneState(profile.fortune), config, RANDOM);
        if (event == null) {
            Constants.LOG.info("No legal FortuneRift event at {}", pos);
            return;
        }
        EventSession session = new EventSession(server, pos);
        EventTicker.track(session);
        Constants.LOG.info("FortuneRift {} triggered {} for {}", variant.id(), event.id, owner.getScoreboardName());
        RiftActions.loot(server, owner, pos, "fortunerift:guaranteed", rewardRolls(variant));
        ScriptRunner.run(event, server, pos, owner, session);
        profile.opened++;
        profile.seen.add(event.id);
        profile.fortune = FortuneState.adjust(profile.fortune, event.positive);
        if (!event.positive) {
            profile.negative++;
        }
        if (profile.opened == 1) {
            var journal = BuiltInRegistries.ITEM.get(RiftIds.id("rift_journal"));
            journal.ifPresent(item -> {
                if (!owner.getInventory().contains(new ItemStack(item))) {
                    RiftActions.give(server, owner, pos, new ItemStack(item));
                }
            });
        }
        ProfileStore.save(server.getServer(), owner.getUUID());
        Advancements.grant(owner, profile, event);
    }

    private static int rewardRolls(RiftVariant variant) {
        return switch (variant) {
            case FORTUNE -> 1;
            case BLESSED, ANCIENT -> 2;
            case CURSED, VOID -> 2;
            case MYTHIC -> 3;
        };
    }

    private static boolean silk(ServerLevel level, ServerPlayer player) {
        var registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        return EnchantmentHelper.getEnchantmentLevel(registry.getOrThrow(Enchantments.SILK_TOUCH), player) > 0;
    }
}
