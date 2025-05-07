package dev.overgrown.thaumaturge.spell.pattern;

import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;

public interface ModifierEffect {
    default void apply(SelfSpellDelivery delivery) {}
    default void apply(TargetedSpellDelivery delivery) {}
    default void apply(AoeSpellDelivery delivery) {}
}