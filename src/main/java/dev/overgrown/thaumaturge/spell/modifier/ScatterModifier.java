package dev.overgrown.thaumaturge.spell.modifier;

import dev.overgrown.thaumaturge.spell.pattern.ModifierEffect;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;

public class ScatterModifier implements ModifierEffect {
    @Override
    public void apply(TargetedSpellDelivery delivery) {
        delivery.setProjectileCount(3);
        delivery.setSpread(15.0f);
    }
}
