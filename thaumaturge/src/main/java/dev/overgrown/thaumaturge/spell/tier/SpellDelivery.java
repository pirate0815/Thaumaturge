package dev.overgrown.thaumaturge.spell.tier;

import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;

import java.util.List;

/**
 * Marker interface for spell deliveries
 */
public interface SpellDelivery {
    void setModifiers(List<ModifierEffect> mods);
}