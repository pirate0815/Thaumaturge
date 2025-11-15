package dev.overgrown.thaumaturge.block.jar;

import dev.overgrown.thaumaturge.util.CorruptionHelper;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class JarBlock extends BlockWithEntity {

    public static final VoxelShape SHAPE = VoxelShapes.union(
    createCuboidShape(1, 0, 1, 15, 14,15),
    createCuboidShape(3, 14, 3, 13, 16, 13));


    public JarBlock(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new JarBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        if (world != null) {
            BlockEntity entity = world.getBlockEntity(pos);
            return entity instanceof JarBlockEntity jarBlockEntity ? jarBlockEntity.comparatorLevel() : 0;
        }
        return 0;
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }


    // If the block is destroyed add aspects as corruption to the world
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (world instanceof ServerWorld serverWorld) {
                BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof JarBlockEntity jarBlockEntity) {
                    CorruptionHelper.addCorruption(serverWorld, pos, jarBlockEntity.getLevel());
                }
            }
            super.onStateReplaced(state,world,pos,newState,moved);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof JarBlockEntity jarBlockEntity) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.getItem() == Items.SLIME_BALL) {
                if (jarBlockEntity.seal()) {
                    if (!world.isClient) {
                        if (!player.isCreative()) {
                            stack.decrement(1);
                            if (stack.getCount() == 0) {
                                player.setStackInHand(hand, ItemStack.EMPTY);
                            }
                        }
                    }
                    return ActionResult.CONSUME;
                }
            }
            else if (stack.isEmpty()) {
                if (jarBlockEntity.unseal()) {
                    if (!world.isClient) {
                        if (!player.isCreative()) {
                            player.setStackInHand(hand, new ItemStack(Items.SLIME_BALL));
                        }
                    }
                    return ActionResult.SUCCESS;
                }
            }
        }
        return ActionResult.PASS;
    }
}
