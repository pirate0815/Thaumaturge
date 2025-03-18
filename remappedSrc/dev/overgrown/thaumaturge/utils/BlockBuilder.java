package dev.overgrown.thaumaturge.utils;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class BlockBuilder<T extends Block> {
    private final Identifier id;
    private final List<RegistryKey<ItemGroup>> itemGroups = new LinkedList<>();
    private Block.Settings blockSettings = Block.Settings.create();
    private Item.Settings itemSettings = new Item.Settings();

    public static <T extends Block> BlockBuilder<T> create(String name) {
        return new BlockBuilder<>(name);
    }

    private BlockBuilder(String name) {
        this.id = Thaumaturge.identifier(name);
    }

    @SafeVarargs
    public final BlockBuilder<T> setItemGroup(RegistryKey<ItemGroup>... itemGroups) {
        this.itemGroups.addAll(Arrays.asList(itemGroups));
        return this;
    }

    public BlockBuilder<T> withBlockSettings(Function<Block.Settings, Block.Settings> settingsFunction) {
        this.blockSettings = settingsFunction.apply(this.blockSettings);
        return this;
    }

    public BlockBuilder<T> withItemSettings(Function<Item.Settings, Item.Settings> settingsFunction) {
        this.itemSettings = settingsFunction.apply(this.itemSettings);
        return this;
    }

    public T buildAndRegister(@NotNull Function<Block.Settings, T> blockFunction) {
        // Create RegistryKey for the block
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, this.id);

        // Apply registry key to block settings
        this.blockSettings = this.blockSettings.registryKey(blockKey);

        // Create and register the block
        T block = blockFunction.apply(this.blockSettings);
        Registry.register(Registries.BLOCK, blockKey, block);

        // Create RegistryKey for the item
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, this.id);

        // Apply registry key to item settings
        this.itemSettings = this.itemSettings.registryKey(itemKey);

        // Create and register the BlockItem
        BlockItem blockItem = new BlockItem(block, this.itemSettings);
        Registry.register(Registries.ITEM, itemKey, blockItem);

        // Add the BlockItem to specified item groups
        for (RegistryKey<ItemGroup> group : this.itemGroups) {
            ItemGroupEvents.modifyEntriesEvent(group).register(entries -> entries.add(blockItem));
        }

        return block;
    }
}