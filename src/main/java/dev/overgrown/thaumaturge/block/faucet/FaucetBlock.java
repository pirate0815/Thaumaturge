package dev.overgrown.thaumaturge.block.faucet;

import dev.overgrown.thaumaturge.block.api.AspectContainer;
import dev.overgrown.thaumaturge.registry.ModBlocks;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class FaucetBlock extends BlockWithEntity {

    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    private final static VoxelShape SHAPE_NORTH = Block.createCuboidShape(6, 6, 0, 10, 10, 5);
    private final static VoxelShape SHAPE_SOUTH = Block.createCuboidShape(6, 6, 11, 10, 10, 16);
    private final static VoxelShape SHAPE_WEST = Block.createCuboidShape(0, 6, 6, 5, 10, 10);
    private final static VoxelShape SHAPE_EAST = Block.createCuboidShape(11, 6, 6, 16, 10, 10);

    public FaucetBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }


    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {builder.add(FACING);}

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FaucetBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : checkType(type, ModBlocks.FAUCET_BLOCK_ENTITY, FaucetBlockEntity::serverTick);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        switch (state.get(FACING)) {
            case SOUTH -> {return SHAPE_SOUTH;}
            case WEST -> {return SHAPE_WEST;}
            case EAST -> {return SHAPE_EAST;}
            default -> {return SHAPE_NORTH;}
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        for(Direction direction : ctx.getPlacementDirections()) {
            BlockState blockState = this.getDefaultState();
            if (direction.getAxis() != Direction.Axis.Y) {
                if (canPlaceAt(ctx.getWorld(), ctx.getBlockPos(), direction)) {
                    return blockState.with(FACING, direction);
                }
            }

        }
        return null;
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
        if (!canPlaceAt(state, world, pos) && (!world.isClient)) {
            world.breakBlock(pos, true);

        }
    }

    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return canPlaceAt(world, pos, state.get(FACING));
    }

    public static boolean canPlaceAt(WorldView world, BlockPos pos, Direction direction) {
        BlockEntity entity = world.getBlockEntity(pos.offset(direction));
        return entity instanceof AspectContainer aspectContainer && aspectContainer.canReduceAspectLevels();
    }

    public static Vec3d nozzlePos(BlockPos pos, BlockState state) {
        switch (state.get(FACING)) {
            case SOUTH -> {return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.75);}
            case WEST -> {return new Vec3d(pos.getX() + 0.25, pos.getY() + 0.25, pos.getZ() + 0.5);}
            case EAST -> {return new Vec3d(pos.getX() + 0.75, pos.getY() + 0.25, pos.getZ() + 0.5);}
            default -> {return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.25);}
        }
    }
}
