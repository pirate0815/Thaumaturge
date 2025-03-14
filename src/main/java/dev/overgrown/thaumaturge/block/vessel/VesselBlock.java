package dev.overgrown.thaumaturge.block.vessel;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.ItemScatterer;
import net.minecraft.registry.tag.BlockTags;

public class VesselBlock extends Block {
    public enum FluidType implements StringIdentifiable {
        EMPTY, WATER, LAVA, POWDERED_SNOW;

        @Override
        public String asString() {
            return this.name().toLowerCase();
        }
    }

    public static final EnumProperty<FluidType> FLUID_TYPE = EnumProperty.of("fluid_type", FluidType.class);
    public static final IntProperty LEVEL = IntProperty.of("level", 0, 3);

    public VesselBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FLUID_TYPE, FluidType.WATER)
                .with(LEVEL, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FLUID_TYPE, LEVEL);
    }

    private boolean isHeatSource(BlockState state) {
        return state.isIn(BlockTags.FIRE) ||
                state.isOf(Blocks.LAVA) ||
                (state.getBlock() instanceof CampfireBlock && state.get(CampfireBlock.LIT));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        // Check both hands for the item
        Hand[] hands = Hand.values();
        for (Hand hand : hands) {
            ItemStack stack = player.getStackInHand(hand);
            FluidType fluidType = null;

            if (stack.isOf(Items.WATER_BUCKET)) {
                fluidType = FluidType.WATER;
            } else if (stack.isOf(Items.LAVA_BUCKET)) {
                fluidType = FluidType.LAVA;
            } else if (stack.isOf(Items.POWDER_SNOW_BUCKET)) {
                fluidType = FluidType.POWDERED_SNOW;
            }

            if (fluidType != null && isHeatSource(world.getBlockState(pos.down()))) {
                int currentLevel = state.get(LEVEL);
                if (currentLevel < 3) {
                    world.setBlockState(pos, state.with(FLUID_TYPE, fluidType).with(LEVEL, currentLevel + 1), Block.NOTIFY_ALL);
                    if (!player.isCreative()) {
                        stack.decrement(1);
                        player.giveItemStack(new ItemStack(Items.BUCKET));
                    }
                    world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    return ActionResult.SUCCESS;
                } else {
                    player.sendMessage(Text.translatable("block.thaumaturge.vessel.full"), true);
                    return ActionResult.CONSUME;
                }
            }
        }
        return ActionResult.PASS;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
        if (!world.isClient && !state.isOf(newState.getBlock())) {
            int level = state.get(LEVEL);
            if (level > 0) {
                Item fluidItem = Items.AIR;
                switch (state.get(FLUID_TYPE)) {
                    case WATER -> fluidItem = Items.WATER_BUCKET;
                    case LAVA -> fluidItem = Items.LAVA_BUCKET;
                    case POWDERED_SNOW -> fluidItem = Items.POWDER_SNOW_BUCKET;
                }
                ItemScatterer.spawn(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(fluidItem));
            }
        }
    }
}