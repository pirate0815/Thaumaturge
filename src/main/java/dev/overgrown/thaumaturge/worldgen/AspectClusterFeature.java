package dev.overgrown.thaumaturge.worldgen;

import com.mojang.serialization.Codec;
import dev.overgrown.thaumaturge.block.aspect_clusters.AspectClusterBlock;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class AspectClusterFeature extends Feature<AspectClusterFeatureConfig> {
    private static final Direction[] DIRECTIONS = Direction.values();

    public AspectClusterFeature(Codec<AspectClusterFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<AspectClusterFeatureConfig> context) {
        AspectClusterFeatureConfig config = context.getConfig();
        StructureWorldAccess world = context.getWorld();
        BlockPos origin = context.getOrigin();
        Random random = context.getRandom();

        BlockState clusterState = config.state();
        int placed = 0;

        for (int i = 0; i < config.tries(); i++) {
            BlockPos pos = origin.add(
                    random.nextInt(config.spreadXZ() * 2 + 1) - config.spreadXZ(),
                    random.nextInt(config.spreadY() * 2 + 1) - config.spreadY(),
                    random.nextInt(config.spreadXZ() * 2 + 1) - config.spreadXZ()
            );

            // The position must be air (or water for waterlogged placement)
            BlockState existing = world.getBlockState(pos);
            if (!existing.isAir() && !existing.getFluidState().isOf(Fluids.WATER)) {
                continue;
            }

            // Shuffle directions for variety, then find a solid face to attach to
            Direction[] dirs = shuffleDirections(random);
            for (Direction dir : dirs) {
                // Check if the block in this direction is a solid full face
                BlockPos supportPos = pos.offset(dir);
                BlockState supportState = world.getBlockState(supportPos);
                Direction facing = dir.getOpposite(); // cluster faces away from support

                if (supportState.isSideSolidFullSquare(world, supportPos, facing)) {
                    boolean waterlogged = world.getFluidState(pos).isOf(Fluids.WATER);
                    BlockState toPlace = clusterState
                            .with(AspectClusterBlock.FACING, facing)
                            .with(AspectClusterBlock.WATERLOGGED, waterlogged);
                    world.setBlockState(pos, toPlace, 2);
                    placed++;
                    break;
                }
            }
        }

        return placed > 0;
    }

    private static Direction[] shuffleDirections(Random random) {
        Direction[] dirs = DIRECTIONS.clone();
        for (int i = dirs.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Direction temp = dirs[i];
            dirs[i] = dirs[j];
            dirs[j] = temp;
        }
        return dirs;
    }
}
