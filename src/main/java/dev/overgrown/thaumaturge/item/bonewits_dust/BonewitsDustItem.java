package dev.overgrown.thaumaturge.item.bonewits_dust;

import dev.overgrown.thaumaturge.registry.ModBlocks;
import dev.overgrown.thaumaturge.registry.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BonewitsDustItem extends Item {
    public BonewitsDustItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        ItemStack stack = context.getStack();

        // Check if we're transforming a bookshelf
        if (state.isOf(Blocks.BOOKSHELF)) {
            if (!world.isClient) {
                // Replace with air and drop Apophenia item
                world.breakBlock(pos, false);
                ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        new ItemStack(ModItems.APOPHENIA));
                world.spawnEntity(itemEntity);

                // Consume one dust
                stack.decrement(1);
            }
            return ActionResult.SUCCESS;
        }

        // Check if we're transforming a cauldron
        if (state.isOf(Blocks.CAULDRON)) {
            if (!world.isClient) {
                // Replace with Vessel block
                world.setBlockState(pos, ModBlocks.VESSEL.getDefaultState());

                // Consume one dust
                stack.decrement(1);
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}