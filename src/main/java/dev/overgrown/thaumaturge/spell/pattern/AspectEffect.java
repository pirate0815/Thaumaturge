package dev.overgrown.thaumaturge.spell.pattern;

import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;

/**
 * Backport-faithful contract for aspect effects.
 * Matches original logic: aspects operate via delivery objects.
 */
public interface AspectEffect {
    default void applySelf(SelfSpellDelivery delivery) {}
    default void applyTargeted(TargetedSpellDelivery delivery) {}
    default void applyAoe(AoeSpellDelivery delivery) {}
}
