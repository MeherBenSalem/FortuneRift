package io.nightbeam.fortunerift;

import io.nightbeam.fortunerift.command.FortuneCommands;
import io.nightbeam.fortunerift.game.EventTicker;
import io.nightbeam.fortunerift.registry.RiftContent;
import io.nightbeam.fortunerift.registry.RiftIds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ExampleMod implements ModInitializer {

    @Override
    public void onInitialize() {
        FortuneRift.init();
        RiftContent.register(
                (name, block) -> Registry.register(BuiltInRegistries.BLOCK, RiftIds.id(name), block),
                (name, item) -> Registry.register(BuiltInRegistries.ITEM, RiftIds.id(name), item)
        );
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, RiftIds.id("main"), CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.fortunerift"))
                .icon(() -> new ItemStack(BuiltInRegistries.ITEM.getValue(RiftIds.id("fortune_rift"))))
                .displayItems((params, output) -> BuiltInRegistries.ITEM.entrySet().stream()
                        .filter(entry -> entry.getKey().identifier().getNamespace().equals(Constants.MOD_ID))
                        .forEach(entry -> output.accept(entry.getValue())))
                .build());
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) -> FortuneCommands.register(dispatcher));
        ServerTickEvents.END_SERVER_TICK.register(EventTicker::tick);
    }
}
