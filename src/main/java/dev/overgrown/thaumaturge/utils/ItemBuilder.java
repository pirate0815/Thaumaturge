package dev.overgrown.thaumaturge.utils;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
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

public class ItemBuilder<T extends Item> {
    private final Identifier id;
    private final List<RegistryKey<ItemGroup>> itemGroups = new LinkedList<>();
    private Item.Settings itemSettings = new Item.Settings();

    public static <T extends Item> ItemBuilder<T> create(String name) {
        return new ItemBuilder<>(name);
    }

    ItemBuilder(String name) {
        this.id = Thaumaturge.identifier(name); // Change this to make it work with your MODID
    }

    @SafeVarargs
    public final ItemBuilder<T> setItemGroup(RegistryKey<ItemGroup>... itemGroup) {
        // Add the items to the list of item groups
        this.itemGroups.addAll(Arrays.stream(itemGroup).toList());
        return this;
    }

    public ItemBuilder<T> withSettings(Function<Item.Settings,Item.Settings> settingsFunction) {
        // modify the current item settings to include the parameters provided by the function
        this.itemSettings = settingsFunction.apply(this.itemSettings);
        return this;
    }

    public ItemBuilder<T> withSettings(Item.Settings settings) {
        // Set the item settings to this
        this.itemSettings = settings;
        return this;
    }

    public Item buildAndRegister() {
        // Create a new Default Item
        return this.buildAndRegister(Item::new);
    }

    public <T extends Item> T buildAndRegister(@NotNull Function<Item.Settings, T> itemFunction) {
        // Create Registry Key
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, this.id);

        // Create item from settings
        T item = itemFunction.apply(this.itemSettings.registryKey(itemKey));

        // Register the item
        Registry.register(Registries.ITEM, itemKey, item);

        // Register the item in all specified item groups
        for (RegistryKey<ItemGroup> itemGroup : this.itemGroups) {
            ItemGroupEvents.modifyEntriesEvent(itemGroup).register((fabricItemGroupEntries -> fabricItemGroupEntries.add(item)));
        }

        // Return the Item
        return item;
    }
}