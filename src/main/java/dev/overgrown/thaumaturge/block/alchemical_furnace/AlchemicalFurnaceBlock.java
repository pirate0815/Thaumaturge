package dev.overgrown.thaumaturge.block.alchemical_furnace;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.registry.ModBlocks;
import dev.overgrown.thaumaturge.util.CorruptionHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AlchemicalFurnaceBlock extends BlockWithEntity {

    public static final BooleanProperty LIT = Properties.LIT;
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    public AlchemicalFurnaceBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(LIT, false).with(FACING, Direction.NORTH));
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AlchemicalFurnaceBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : checkType(type, ModBlocks.ALCHEMICAL_FURNACE_BLOCK_ENTITY, AlchemicalFurnaceBlockEntity::serverTick);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(LIT).add(FACING);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (world instanceof ServerWorld serverWorld) {
                BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof AlchemicalFurnaceBlockEntity alchemicalFurnaceBlockEntity) {
                    CorruptionHelper.addCorruption(serverWorld, pos, alchemicalFurnaceBlockEntity.getTotalAspectAmount());
                    ItemScatterer.spawn(world, pos, alchemicalFurnaceBlockEntity);
                }
            }
            super.onStateReplaced(state,world,pos,newState,moved);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient)  {
            NamedScreenHandlerFactory factory = state.createScreenHandlerFactory(world, pos);
            if (factory != null) {
                player.openHandledScreen(factory);
            } else {
                Thaumaturge.LOGGER.info("screen factory is null");
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }
}
