package dev.overgrown.thaumaturge.block.aspect_clusters;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TerraClusterBlock extends AspectClusterBlock {
    public TerraClusterBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected DefaultParticleType getParticleType() {
        return ParticleTypes.ITEM_SLIME;
    }

    @Override
    protected void triggerBreakEffect(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        // Grow plants and grass in nearby areas
        BlockPos.iterateOutwards(pos, 5, 3, 5).forEach(blockPos -> {
            BlockState blockState = world.getBlockState(blockPos);

            if (blockState.isOf(net.minecraft.block.Blocks.DIRT)) {
                world.setBlockState(blockPos, net.minecraft.block.Blocks.GRASS_BLOCK.getDefaultState());
            } else if (blockState.isOf(net.minecraft.block.Blocks.GRASS_BLOCK)) {
                // Randomly grow tall grass or flowers
                BlockPos abovePos = blockPos.up();
                if (world.getBlockState(abovePos).isAir() && world.random.nextFloat() < 0.4f) {
                    world.setBlockState(abovePos,
                            world.random.nextBoolean() ?
                                    net.minecraft.block.Blocks.GRASS.getDefaultState() :
                                    net.minecraft.block.Blocks.DANDELION.getDefaultState());
                }
            }
        });
    }
}