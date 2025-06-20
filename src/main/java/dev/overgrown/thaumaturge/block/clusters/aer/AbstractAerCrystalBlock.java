package dev.overgrown.thaumaturge.block.clusters.aer;

import net.minecraft.block.AmethystClusterBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public abstract class AbstractAerCrystalBlock extends AmethystClusterBlock {
    public AbstractAerCrystalBlock(float height, float width, Settings settings) {
        super(height, width, settings);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(10) == 0) {
            world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.BLOCK_SAND_IDLE,
                    SoundCategory.BLOCKS, 0.1f, 0.6f + random.nextFloat() * 0.3f);
        }

        // Spawn particles
        int count = getParticleCount();
        for (int i = 0; i < count; i++) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            world.addParticleClient(ParticleTypes.POOF, false, false, x, y, z, 0, 0, 0);
        }
    }

    protected abstract int getParticleCount();

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient && !player.isCreative()) {
            Direction facing = state.get(FACING);
            Vec3d push = new Vec3d(facing.getOffsetX(), facing.getOffsetY(), facing.getOffsetZ())
                    .multiply(getPushStrength());
            player.addVelocity(push.getX(), push.getY(), push.getZ());
            player.velocityModified = true;
            world.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                    SoundEvents.ENTITY_BREEZE_DEFLECT,
                    SoundCategory.BLOCKS, 0.7f, 0.50f, world.random.nextLong());
        }
        return super.onBreak(world, pos, state, player);
    }

    protected abstract float getPushStrength();
}