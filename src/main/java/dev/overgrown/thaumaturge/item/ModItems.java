package dev.overgrown.thaumaturge.item;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item LESSER_FOCI = registerItem(new Item(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Thaumaturge.MOD_ID,"lesser_foci")))));

    private static Item registerItem(Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(Thaumaturge.MOD_ID, "lesser_foci"), item);
    }

    public static void registerModItems() {
        Thaumaturge.LOGGER.info("Registering Mod Items for " + Thaumaturge.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register((itemGroup) -> itemGroup.add(ModItems.LESSER_FOCI));
    }
}
