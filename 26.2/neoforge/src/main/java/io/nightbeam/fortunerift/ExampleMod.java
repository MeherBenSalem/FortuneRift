package io.nightbeam.fortunerift;

import io.nightbeam.fortunerift.command.FortuneCommands;
import io.nightbeam.fortunerift.event.RiftVariant;
import io.nightbeam.fortunerift.game.EventTicker;
import io.nightbeam.fortunerift.registry.RiftContent;
import io.nightbeam.fortunerift.registry.RiftIds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Constants.MOD_ID)
public class ExampleMod {
    private static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, Constants.MOD_ID);
    private static final DeferredRegister<net.minecraft.world.item.Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, Constants.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

    public ExampleMod(IEventBus eventBus) {
        FortuneRift.init();
        for (RiftVariant variant : RiftVariant.values()) {
            DeferredHolder<Block, Block> block = BLOCKS.register(variant.id(), () -> RiftContent.createBlock(variant));
            ITEMS.register(variant.id(), () -> RiftContent.createBlockItem(block.get(), variant.id()));
        }
        ITEMS.register("rift_journal", RiftContent::journal);
        ITEMS.register("rift_fragment", RiftContent::fragment);
        ITEMS.register("rift_heart", RiftContent::heart);
        TABS.register("main", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.fortunerift"))
                .icon(() -> new ItemStack(BuiltInRegistries.ITEM.getValue(RiftIds.id("fortune_rift"))))
                .displayItems((params, output) -> BuiltInRegistries.ITEM.entrySet().stream()
                        .filter(entry -> entry.getKey().identifier().getNamespace().equals(Constants.MOD_ID))
                        .forEach(entry -> output.accept(entry.getValue())))
                .build());
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        TABS.register(eventBus);
        NeoForge.EVENT_BUS.addListener(ExampleMod::commands);
        NeoForge.EVENT_BUS.addListener(ExampleMod::tick);
    }

    private static void commands(RegisterCommandsEvent event) {
        FortuneCommands.register(event.getDispatcher());
    }

    private static void tick(ServerTickEvent.Post event) {
        EventTicker.tick(event.getServer());
    }
}