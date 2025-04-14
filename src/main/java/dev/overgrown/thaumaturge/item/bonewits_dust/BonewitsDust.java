package dev.overgrown.thaumaturge.item.bonewits_dust;

import dev.overgrown.thaumaturge.block.ModBlocks;
import dev.overgrown.thaumaturge.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.HashMap;
import java.util.Map;

public class BonewitsDust extends Item {
    public BonewitsDust(Settings settings) {
        super(settings);
    }

    private static final Map<Block, TransformationHandler> TRANSFORMATIONS = new HashMap<>();

    static {
        // Transform Bookshelf to Apophenia
        TRANSFORMATIONS.put(Blocks.BOOKSHELF, (world, pos, player, stack) -> {
            world.breakBlock(pos, false); // Destroy block without drops
            ItemStack book = new ItemStack(ModItems.APOPHENIA);
            ItemEntity item = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, book);
            world.spawnEntity(item);
            return true;
        });

        // Transform Cauldron to Vessel
        TRANSFORMATIONS.put(Blocks.CAULDRON, (world, pos, player, stack) -> {
            world.setBlockState(pos, ModBlocks.VESSEL.getDefaultState());
            return true;
        });
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        TransformationHandler handler = TRANSFORMATIONS.get(block);
        if (handler != null) {
            if (!world.isClient()) {
                PlayerEntity player = context.getPlayer();
                boolean success = handler.handle(world, pos, player, context.getStack());
                if (success) {
                    if (player != null && !player.getAbilities().creativeMode) {
                        context.getStack().decrement(1);
                    }
                }
            }
            // Return appropriate ActionResult based on client/server side
            return world.isClient() ? ActionResult.SUCCESS : ActionResult.SUCCESS_SERVER;
        }
        return ActionResult.PASS;
    }

    @FunctionalInterface
    private interface TransformationHandler {
        boolean handle(World world, BlockPos pos, PlayerEntity player, ItemStack stack);
    }
}