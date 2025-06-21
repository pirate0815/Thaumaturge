package dev.overgrown.thaumaturge.spell.impl.victus;

import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class VictusEffect implements AspectEffect {

    @Override
    public void apply(SelfSpellDelivery delivery) {
        delivery.addEffect(entity -> {
            if (entity instanceof LivingEntity livingEntity) {
                ServerWorld world = (ServerWorld) livingEntity.getWorld();
                handleHealingOrDamage(livingEntity, delivery.getPowerMultiplier(), world);
            }
        });
    }

    @Override
    public void apply(TargetedSpellDelivery delivery) {
        delivery.addOnHitEffect(entity -> {
            if (entity instanceof LivingEntity livingEntity) {
                ServerWorld world = (ServerWorld) livingEntity.getWorld();
                handleHealingOrDamage(livingEntity, delivery.getPowerMultiplier(), world);
            }
        });
    }

    @Override
    public void apply(AoeSpellDelivery delivery) {
        delivery.addEffect(blockPos -> {
            ServerWorld world = delivery.getCasterWorld();
            if (world == null) return;

            Box box = new Box(blockPos);
            world.getEntitiesByClass(LivingEntity.class, box, e -> true).forEach(entity ->
                    handleHealingOrDamage(entity, delivery.getPowerMultiplier(), world)
            );
        });
    }

    private void handleHealingOrDamage(LivingEntity entity, float powerMultiplier, ServerWorld world) {
        float amount;

        if (powerMultiplier > 1.0f) {
            // Random between 3-5 hearts (6-10 HP)
            amount = (world.random.nextInt(3) + 3) * 2;
        } else {
            // Default 2 hearts (4 HP)
            amount = 4.0f;
        }

        if (entity.hasInvertedHealingAndHarm()) {
            entity.damage(world, world.getDamageSources().magic(), amount);
        } else {
            entity.heal(amount);
            // Spawn heart particles around the entity
            spawnHeartParticles(entity, world);
        }
    }

    private void spawnHeartParticles(LivingEntity entity, ServerWorld world) {
        Vec3d pos = entity.getPos();
        for (int i = 0; i < 5; i++) {
            double offsetX = world.random.nextGaussian() * 0.5;
            double offsetY = world.random.nextDouble() * 2;
            double offsetZ = world.random.nextGaussian() * 0.5;
            world.spawnParticles(
                    ParticleTypes.HEART,
                    pos.x + offsetX,
                    pos.y + entity.getHeight() / 2 + offsetY,
                    pos.z + offsetZ,
                    1,
                    0, 0, 0,
                    0.1
            );
        }
    }
}