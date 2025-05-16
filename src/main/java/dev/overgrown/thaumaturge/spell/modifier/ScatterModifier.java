package dev.overgrown.thaumaturge.spell.modifier;

import dev.overgrown.thaumaturge.networking.SpellCastPacket;
import dev.overgrown.thaumaturge.spell.pattern.ModifierEffect;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;

public class ScatterModifier implements ModifierEffect {

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
            }
            case GREATER -> {
                delivery.setProjectileCount(4);
                delivery.setSpread(20.0f);
            }
        }
    }
}