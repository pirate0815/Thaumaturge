package dev.overgrown.thaumaturge.block.aspect_clusters;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AerClusterBlock extends AspectClusterBlock {
    public AerClusterBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected DefaultParticleType getParticleType() {
        return ParticleTypes.CLOUD;
    }

    @Override
    protected void triggerBreakEffect(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        // Launch entities upward
        world.getOtherEntities(null, new Box(pos.add(-10, -10, -10), pos.add(10, 10, 10))).forEach(entity -> {
            if (entity.squaredDistanceTo(Vec3d.ofCenter(pos)) <= 25.0) { // 5 block radius
                entity.addVelocity(0, 1.5, 0); // Launch upward
            }
        });
    }
}