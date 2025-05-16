package dev.overgrown.thaumaturge.spell.modifier;

import dev.overgrown.thaumaturge.spell.pattern.ModifierEffect;
import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;

public class PowerModifier implements ModifierEffect {

    @Override
    public void apply(SelfSpellDelivery delivery) {
        delivery.setPowerMultiplier(delivery.getPowerMultiplier() + 1.0f);
    }

    @Override
    public void apply(TargetedSpellDelivery delivery) {
        delivery.setPowerMultiplier(delivery.getPowerMultiplier() + 1.0f);
    }

    @Override
    public void apply(AoeSpellDelivery delivery) {
        delivery.setPowerMultiplier(delivery.getPowerMultiplier() + 1.0f);
    }
}