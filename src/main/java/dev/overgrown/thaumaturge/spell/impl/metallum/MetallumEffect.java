package dev.overgrown.thaumaturge.spell.impl.metallum;

import dev.overgrown.thaumaturge.entity.ModEntities;
import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import dev.overgrown.thaumaturge.spell.impl.metallum.entity.MetalShardEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class MetallumEffect implements AspectEffect {

    @Override
    public void apply(TargetedSpellDelivery delivery) {
        delivery.addOnHitEffect(target -> {
            ServerWorld world = delivery.getWorld();
            ServerPlayerEntity caster = delivery.getCaster();

            if (world != null && caster != null) {
                spawnShards(world, caster, delivery.getPowerMultiplier(), delivery.getScatterSize());
            }
        });
    }

    private void spawnShards(ServerWorld world, ServerPlayerEntity caster,
                             float powerMultiplier, int scatterSize) {
        int shardCount = scatterSize > 0 ? 3 : 1; // Scatter modifier creates 3 shards
        float damage = 5.0f * powerMultiplier; // Base damage multiplied by power modifier

        for (int i = 0; i < shardCount; i++) {
            MetalShardEntity shard = new MetalShardEntity(ModEntities.METAL_SHARD, world);
            shard.setPosition(caster.getEyePos());
            shard.setDamage(damage);
            shard.setPunch(1); // Knockback level
            shard.setLifetime(100); // 5 seconds at 20tps

            // Apply scatter spread if needed
            Vec3d velocity = calculateVelocity(caster, i, shardCount);
            shard.setVelocity(velocity);

            world.spawnEntity(shard);
        }

        // Play sound
        world.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS,
                1.0f, 1.2f);
    }

    private Vec3d calculateVelocity(ServerPlayerEntity caster, int index, int total) {
        Vec3d baseDirection = caster.getRotationVector();

        if (total > 1) {
            // Apply spread for scatter effect
            float spread = 15.0f * (index - (total - 1) / 2.0f); // Degrees
            Vec3d spreadVec = applySpread(baseDirection, spread, caster.getRandom());
            return spreadVec.multiply(1.5); // Base speed
        }

        return baseDirection.multiply(1.5);
    }

    private Vec3d applySpread(Vec3d direction, float spreadDegrees, Random random) {
        float spreadRadians = spreadDegrees * (float) (Math.PI / 180);
        float randomPitch = (random.nextFloat() - 0.5f) * spreadRadians;
        float randomYaw = (random.nextFloat() - 0.5f) * spreadRadians;

        return direction
                .rotateX(randomPitch)
                .rotateY(randomYaw);
    }
}