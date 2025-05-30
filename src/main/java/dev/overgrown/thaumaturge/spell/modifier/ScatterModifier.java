package dev.overgrown.thaumaturge.spell.modifier;

import dev.overgrown.thaumaturge.networking.SpellCastPacket;
import dev.overgrown.thaumaturge.spell.pattern.ModifierEffect;
import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;

public class ScatterModifier implements ModifierEffect {

    @Override
    public void apply(SelfSpellDelivery delivery) {
        delivery.setScatterSize(1);
    }

    @Override
    public void apply(TargetedSpellDelivery delivery) {
        SpellCastPacket.SpellTier tier = delivery.getTier();
        switch (tier) {
            case LESSER -> {
                delivery.setProjectileCount(2);
                delivery.setSpread(10.0f);
            }
            case ADVANCED -> {
                delivery.setProjectileCount(3);
                delivery.setSpread(15.0f);
                delivery.setScatterSize(1);
            }
            case GREATER -> {
                delivery.setProjectileCount(4);
                delivery.setSpread(20.0f);
            }
        }
    }

    @Override
    public void apply(AoeSpellDelivery delivery) {
        delivery.setScatterSize(1);
    }
}