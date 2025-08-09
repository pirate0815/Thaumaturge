package dev.overgrown.thaumaturge.spell.modifier;

import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;

/**
 * Contract for spell modifiers. Modifiers can tweak or augment an aspect's behavior.
 * All hooks are optional (default no-op); implement whichever ones you need.
 */
public interface ModifierEffect {
    /** Hook for self-cast spells. */
    default void onSelf(SelfSpellDelivery delivery) {}

    /** Hook for targeted spells (entity or block). */
    default void onTargeted(TargetedSpellDelivery delivery) {}

    /** Hook for AOE spells. */
    default void onAoe(AoeSpellDelivery delivery) {}
}
