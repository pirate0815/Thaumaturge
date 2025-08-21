package dev.overgrown.thaumaturge.block;

import dev.overgrown.thaumaturge.block.entity.VesselBlockEntity;
import dev.overgrown.thaumaturge.registry.ModBlocks;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

@SuppressWarnings("deprecation")
public class VesselBlock extends BlockWithEntity implements Waterloggable {
    public static final IntProperty WATER_LEVEL = IntProperty.of("water_level", 0, 3);
    public static final BooleanProperty BOILING = BooleanProperty.of("boiling");
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    private static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);

    public VesselBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(WATER_LEVEL, 0)
                .with(BOILING, false)
                .with(WATERLOGGED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATER_LEVEL, BOILING, WATERLOGGED);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new VesselBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : checkType(type, ModBlocks.VESSEL_BLOCK_ENTITY, VesselBlockEntity::serverTick);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);

        if (stack.getItem() == Items.WATER_BUCKET && state.get(WATER_LEVEL) < 3) {
            if (!world.isClient) {
                world.setBlockState(pos, state.with(WATER_LEVEL, 3).with(BOILING, isHeatSourceBelow(world, pos)));
                if (!player.isCreative()) {
                    player.setStackInHand(hand, new ItemStack(Items.BUCKET));
                }
            }
            return ActionResult.SUCCESS;
        }

        if (stack.getItem() == Items.BUCKET && state.get(WATER_LEVEL) == 3) {
            if (!world.isClient) {
                world.setBlockState(pos, state.with(WATER_LEVEL, 0).with(BOILING, false));
                if (!player.isCreative()) {
                    stack.decrement(1);
                    if (stack.isEmpty()) {
                        player.setStackInHand(hand, new ItemStack(Items.WATER_BUCKET));
                    } else {
                        player.getInventory().insertStack(new ItemStack(Items.WATER_BUCKET));
                    }
                }
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    public static boolean isHeatSourceBelow(World world, BlockPos pos) {
        BlockPos belowPos = pos.down();
        BlockState belowState = world.getBlockState(belowPos);

        return belowState.isOf(Blocks.FIRE) ||
                belowState.isOf(Blocks.LAVA) ||
                belowState.isOf(Blocks.CAMPFIRE) ||
                belowState.isOf(Blocks.SOUL_CAMPFIRE);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        BlockState state = super.getPlacementState(ctx);
        if (state == null) {
            state = getDefaultState();
        }
        return state.with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : Fluids.EMPTY.getDefaultState();
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return state;
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!oldState.isOf(state.getBlock()) && state.get(WATER_LEVEL) > 0) {
            world.setBlockState(pos, state.with(BOILING, isHeatSourceBelow(world, pos)));
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (state.get(WATER_LEVEL) > 0) {
            boolean boiling = isHeatSourceBelow(world, pos);
            if (state.get(BOILING) != boiling) {
                world.setBlockState(pos, state.with(BOILING, boiling));
            }
        }
        super.neighborUpdate(state, world, pos, block, fromPos, notify);
    }
}