package dev.overgrown.thaumaturge.block.clusters;

import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class AerCrystalClusterBlock extends ExperienceDroppingBlock implements Waterloggable {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final EnumProperty<Direction> FACING = Properties.FACING;

    private static final float XZ_OFFSET = 3.0F;
    private static final float CLUSTER_LENGTH = 9.0F;

    protected final VoxelShape northShape;
    protected final VoxelShape southShape;
    protected final VoxelShape eastShape;
    protected final VoxelShape westShape;
    protected final VoxelShape upShape;
    protected final VoxelShape downShape;

    public AerCrystalClusterBlock(IntProvider experience, Settings settings) {
        super(experience, settings);
        this.setDefaultState(getStateManager().getDefaultState()
                .with(WATERLOGGED, false)
                .with(FACING, Direction.UP)
        );

        this.upShape = createUpDownShape(0.0F);
        this.downShape = createUpDownShape(16.0F - CLUSTER_LENGTH);
        this.northShape = createNorthSouthShape(16.0F - CLUSTER_LENGTH);
        this.southShape = createNorthSouthShape(0.0F);
        this.eastShape = createEastWestShape(0.0F);
        this.westShape = createEastWestShape(16.0F - CLUSTER_LENGTH);
    }

    private VoxelShape createUpDownShape(float minY) {
        return Block.createCuboidShape(
                XZ_OFFSET,
                minY,
                XZ_OFFSET,
                16.0F - XZ_OFFSET,
                minY + CLUSTER_LENGTH,  // maxY
                16.0F - XZ_OFFSET
        );
    }

    private VoxelShape createNorthSouthShape(float minZ) {
        return Block.createCuboidShape(
                XZ_OFFSET,
                XZ_OFFSET,
                minZ,
                16.0F - XZ_OFFSET,
                16.0F - XZ_OFFSET,
                minZ + CLUSTER_LENGTH  // maxZ
        );
    }

    private VoxelShape createEastWestShape(float minX) {
        return Block.createCuboidShape(
                minX,
                XZ_OFFSET,
                XZ_OFFSET,
                minX + CLUSTER_LENGTH,  // maxX
                16.0F - XZ_OFFSET,
                16.0F - XZ_OFFSET
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState()
                .with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER)
                .with(FACING, ctx.getSide());
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(
            BlockState state,
            WorldView world,
            ScheduledTickView tickView,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            Random random
    ) {
        if (state.get(WATERLOGGED)) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return direction == state.get(FACING).getOpposite() && !state.canPlaceAt(world, pos)
                ? Blocks.AIR.getDefaultState()
                : super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case NORTH -> northShape;
            case SOUTH -> southShape;
            case EAST -> eastShape;
            case WEST -> westShape;
            case DOWN -> downShape;
            default -> upShape;
        };
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction direction = state.get(FACING);
        BlockPos blockPos = pos.offset(direction.getOpposite());
        return world.getBlockState(blockPos).isSideSolidFullSquare(world, blockPos, direction);
    }
}