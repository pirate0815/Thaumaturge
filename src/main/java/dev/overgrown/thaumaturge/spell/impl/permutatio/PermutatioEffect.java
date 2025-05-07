package dev.overgrown.thaumaturge.spell.impl.permutatio;

import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Consumer;

public class PermutatioEffect implements AspectEffect {

    @Override
    public void apply(TargetedSpellDelivery delivery) {
        // Create an anonymous Consumer instance
        Consumer<Entity> permutatioConsumer = new Consumer<>() {
            @Override
            public void accept(Entity originalTarget) {
                ServerPlayerEntity caster = delivery.getCaster();

                // Apply all previous effects to the caster instead, excluding this one
                delivery.getOnHitEffects().forEach(effect -> {
                    if (effect != this) { // Compare with 'this' instance
                        effect.accept(caster);
                    }
                });
            }
        };

        // Add the Consumer to the delivery
        delivery.addOnHitEffect(permutatioConsumer);
    }
}