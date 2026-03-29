package dev.overgrown.thaumaturge.block.aspect_clusters;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AquaClusterBlock extends AspectClusterBlock {
    public AquaClusterBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected DefaultParticleType getParticleType() {
        return ParticleTypes.DRIPPING_WATER;
    }

    @Override
    protected void triggerBreakEffect(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        // Extinguish fires and create water in nearby areas
        BlockPos.iterateOutwards(pos, 4, 4, 4).forEach(blockPos -> {
            if (world.getBlockState(blockPos).isOf(net.minecraft.block.Blocks.FIRE)) {
                world.removeBlock(blockPos, false);
            } else if (world.getBlockState(blockPos).isAir() && world.random.nextFloat() < 0.3f) {
                world.setBlockState(blockPos, net.minecraft.block.Blocks.WATER.getDefaultState());
            }
        });

        // Extinguish entities
        world.getOtherEntities(null, new Box(pos.add(-10, -10, -10), pos.add(10, 10, 10))).forEach(entity -> {
            if (entity.squaredDistanceTo(Vec3d.ofCenter(pos)) <= 25.0 && entity.isOnFire()) {
                entity.extinguish();
            }
        });
    }
}