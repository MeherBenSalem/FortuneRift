package io.nightbeam.fortunerift.game;

import io.nightbeam.fortunerift.registry.RiftIds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class RiftActions {
    private RiftActions() {
    }

    public static ServerPlayer owner(ServerLevel level, java.util.UUID id) {
        return level.getServer().getPlayerList().getPlayer(id);
    }

    public static List<ServerPlayer> nearby(ServerLevel level, BlockPos pos, int radius) {
        List<ServerPlayer> players = new ArrayList<>();
        for (ServerPlayer player : level.players()) {
            if (player.blockPosition().distManhattan(pos) <= radius) {
                players.add(player);
            }
        }
        return players;
    }

    public static void message(ServerPlayer player, String text) {
        if (player != null) {
            player.sendSystemMessage(Component.literal(text));
        }
    }

    public static void title(ServerPlayer player, String text) {
        if (player != null) {
            player.sendSystemMessage(Component.literal(text));
        }
    }

    public static void loot(ServerLevel level, ServerPlayer player, BlockPos pos, String table, int times) {
        ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, RiftIds.parse(table.contains(":") ? table : "fortunerift:" + table));
        LootTable loot = level.getServer().reloadableRegistries().getLootTable(key);
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
                .create(LootContextParamSets.CHEST);
        for (int i = 0; i < times; i++) {
            for (ItemStack stack : loot.getRandomItems(params)) {
                give(level, player, pos, stack);
            }
        }
    }

    public static void give(ServerLevel level, ServerPlayer player, BlockPos pos, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        if (player != null && player.level() == level && player.addItem(stack.copy())) {
            return;
        }
        ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, stack.copy());
        level.addFreshEntity(entity);
    }

    public static void rain(ServerLevel level, BlockPos pos, String itemId, int count, boolean fountain) {
        var item = item(itemId);
        for (int i = 0; i < count; i++) {
            ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, new ItemStack(item));
            double angle = i * 0.7;
            entity.setDeltaMovement(Math.cos(angle) * 0.2, fountain ? 0.6 : 0.35, Math.sin(angle) * 0.2);
            level.addFreshEntity(entity);
        }
    }

    public static Entity spawn(ServerLevel level, BlockPos pos, String entityId, double yOffset) {
        var type = BuiltInRegistries.ENTITY_TYPE.get(RiftIds.parse(entityId));
        if (type.isEmpty()) {
            return null;
        }
        Entity entity = type.get().value().create(level, EntitySpawnReason.EVENT);
        if (entity == null) {
            return null;
        }
        entity.setPos(pos.getX() + 0.5, pos.getY() + yOffset, pos.getZ() + 0.5);
        level.addFreshEntity(entity);
        return entity;
    }

    public static void effect(ServerPlayer player, String effectId, int seconds, int amplifier) {
        if (player == null) {
            return;
        }
        BuiltInRegistries.MOB_EFFECT.get(RiftIds.parse(effectId)).ifPresent(effect ->
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(effect, seconds * 20, amplifier))
        );
    }

    public static void set(EventSession session, ServerLevel level, BlockPos pos, BlockState state) {
        session.restore(pos, level.getBlockState(pos));
        level.setBlock(pos, state, Block.UPDATE_ALL);
    }

    public static net.minecraft.world.item.Item item(String itemId) {
        return BuiltInRegistries.ITEM.get(RiftIds.parse(itemId)).map(holder -> holder.value()).orElse(net.minecraft.world.item.Items.AIR);
    }

    public static BlockPos standable(ServerLevel level, BlockPos origin) {
        BlockPos.MutableBlockPos cursor = origin.mutable();
        for (int dy = 0; dy < 6; dy++) {
            cursor.set(origin.getX(), origin.getY() + dy, origin.getZ());
            if (level.getBlockState(cursor).isAir() && level.getBlockState(cursor.above()).isAir()) {
                return cursor.immutable();
            }
        }
        return origin.above();
    }
}
