package dev.overgrown.thaumaturge.spell.impl.vitium;

import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import net.minecraft.server.world.ServerWorld;

public class VitiumEffect implements AspectEffect {
    private static final float BASE_DAMAGE = 5.0f;

    @Override
    public void apply(TargetedSpellDelivery delivery) {
        float damage = BASE_DAMAGE * delivery.getPowerMultiplier();
        delivery.addOnHitEffect(entity -> {
            if (delivery.getCaster() != null) {
                // Cast the world to ServerWorld to match the method signature
                entity.damage(
                        (ServerWorld) delivery.getCaster().getWorld(), // Corrected to ServerWorld
                        delivery.getCaster().getDamageSources().magic(),
                        damage
                );
            }
        });
    }
}