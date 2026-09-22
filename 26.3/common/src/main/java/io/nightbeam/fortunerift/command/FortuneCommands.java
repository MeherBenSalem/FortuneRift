package io.nightbeam.fortunerift.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.nightbeam.fortunerift.config.FortuneConfig;
import io.nightbeam.fortunerift.data.PlayerRiftProfile;
import io.nightbeam.fortunerift.data.ProfileStore;
import io.nightbeam.fortunerift.event.EventDefinition;
import io.nightbeam.fortunerift.event.EventRegistry;
import io.nightbeam.fortunerift.event.RiftVariant;
import io.nightbeam.fortunerift.fortune.FortuneState;
import io.nightbeam.fortunerift.game.EventSession;
import io.nightbeam.fortunerift.game.EventTicker;
import io.nightbeam.fortunerift.game.RiftActions;
import io.nightbeam.fortunerift.game.ScriptRunner;
import io.nightbeam.fortunerift.registry.RiftIds;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class FortuneCommands {
    private FortuneCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("fortunerift")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("rift", StringArgumentType.word())
                                        .executes(ctx -> give(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), StringArgumentType.getString(ctx, "rift"), 1))
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                                                .executes(ctx -> give(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), StringArgumentType.getString(ctx, "rift"), IntegerArgumentType.getInteger(ctx, "amount")))))))
                .then(Commands.literal("event")
                        .then(Commands.argument("event", StringArgumentType.word())
                                .executes(ctx -> event(ctx.getSource(), StringArgumentType.getString(ctx, "event"), ctx.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> event(ctx.getSource(), StringArgumentType.getString(ctx, "event"), EntityArgument.getPlayer(ctx, "player"))))))
                .then(Commands.literal("random")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> random(EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("reload").executes(ctx -> {
                    FortuneConfig.load();
                    EventTicker.resetGuard();
                    ctx.getSource().sendSuccess(() -> Component.literal("Reloaded FortuneRift config"), true);
                    return 1;
                }))
                .then(Commands.literal("list").executes(ctx -> {
                    for (EventDefinition event : EventRegistry.definitions()) {
                        ctx.getSource().sendSuccess(() -> Component.literal(event.id + " " + event.rarity + (event.enabled ? "" : " disabled")), false);
                    }
                    return EventRegistry.definitions().size();
                }))
                .then(Commands.literal("fortune")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                    PlayerRiftProfile profile = ProfileStore.get(player.level().getServer(), player.getUUID());
                                    ctx.getSource().sendSuccess(() -> Component.literal(player.getScoreboardName() + " fortune " + profile.fortune), false);
                                    return profile.fortune;
                                })))
                .then(Commands.literal("setfortune")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("value", IntegerArgumentType.integer(0, 100))
                                        .executes(ctx -> {
                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                            PlayerRiftProfile profile = ProfileStore.get(player.level().getServer(), player.getUUID());
                                            profile.fortune = FortuneState.clamp(IntegerArgumentType.getInteger(ctx, "value"));
                                            ProfileStore.save(player.level().getServer(), player.getUUID());
                                            return profile.fortune;
                                        })))));
    }

    private static int give(CommandSourceStack source, ServerPlayer player, String rift, int amount) {
        String id = rift.endsWith("_rift") ? rift : rift + "_rift";
        var item = BuiltInRegistries.ITEM.get(RiftIds.id(id));
        if (item.isEmpty()) {
            source.sendFailure(Component.literal("Unknown rift " + rift));
            return 0;
        }
        RiftActions.give(player.level(), player, player.blockPosition(), new ItemStack(item.get(), amount));
        return amount;
    }

    private static int event(CommandSourceStack source, String id, ServerPlayer player) {
        EventDefinition definition = EventRegistry.byId(id);
        if (definition == null) {
            source.sendFailure(Component.literal("Unknown event " + id));
            return 0;
        }
        EventSession session = new EventSession(player.level(), player.blockPosition());
        EventTicker.track(session);
        ScriptRunner.run(definition, player.level(), player.blockPosition(), player, session);
        return 1;
    }

    private static int random(ServerPlayer player) {
        EventDefinition definition = EventRegistry.roll(
                RiftVariant.FORTUNE,
                player.level().dimension().identifier().toString(),
                new FortuneState(ProfileStore.get(player.level().getServer(), player.getUUID()).fortune),
                FortuneConfig.get(),
                new java.util.Random(player.getRandom().nextLong())
        );
        if (definition == null) {
            return 0;
        }
        return event(player.createCommandSourceStack(), definition.id, player);
    }
}
