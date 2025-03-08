package dev.overgrown.thaumaturge.item;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.utils.ItemBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;

public class ModItems {

    public static final Item LESSER_FOCI = ItemBuilder.create("lesser_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings().maxCount(1))
            .buildAndRegister();

    public static void register() {
        Thaumaturge.LOGGER.info("Registering Mod Items for " + Thaumaturge.MOD_ID);
    }
}
