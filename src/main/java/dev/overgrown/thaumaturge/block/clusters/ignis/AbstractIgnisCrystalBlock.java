package dev.overgrown.thaumaturge.block.clusters.ignis;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AmethystClusterBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public abstract class AbstractIgnisCrystalBlock extends AmethystClusterBlock {
    public AbstractIgnisCrystalBlock(float height, float width, AbstractBlock.Settings settings) {
        super(height, width, settings);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(10) == 0) {
            world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.BLOCK_FIRE_AMBIENT,
                    SoundCategory.BLOCKS, 0.1f, 0.6f + random.nextFloat() * 0.3f);
        }

        int count = getParticleCount();
        for (int i = 0; i < count; i++) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            world.addParticleClient(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
        }
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        if (!world.isClient()) {
            for (Direction direction : Direction.values()) {
                BlockPos adjacentPos = pos.offset(direction);
                if (world.getBlockState(adjacentPos).isAir()) {
                    world.setBlockState(adjacentPos, Blocks.FIRE.getDefaultState());
                }
            }
        }
        return state;
    }

    protected abstract int getParticleCount();
}