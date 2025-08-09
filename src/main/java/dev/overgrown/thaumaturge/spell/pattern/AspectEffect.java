package dev.overgrown.thaumaturge.spell.pattern;

import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;

/**
 * Core contract for an Aspect's behavior. Implementations (e.g., Ignis) decide
 * how to react for each delivery type.
 */
public interface AspectEffect {
    /** Cast affecting only the caster. */
    void applySelf(SelfSpellDelivery delivery);

    /** Cast targeting either an entity or a block face. */
    void applyTargeted(TargetedSpellDelivery delivery);

    /** Cast affecting an area around a center point. */
    void applyAoe(AoeSpellDelivery delivery);
}
