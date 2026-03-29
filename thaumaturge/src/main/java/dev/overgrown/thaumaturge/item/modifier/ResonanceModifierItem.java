package dev.overgrown.thaumaturge.item.modifier;

import net.minecraft.item.Item;

public abstract class ResonanceModifierItem extends Item {
    public ResonanceModifierItem(Settings settings) {
        super(settings);
    }

    public abstract String getModifierType();
}