package dev.overgrown.thaumaturge.spell.impl.gelum;

import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import dev.overgrown.thaumaturge.utils.ModSounds;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;

public class GelumEffect implements AspectEffect {

    @Override
    public void apply(TargetedSpellDelivery delivery) {
        delivery.addOnHitEffect(entity -> {
            if (entity instanceof LivingEntity living) {
                float damage = 4.0f * delivery.getPowerMultiplier();
                ServerWorld world = (ServerWorld) delivery.getCaster().getWorld();
                Entity projectile = delivery.getCaster();
                LivingEntity caster = delivery.getCaster();

                living.damage(world, living.getDamageSources().mobProjectile(projectile, caster), damage);

                int duration = (int) (600 * delivery.getPowerMultiplier());
                living.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SLOWNESS,
                        duration,
                        1
                ));

                // Play sound when the spell hits an entity
                world.playSound(null, living.getX(), living.getY(), living.getZ(),
                        ModSounds.GELUM_SPELL_CAST, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
        });

        delivery.addBlockHitEffect(blockHitResult -> {
            ServerWorld world = (ServerWorld) delivery.getCaster().getWorld();
            BlockPos center = blockHitResult.getBlockPos();
            int radius = 1;

            BlockPos.iterate(center.add(-radius, -radius, -radius), center.add(radius, radius, radius))
                    .forEach(pos -> {
                        if (world.getBlockState(pos).isOf(Blocks.WATER)) {
                            world.setBlockState(pos, Blocks.FROSTED_ICE.getDefaultState());
                            // Play sound when water freezes
                            world.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                                    ModSounds.GELUM_SPELL_CAST, SoundCategory.PLAYERS, 0.5f, 1.0f);
                        }
                    });
        });
    }
}