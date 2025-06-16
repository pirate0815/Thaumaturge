package dev.overgrown.thaumaturge.block.vessel;

import dev.overgrown.thaumaturge.component.AspectComponent;
import dev.overgrown.thaumaturge.component.ModComponents;
import dev.overgrown.thaumaturge.data.Aspect;
import dev.overgrown.thaumaturge.block.vessel.recipe.Recipe;
import dev.overgrown.thaumaturge.block.vessel.recipe.RecipeManager;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.registry.tag.BlockTags;

import java.util.Optional;

public class VesselBlock extends Block implements BlockEntityProvider {
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
                .with(FLUID_TYPE, FluidType.EMPTY)
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
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new VesselBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        // Shift-right-click to reset only if not empty
        if (player.isSneaking() && player.getStackInHand(Hand.MAIN_HAND).isEmpty() && player.getStackInHand(Hand.OFF_HAND).isEmpty()) {
            VesselBlockEntity blockEntity = (VesselBlockEntity) world.getBlockEntity(pos);
            boolean isFluidEmpty = state.get(FLUID_TYPE) == FluidType.EMPTY && state.get(LEVEL) == 0;
            boolean hasAspects = blockEntity != null && !blockEntity.getAspectComponent().isEmpty();

            if (!isFluidEmpty || hasAspects) {
                resetVessel(world, pos, state);
                world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return ActionResult.SUCCESS;
            } else {
                return ActionResult.PASS;
            }
        }

        // Check both hands for interactions
        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getStackInHand(hand);

            // Check for fluid filling
            FluidType fluidType = getFluidType(stack);
            if (fluidType != null) {
                return handleFluidAddition(world, pos, state, player, stack, fluidType);
            }

            VesselBlockEntity blockEntity = (VesselBlockEntity) world.getBlockEntity(pos);
            if (blockEntity == null) continue;

            // Check heat and fluid requirements
            if (!isHeatSource(world.getBlockState(pos.down())) || state.get(FLUID_TYPE) == FluidType.EMPTY) {
                player.sendMessage(Text.translatable("block.thaumaturge.vessel.no_heat_or_fluid"), true);
                return ActionResult.CONSUME;
            }

            // Handle catalyst or aspect item
            if (RecipeManager.isCatalyst(stack.getItem())) {
                return handleCatalyst(world, pos, player, hand, stack, blockEntity, state);
            } else {
                ActionResult result = handleIngredient(player, hand, stack, blockEntity);
                if (result.isAccepted()) return result;
            }
        }

        return ActionResult.PASS;
    }

    private ActionResult handleFluidAddition(World world, BlockPos pos, BlockState state, PlayerEntity player, ItemStack stack, FluidType fluidType) {
        int currentLevel = state.get(LEVEL);
        if (currentLevel < 3) {
            world.setBlockState(pos, state.with(FLUID_TYPE, fluidType).with(LEVEL, currentLevel + 1));
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

    private ActionResult handleCatalyst(World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack catalystStack, VesselBlockEntity blockEntity, BlockState state) {
        FluidType currentFluid = state.get(FLUID_TYPE);
        int currentLevel = state.get(LEVEL);

        Optional<Recipe> recipe = RecipeManager.findMatchingRecipe(catalystStack, blockEntity.getAspectComponent(), currentFluid, currentLevel);
        if (recipe.isEmpty()) {
            player.sendMessage(Text.translatable("block.thaumaturge.vessel.recipe_failed"), true);
            return ActionResult.CONSUME;
        }

        if (!deductRequiredAspects(blockEntity, recipe.get().getRequiredAspects())) {
            player.sendMessage(Text.translatable("block.thaumaturge.vessel.insufficient_aspects"), true);
            return ActionResult.CONSUME;
        }

        // Successfully crafted
        if (!player.isCreative()) {
            catalystStack.decrement(1);
        }
        player.giveItemStack(recipe.get().getOutput());

        // 50% chance to reduce fluid level
        if (world.random.nextBoolean()) {
            reduceFluidLevel(world, pos, state, blockEntity);
        }

        world.playSound(null, pos, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 1.0F, 1.0F);
        return ActionResult.SUCCESS;
    }

    private void reduceFluidLevel(World world, BlockPos pos, BlockState state, VesselBlockEntity blockEntity) {
        int currentLevel = state.get(LEVEL);
        if (currentLevel > 0) {
            int newLevel = currentLevel - 1;
            BlockState newState;
            if (newLevel == 0) {
                newState = getDefaultState().with(FLUID_TYPE, FluidType.EMPTY).with(LEVEL, 0);
                blockEntity.reset();
            } else {
                newState = state.with(LEVEL, newLevel);
            }
            world.setBlockState(pos, newState);
            world.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 1.0F);
        }
    }

    private boolean deductRequiredAspects(VesselBlockEntity blockEntity, Object2IntMap<RegistryEntry<Aspect>> requiredAspects) {
        AspectComponent aspects = blockEntity.getAspectComponent();
        for (Object2IntMap.Entry<RegistryEntry<Aspect>> entry : requiredAspects.object2IntEntrySet()) {
            if (aspects.getMap().getInt(entry.getKey()) < entry.getIntValue()) {
                return false;
            }
        }

        // Deduct the aspects
        for (Object2IntMap.Entry<RegistryEntry<Aspect>> entry : requiredAspects.object2IntEntrySet()) {
            RegistryEntry<Aspect> aspect = entry.getKey();
            int amount = entry.getIntValue();
            aspects.getMap().put(aspect, aspects.getMap().getInt(aspect) - amount);
            if (aspects.getMap().getInt(aspect) <= 0) {
                aspects.getMap().removeInt(aspect);
            }
        }

        blockEntity.markDirty();
        return true;
    }

    private ActionResult handleIngredient(PlayerEntity player, Hand hand, ItemStack stack, VesselBlockEntity blockEntity) {
        AspectComponent itemAspects = stack.getOrDefault(ModComponents.ASPECT, AspectComponent.DEFAULT);
        if (itemAspects.isEmpty()) {
            player.sendMessage(Text.translatable("block.thaumaturge.vessel.no_aspects"), true);
            return ActionResult.PASS;
        }

        blockEntity.getAspectComponent().addAspect(itemAspects);
        if (!player.isCreative()) {
            stack.decrement(1);
        }

        player.getWorld().playSound(null, blockEntity.getPos(), SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.BLOCKS, 1.0F, 1.0F);
        blockEntity.markDirty();
        return ActionResult.SUCCESS;
    }

    private void resetVessel(World world, BlockPos pos, BlockState state) {
        world.setBlockState(pos, getDefaultState());
        VesselBlockEntity blockEntity = (VesselBlockEntity) world.getBlockEntity(pos);
        if (blockEntity != null) {
            blockEntity.reset();
        }
    }

    private FluidType getFluidType(ItemStack stack) {
        if (stack.isOf(Items.WATER_BUCKET)) return FluidType.WATER;
        if (stack.isOf(Items.LAVA_BUCKET)) return FluidType.LAVA;
        if (stack.isOf(Items.POWDER_SNOW_BUCKET)) return FluidType.POWDERED_SNOW;
        return null;
    }

    @Override
    public void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        super.onStateReplaced(state, world, pos, moved);
    }
}
