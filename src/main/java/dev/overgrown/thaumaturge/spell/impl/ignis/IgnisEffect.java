package dev.overgrown.thaumaturge.spell.impl.ignis;

import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;

public class IgnisEffect implements AspectEffect {
    @Override
    public void apply(TargetedSpellDelivery delivery) {
        int baseTicks = 100;
        int duration = (int) (baseTicks * delivery.getPowerMultiplier());
        delivery.addOnHitEffect(entity -> entity.setFireTicks(duration));
    }
}