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

/**
 * ItemBuilder is a generic builder class designed to simplify the creation and registration
 * of custom items in the Thaumaturge mod. It encapsulates the configuration of item settings,
 * item group assignments, and the registration of items with the Minecraft registry.
 *
 * <p>
 * Usage Example:
 * <pre>
 *     Item myItem = ItemBuilder.create("my_item")
 *                   .setItemGroup(ItemGroups.TOOLS)
 *                   .withSettings(settings -> settings.maxCount(1).rarity(Rarity.RARE))
 *                   .buildAndRegister();
 * </pre>
 * </p>
 *
 * @param <T> The type of the Item being built.
 */
public class ItemBuilder<T extends Item> {
    // Unique identifier for the item based on the mod's namespace.
    private final Identifier id;

    // Holds all the creative inventory groups where the item will be displayed.
    private final List<RegistryKey<ItemGroup>> itemGroups = new LinkedList<>();

    // The configuration settings for this item, such as stack size, rarity, etc.
    private Item.Settings itemSettings = new Item.Settings();

    /**
     * Static factory method to instantiate an ItemBuilder.
     *
     * @param name The unique name for the item.
     * @param <T>  The type of item.
     * @return A new instance of ItemBuilder for the given item name.
     */
    public static <T extends Item> ItemBuilder<T> create(String name) {
        return new ItemBuilder<>(name);
    }

    /**
     * Constructor initializes the builder with a namespaced Identifier.
     *
     * @param name The unique name that will be combined with the mod id to create an Identifier.
     */
    ItemBuilder(String name) {
        // Generate a namespaced ID using the mod's identifier helper.
        this.id = Thaumaturge.identifier(name); // Change this to make it work with your MODID
    }

    /**
     * Assigns one or more creative item groups to the item.
     * This determines where the item appears in the creative mode inventory.
     *
     * @param itemGroup Varargs of item group keys.
     * @return The current ItemBuilder instance for chaining.
     */
    @SafeVarargs
    public final ItemBuilder<T> setItemGroup(RegistryKey<ItemGroup>... itemGroup) {
        // Convert the varargs into a list and add them to our itemGroups.
        this.itemGroups.addAll(Arrays.stream(itemGroup).toList());
        return this;
    }

    /**
     * Modifies the item settings using a settings function.
     * This allows for fluent customization of the item’s properties.
     *
     * @param settingsFunction A lambda/function that modifies the current settings.
     * @return The current ItemBuilder instance for chaining.
     */
    public ItemBuilder<T> withSettings(Function<Item.Settings, Item.Settings> settingsFunction) {
        // Apply the provided function to update the item settings.
        this.itemSettings = settingsFunction.apply(this.itemSettings);
        return this;
    }

    /**
     * Directly sets the item settings.
     * This can be used if you already have a configured Item.Settings object.
     *
     * @param settings The settings to assign.
     * @return The current ItemBuilder instance for chaining.
     */
    public ItemBuilder<T> withSettings(Item.Settings settings) {
        // Replace the current settings with the given settings.
        this.itemSettings = settings;
        return this;
    }

    /**
     * Builds a default Item using the basic Item constructor and registers it.
     * This method is a shortcut when no custom item behavior is required.
     *
     * @return The registered Item instance.
     */
    public Item buildAndRegister() {
        // Delegate to the overloaded version using the default constructor.
        return this.buildAndRegister(Item::new);
    }

    /**
     * Builds an item using a provided item creation function and registers it.
     * The process involves generating a registry key, applying the item settings, registering
     * the item with Minecraft’s item registry, and then associating the item with the proper creative groups.
     *
     * @param itemFunction A function that takes Item.Settings and returns a new item instance.
     * @param <R>          The type of the item.
     * @return The newly created and registered item.
     */
    public <R extends Item> R buildAndRegister(@NotNull Function<Item.Settings, R> itemFunction) {
        // Create a registry key for the item using the mod-specific identifier.
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, this.id);

        // Create a new item instance using the provided itemFunction, binding the registry key to the settings.
        R item = itemFunction.apply(this.itemSettings.registryKey(itemKey));

        // Register the item in Minecraft's global registry.
        Registry.register(Registries.ITEM, itemKey, item);

        // Register the item within each specified creative item group using Fabric's event system.
        for (RegistryKey<ItemGroup> itemGroup : this.itemGroups) {
            ItemGroupEvents.modifyEntriesEvent(itemGroup)
                    .register((fabricItemGroupEntries -> fabricItemGroupEntries.add(item)));
        }

        // Return the constructed, registered item.
        return item;
    }
}