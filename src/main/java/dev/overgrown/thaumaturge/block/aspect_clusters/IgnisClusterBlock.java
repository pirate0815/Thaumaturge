package dev.overgrown.thaumaturge.block.aspect_clusters;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class IgnisClusterBlock extends AspectClusterBlock {
    public IgnisClusterBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected DefaultParticleType getParticleType() {
        return ParticleTypes.FLAME;
    }

    @Override
    protected void triggerBreakEffect(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        // Set nearby blocks and entities on fire
        BlockPos.iterateOutwards(pos, 3, 3, 3).forEach(blockPos -> {
            if (world.getBlockState(blockPos).isAir() && world.random.nextFloat() < 0.7f) {
                world.setBlockState(blockPos, net.minecraft.block.Blocks.FIRE.getDefaultState());
            }
        });

        world.getOtherEntities(null, new Box(pos.add(-10, -10, -10), pos.add(10, 10, 10))).forEach(entity -> {
            if (entity.squaredDistanceTo(Vec3d.ofCenter(pos)) <= 16.0) { // 4 block radius
                entity.setOnFireFor(5); // Set on fire for 5 seconds
            }
        });
    }
}