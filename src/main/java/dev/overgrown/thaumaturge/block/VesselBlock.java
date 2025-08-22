package dev.overgrown.thaumaturge.block;

import dev.overgrown.thaumaturge.block.entity.VesselBlockEntity;
import dev.overgrown.thaumaturge.registry.ModBlocks;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

@SuppressWarnings("deprecation")
public class VesselBlock extends BlockWithEntity {
    public static final IntProperty WATER_LEVEL = IntProperty.of("water_level", 0, 3);
    private static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);

    public VesselBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(WATER_LEVEL, 0));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATER_LEVEL);
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
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (!(blockEntity instanceof VesselBlockEntity vessel)) {
            return ActionResult.PASS;
        }

        // Handle water filling/emptying
        if (stack.getItem() == Items.WATER_BUCKET && state.get(WATER_LEVEL) < 3) {
            if (!world.isClient) {
                world.setBlockState(pos, state.with(WATER_LEVEL, 3));
                if (!player.isCreative()) {
                    player.setStackInHand(hand, new ItemStack(Items.BUCKET));
                }
                updateBoilingState(world, pos, state.with(WATER_LEVEL, 3));
            }
            return ActionResult.SUCCESS;
        }

        if (stack.getItem() == Items.BUCKET && state.get(WATER_LEVEL) == 3) {
            if (!world.isClient) {
                world.setBlockState(pos, state.with(WATER_LEVEL, 0));
                if (!player.isCreative()) {
                    stack.decrement(1);
                    if (stack.isEmpty()) {
                        player.setStackInHand(hand, new ItemStack(Items.WATER_BUCKET));
                    } else {
                        player.getInventory().insertStack(new ItemStack(Items.WATER_BUCKET));
                    }
                }
                updateBoilingState(world, pos, state.with(WATER_LEVEL, 0));
            }
            return ActionResult.SUCCESS;
        }

        // Handle catalyst setting and immediate processing
        if (!stack.isEmpty()) {
            if (!world.isClient) {
                if (vessel.isCatalyst(stack)) {
                    // Set as catalyst and immediately try to craft
                    vessel.setCatalyst(stack.split(1));
                    vessel.processItem(); // Try to craft immediately
                    return ActionResult.SUCCESS;
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

    private void updateBoilingState(World world, BlockPos pos, BlockState state) {
        boolean hasWater = state.get(WATER_LEVEL) > 0;
        boolean hasHeat = isHeatSourceBelow(world, pos);

        if (world.getBlockEntity(pos) instanceof VesselBlockEntity vessel) {
            vessel.setBoiling(hasWater && hasHeat);
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        // Check if we should start/stop boiling based on heat source
        boolean hasWater = state.get(WATER_LEVEL) > 0;
        boolean hasHeat = isHeatSourceBelow(world, pos);

        if (world.getBlockEntity(pos) instanceof VesselBlockEntity vessel) {
            vessel.setBoiling(hasWater && hasHeat);
        }
        super.neighborUpdate(state, world, pos, block, fromPos, notify);
    }
}