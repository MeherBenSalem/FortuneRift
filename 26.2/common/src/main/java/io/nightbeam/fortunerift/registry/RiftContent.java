package io.nightbeam.fortunerift.registry;

import io.nightbeam.fortunerift.block.FortuneRiftBlock;
import io.nightbeam.fortunerift.event.RiftVariant;
import io.nightbeam.fortunerift.item.RiftFragmentItem;
import io.nightbeam.fortunerift.item.RiftHeartItem;
import io.nightbeam.fortunerift.item.RiftJournalItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.function.BiConsumer;

public final class RiftContent {
    private RiftContent() {
    }

    public static void register(BiConsumer<String, Block> blocks, BiConsumer<String, Item> items) {
        for (RiftVariant variant : RiftVariant.values()) {
            ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, RiftIds.id(variant.id()));
            Block block = new FortuneRiftBlock(properties().setId(blockKey), variant);
            blocks.accept(variant.id(), block);
            ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, RiftIds.id(variant.id()));
            items.accept(variant.id(), new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));
        }
        items.accept("rift_journal", new RiftJournalItem(new Item.Properties().setId(itemKey("rift_journal")).stacksTo(1)));
        items.accept("rift_fragment", new RiftFragmentItem(new Item.Properties().setId(itemKey("rift_fragment")).stacksTo(64)));
        items.accept("rift_heart", new RiftHeartItem(new Item.Properties().setId(itemKey("rift_heart")).stacksTo(1)));
    }

    public static Block createBlock(RiftVariant variant) {
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, RiftIds.id(variant.id()));
        return new FortuneRiftBlock(properties().setId(blockKey), variant);
    }

    public static Item createBlockItem(Block block, String path) {
        return new BlockItem(block, new Item.Properties().setId(itemKey(path)).useBlockDescriptionPrefix());
    }

    public static Item journal() {
        return new RiftJournalItem(new Item.Properties().setId(itemKey("rift_journal")).stacksTo(1));
    }

    public static Item fragment() {
        return new RiftFragmentItem(new Item.Properties().setId(itemKey("rift_fragment")).stacksTo(64));
    }

    public static Item heart() {
        return new RiftHeartItem(new Item.Properties().setId(itemKey("rift_heart")).stacksTo(1));
    }

    private static ResourceKey<Item> itemKey(String path) {
        return ResourceKey.create(Registries.ITEM, RiftIds.id(path));
    }

    private static BlockBehaviour.Properties properties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BLACK)
                .strength(8.0F, 1200.0F)
                .sound(SoundType.AMETHYST)
                .lightLevel(state -> 7)
                .requiresCorrectToolForDrops();
    }
}
