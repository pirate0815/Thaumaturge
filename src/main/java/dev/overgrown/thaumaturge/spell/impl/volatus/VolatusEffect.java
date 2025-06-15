package dev.overgrown.thaumaturge.spell.impl.volatus;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

public class VolatusEffect implements AspectEffect {
    @Override
    public void apply(SelfSpellDelivery delivery) {
        delivery.addEffect(entity -> {
            if (entity instanceof LivingEntity) {
                ((LivingEntity) entity).addStatusEffect(new StatusEffectInstance(
                        Thaumaturge.VOLATUS_FLIGHT_EFFECT,
                        20 * 60, // 1 minute
                        0,
                        false,
                        true,
                        true
                ));
            }
        });
    }

    @Override
    public void apply(TargetedSpellDelivery delivery) {
        delivery.addOnHitEffect(target -> {
            if (target instanceof LivingEntity) {
                ((LivingEntity) target).addStatusEffect(new StatusEffectInstance(
                        Thaumaturge.VOLATUS_FLIGHT_EFFECT,
                        20 * 60,
                        0,
                        false,
                        true,
                        true
                ));
            }
        });
    }
}