package dev.overgrown.thaumaturge.block.aspect_clusters;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PerditioClusterBlock extends AspectClusterBlock {
    public PerditioClusterBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected DefaultParticleType getParticleType() {
        return ParticleTypes.SMOKE;
    }

    @Override
    protected void triggerBreakEffect(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        // Damage nearby tools and armor, and cause random block decay
        world.getOtherEntities(null, new Box(pos.add(-10, -10, -10), pos.add(10, 10, 10))).forEach(entity -> {
            if (entity.squaredDistanceTo(Vec3d.ofCenter(pos)) <= 36.0) { // 6 block radius
                if (entity instanceof net.minecraft.entity.LivingEntity livingEntity) {
                    livingEntity.getArmorItems().forEach(stack -> {
                        if (!stack.isDamaged() && world.random.nextFloat() < 0.3f) {
                            stack.setDamage(1);
                        } else if (stack.isDamaged() && world.random.nextFloat() < 0.5f) {
                            stack.setDamage(stack.getDamage() + 1);
                        }
                    });

                    if (world.random.nextFloat() < 0.4f) {
                        livingEntity.getMainHandStack().damage(1, livingEntity, e -> {});
                    }
                }
            }
        });

        // Randomly damage blocks
        BlockPos.iterateOutwards(pos, 4, 4, 4).forEach(blockPos -> {
            if (world.random.nextFloat() < 0.1f &&
                    !world.getBlockState(blockPos).isAir() &&
                    world.getBlockState(blockPos).getHardness(world, blockPos) >= 0) {
                world.breakBlock(blockPos, true);
            }
        });
    }
}