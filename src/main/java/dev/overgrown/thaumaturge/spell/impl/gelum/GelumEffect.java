package dev.overgrown.thaumaturge.spell.impl.gelum;

import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import dev.overgrown.thaumaturge.utils.ModSounds;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;

public class GelumEffect implements AspectEffect {

    @Override
    public void apply(TargetedSpellDelivery delivery) {
        // Entity effect: damage and slowness
        delivery.addOnHitEffect(entity -> {
            if (entity instanceof LivingEntity living) {
                handleEntityHit(delivery, living);
            }
        });

        // Block effect: freeze water in a 3x3x3 area
        delivery.addBlockHitEffect(blockHitResult -> {
            // Use the block position from the hit result
            handleBlockEffect(delivery, blockHitResult.getBlockPos());
        });
    }

    private void handleEntityHit(TargetedSpellDelivery delivery, LivingEntity entity) {
        float damage = 4.0f * delivery.getPowerMultiplier();
        ServerWorld world = delivery.getCaster().getWorld();
        LivingEntity caster = delivery.getCaster();

        entity.damage(world, entity.getDamageSources().mobProjectile(null, caster), damage);

        int duration = (int) (600 * delivery.getPowerMultiplier());
        entity.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SLOWNESS,
                duration,
                1
        ));

        // Play sound when the spell hits an entity
        world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                ModSounds.GELUM_SPELL_CAST, SoundCategory.PLAYERS, 1.0f, 1.0f);
    }

    private void handleBlockEffect(TargetedSpellDelivery delivery, BlockPos center) {
        ServerWorld world = delivery.getCaster().getWorld();
        int radius = 1; // 3x3x3 area

        BlockPos.iterate(center.add(-radius, -radius, -radius), center.add(radius, radius, radius))
                .forEach(pos -> {
                    if (world.getBlockState(pos).isOf(Blocks.WATER)) {
                        world.setBlockState(pos, Blocks.FROSTED_ICE.getDefaultState());
                        // Play sound when water freezes
                        world.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                                ModSounds.GELUM_SPELL_CAST, SoundCategory.PLAYERS, 0.5f, 1.0f);
                    }
                });
    }
}