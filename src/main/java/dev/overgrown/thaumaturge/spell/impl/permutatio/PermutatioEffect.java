package dev.overgrown.thaumaturge.spell.impl.permutatio;

import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;

public class PermutatioEffect implements AspectEffect {

    @Override
    public void apply(SelfSpellDelivery delivery) {
        delivery.setRedirectToTarget(true);
    }

    @Override
    public void apply(TargetedSpellDelivery delivery) {
        delivery.setSwapActorTarget(true);
    }
}