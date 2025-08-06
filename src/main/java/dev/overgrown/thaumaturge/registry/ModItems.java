package dev.overgrown.thaumaturge.registry;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.item.AethericGogglesItem;
import dev.overgrown.thaumaturge.item.AspectLensItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;

public class ModItems {
    public static final Item ASPECT_LENS = registerItem("aspect_lens",
            new AspectLensItem(new FabricItemSettings()
                    .maxCount(1)
            )
    );

    public static final Item AETHERIC_GOGGLES = registerItem("aetheric_goggles",
            new AethericGogglesItem()
    );

    public static final Item LESSER_FOCUS = registerItem("lesser_focus",
            new Item(new FabricItemSettings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
    );

    public static final Item ADVANCED_FOCUS = registerItem("advanced_focus",
            new Item(new FabricItemSettings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
    );

    public static final Item GREATER_FOCUS = registerItem("greater_focus",
            new Item(new FabricItemSettings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
    );

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Thaumaturge.identifier(name), item);
    }

    public static void initialize() {
        // Initialization handled by static field loading
    }
}