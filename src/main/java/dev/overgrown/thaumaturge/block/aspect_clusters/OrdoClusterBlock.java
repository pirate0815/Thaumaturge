package dev.overgrown.thaumaturge.block.aspect_clusters;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class OrdoClusterBlock extends AspectClusterBlock {
    public OrdoClusterBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected DefaultParticleType getParticleType() {
        return ParticleTypes.ENCHANT;
    }

    @Override
    protected void triggerBreakEffect(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        // Repair nearby tools and armor
        world.getOtherEntities(null, new Box(pos.add(-10, -10, -10), pos.add(10, 10, 10))).forEach(entity -> {
            if (entity.squaredDistanceTo(Vec3d.ofCenter(pos)) <= 36.0) { // 6 block radius
                if (entity instanceof net.minecraft.entity.LivingEntity livingEntity) {
                    livingEntity.getArmorItems().forEach(stack -> {
                        if (stack.isDamaged() && world.random.nextFloat() < 0.5f) {
                            stack.setDamage(stack.getDamage() - 1);
                        }
                    });

                    if (livingEntity.getMainHandStack().isDamaged() && world.random.nextFloat() < 0.5f) {
                        livingEntity.getMainHandStack().setDamage(livingEntity.getMainHandStack().getDamage() - 1);
                    }
                }
            }
        });
    }
}