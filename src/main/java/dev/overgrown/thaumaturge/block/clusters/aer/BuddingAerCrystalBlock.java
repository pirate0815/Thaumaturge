package dev.overgrown.thaumaturge.block.clusters.aer;

import dev.overgrown.thaumaturge.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

import static net.minecraft.block.AmethystClusterBlock.FACING;
import static net.minecraft.block.AmethystClusterBlock.WATERLOGGED;

public class BuddingAerCrystalBlock extends Block {
    public BuddingAerCrystalBlock(Settings settings) {
        super(settings);
    }

    private static final Direction[] DIRECTIONS = Direction.values();

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        Direction dir = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
        BlockPos targetPos = pos.offset(dir);
        BlockState targetState = world.getBlockState(targetPos);

        Block nextBlock = null;
        if (canGrowIn(targetState)) {
            nextBlock = ModBlocks.SMALL_AER_CRYSTAL_BUD;
        } else if (targetState.isOf(ModBlocks.SMALL_AER_CRYSTAL_BUD) && targetState.get(FACING) == dir) {
            nextBlock = ModBlocks.MEDIUM_AER_CRYSTAL_BUD;
        } else if (targetState.isOf(ModBlocks.MEDIUM_AER_CRYSTAL_BUD) && targetState.get(FACING) == dir) {
            nextBlock = ModBlocks.LARGE_AER_CRYSTAL_BUD;
        } else if (targetState.isOf(ModBlocks.LARGE_AER_CRYSTAL_BUD) && targetState.get(FACING) == dir) {
            nextBlock = ModBlocks.AER_CRYSTAL_CLUSTER;
        }

        if (nextBlock != null && random.nextInt(5) == 0) { // 20% chance after valid direction check
            BlockState newState = nextBlock.getDefaultState()
                    .with(FACING, dir)
                    .with(WATERLOGGED, targetState.getFluidState().getFluid() == Fluids.WATER);
            world.setBlockState(targetPos, newState);
        }
    }

    public static boolean canGrowIn(BlockState state) {
        return state.isAir() || (state.isOf(Blocks.WATER) && state.getFluidState().getLevel() == 8);
    }
}