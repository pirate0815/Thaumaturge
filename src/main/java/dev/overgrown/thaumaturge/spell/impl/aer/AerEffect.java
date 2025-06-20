package dev.overgrown.thaumaturge.spell.impl.aer;

import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import dev.overgrown.thaumaturge.utils.ModSounds;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Box;

public class AerEffect implements AspectEffect {
    @Override
    public void apply(SelfSpellDelivery delivery) {
        delivery.addEffect(player -> {
            // Apply upward velocity
            player.addVelocity(0, 0.9, 0);
            player.velocityModified = true;

            // Play sound and particles
            ServerWorld world = (ServerWorld) player.getWorld();
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.AER_SPELL_CAST, SoundCategory.PLAYERS, 1.0f, 1.0f);
            world.spawnParticles(ParticleTypes.GUST_EMITTER_SMALL,
                    player.getX(), player.getY(), player.getZ(), 1,
                    0.5, 0.1, 0.5, 0.1);
        });
    }

    // Existing targeted spell implementation
    @Override
    public void apply(TargetedSpellDelivery delivery) {
        delivery.addOnHitEffect(target -> {
            target.addVelocity(0, 1.5, 0);
            target.velocityModified = true;

            ServerPlayerEntity caster = delivery.getCaster();
            ServerWorld world = caster.getWorld();

            world.playSound(null, target.getX(), target.getY(), target.getZ(),
                    ModSounds.AER_SPELL_CAST, SoundCategory.PLAYERS, 1.0f, 1.0f);
            world.spawnParticles(ParticleTypes.GUST_EMITTER_SMALL,
                    target.getX(), target.getY(), target.getZ(), 5,
                    0.5, 0.1, 0.5, 0.1);
        });
    }

    // Existing AOE implementation
    @Override
    public void apply(AoeSpellDelivery delivery) {
        delivery.addEffect(pos -> {
            ServerPlayerEntity caster = delivery.getCaster();
            float radius = delivery.getRadius();

            if (pos.equals(caster.getBlockPos())) {
                ServerWorld world = delivery.getCasterWorld();
                Box area = new Box(
                        pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius,
                        pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius
                );

                world.getOtherEntities(caster, area).forEach(entity -> {
                    entity.addVelocity(0, 1.2, 0);
                    entity.velocityModified = true;

                    world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                            ModSounds.AER_SPELL_CAST, SoundCategory.PLAYERS, 1.0f, 1.0f);
                    world.spawnParticles(ParticleTypes.GUST_EMITTER_SMALL,
                            entity.getX(), entity.getY(), entity.getZ(), 3,
                            0.3, 0.1, 0.3, 0.1);
                });
            }
        });
    }
}