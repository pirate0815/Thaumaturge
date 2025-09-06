package dev.overgrown.thaumaturge.block.aspect_clusters;

import dev.overgrown.thaumaturge.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public abstract class AspectClusterBlock extends Block {
    public AspectClusterBlock(Settings settings) {
        super(settings);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        // Play ambient sound when player approaches
        if (world.isClient) {
            world.playSoundAtBlockCenter(pos, ModSounds.ASPECT_CLUSTER_AMBIENT, SoundCategory.BLOCKS, 0.5f,
                    world.random.nextFloat() * 0.1f + 0.9f, false);
        }
        super.onBlockBreakStart(state, world, pos, player);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        // Emit particles
        if (random.nextInt(5) == 0) {
            double x = (double)pos.getX() + random.nextDouble();
            double y = (double)pos.getY() + random.nextDouble();
            double z = (double)pos.getZ() + random.nextDouble();
            world.addParticle(getParticleType(), x, y, z, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        // Trigger break effect
        if (!world.isClient) {
            triggerBreakEffect(world, pos, state, player);
        }
        super.onBreak(world, pos, state, player);
    }

    protected abstract DefaultParticleType getParticleType();
    protected abstract void triggerBreakEffect(World world, BlockPos pos, BlockState state, PlayerEntity player);
}