package dev.overgrown.thaumaturge.spell.impl.potentia;

import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;

public class PotentiaEffect implements AspectEffect {
    @Override
    public void apply(TargetedSpellDelivery delivery) {
        delivery.setMaxDistance(16.0);
    }
}