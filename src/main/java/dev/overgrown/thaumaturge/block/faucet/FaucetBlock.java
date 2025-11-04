package dev.overgrown.thaumaturge.block.faucet;

import dev.overgrown.thaumaturge.block.api.AspectContainer;
import dev.overgrown.thaumaturge.block.faucet.entity.FaucetBlockEntity;
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
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
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
        Direction horizontalFacing = ctx.getHorizontalPlayerFacing();
        if (ctx.getWorld().getBlockEntity(ctx.getBlockPos().offset(horizontalFacing)) instanceof AspectContainer) {
            return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing());
        }
        return null;
    }
}
