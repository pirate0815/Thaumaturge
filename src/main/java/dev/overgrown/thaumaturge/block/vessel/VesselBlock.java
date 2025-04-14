package dev.overgrown.thaumaturge.block.vessel;

import dev.overgrown.thaumaturge.component.AspectComponent;
import dev.overgrown.thaumaturge.component.ModComponents;
import dev.overgrown.thaumaturge.data.Aspect;
import dev.overgrown.thaumaturge.recipe.Recipe;
import dev.overgrown.thaumaturge.recipe.RecipeManager;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
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
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new VesselBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        // Check for shift-right-click with empty hand to empty the Vessel
        for (Hand currentHand : Hand.values()) {
            ItemStack stackInHand = player.getStackInHand(currentHand);
            if (player.isSneaking() && stackInHand.isEmpty()) {
                VesselBlockEntity blockEntity = (VesselBlockEntity) world.getBlockEntity(pos);
                if (blockEntity == null) return ActionResult.PASS;

                BlockState currentState = world.getBlockState(pos);
                FluidType currentFluid = currentState.get(FLUID_TYPE);
                int currentLevel = currentState.get(LEVEL);

                if (currentFluid != FluidType.EMPTY || currentLevel > 0 || !blockEntity.isEmpty()) {
                    world.setBlockState(pos, getDefaultState());
                    blockEntity.clear();
                    world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    return ActionResult.SUCCESS;
                } else {
                    player.sendMessage(Text.translatable("block.thaumaturge.vessel.empty"), true);
                    return ActionResult.CONSUME;
                }
            }
        }
        // Check both hands for the item
        Hand[] hands = Hand.values();
        for (Hand currentHand : hands) {
            ItemStack stackInHand = player.getStackInHand(currentHand);
            FluidType fluidType = null;

            if (stackInHand.isOf(Items.WATER_BUCKET)) {
                fluidType = FluidType.WATER;
            } else if (stackInHand.isOf(Items.LAVA_BUCKET)) {
                fluidType = FluidType.LAVA;
            } else if (stackInHand.isOf(Items.POWDER_SNOW_BUCKET)) {
                fluidType = FluidType.POWDERED_SNOW;
            }

            if (fluidType != null && isHeatSource(world.getBlockState(pos.down()))) {
                int currentLevel = state.get(LEVEL);
                if (currentLevel < 3) {
                    world.setBlockState(pos, state.with(FLUID_TYPE, fluidType).with(LEVEL, currentLevel + 1), Block.NOTIFY_ALL);
                    if (!player.isCreative()) {
                        stackInHand.decrement(1);
                        player.giveItemStack(new ItemStack(Items.BUCKET));
                    }
                    world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    return ActionResult.SUCCESS;
                } else {
                    player.sendMessage(Text.translatable("block.thaumaturge.vessel.full"), true);
                    return ActionResult.CONSUME;
                }
            }

            // Handle catalyst and ingredient checks for each hand
            VesselBlockEntity blockEntity = (VesselBlockEntity) world.getBlockEntity(pos);
            if (blockEntity == null) continue;

            if (RecipeManager.isCatalyst(stackInHand.getItem())) {
                ActionResult catalystResult = handleCatalyst(world, pos, player, currentHand, stackInHand, blockEntity);
                if (catalystResult.isAccepted()) {
                    return catalystResult;
                }
            } else {
                ActionResult ingredientResult = handleIngredient(player, currentHand, stackInHand, blockEntity);
                if (ingredientResult.isAccepted()) {
                    return ingredientResult;
                }
            }
        }

        return ActionResult.PASS;
    }

    private ActionResult handleCatalyst(World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack catalystStack, VesselBlockEntity blockEntity) {

        BlockState currentState = world.getBlockState(pos);
        FluidType currentFluid = currentState.get(FLUID_TYPE);
        int currentLevel = currentState.get(LEVEL);

        // Only check for heat if the Vessel has a fluid
        if (currentFluid != FluidType.EMPTY && !isHeatSource(world.getBlockState(pos.down()))) {
            player.sendMessage(Text.translatable("block.thaumaturge.vessel.no_heat_or_fluid"), true);
            return ActionResult.CONSUME;
        }

        AspectComponent totalAspects = new AspectComponent(new Object2IntOpenHashMap<>());
        for (ItemStack itemStack : blockEntity.getItems()) {
            AspectComponent component = itemStack.getOrDefault(ModComponents.ASPECT, AspectComponent.DEFAULT);
            totalAspects.addAspect(component);
        }

        Optional<Recipe> recipe = RecipeManager.findMatchingRecipe(catalystStack, totalAspects, currentFluid, currentLevel);
        if (recipe.isPresent()) {
            boolean success = deductRequiredAspects(blockEntity, recipe.get().getRequiredAspects());
            if (success) {
                if (!player.isCreative()) catalystStack.decrement(1);
                player.giveItemStack(recipe.get().getOutput());
                world.playSound(null, pos, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return ActionResult.SUCCESS;
            } else {
                player.sendMessage(Text.translatable("block.thaumaturge.vessel.recipe_failed"), true);
                return ActionResult.CONSUME;
            }
        } else {
            player.sendMessage(Text.translatable("block.thaumaturge.vessel.recipe_failed"), true);
            return ActionResult.CONSUME;
        }
    }

    private boolean deductRequiredAspects(VesselBlockEntity blockEntity, Object2IntMap<RegistryEntry<Aspect>> requiredAspects) {
        Object2IntOpenHashMap<RegistryEntry<Aspect>> remaining = new Object2IntOpenHashMap<>(requiredAspects);

        for (int i = 0; i < blockEntity.size(); i++) {
            ItemStack stack = blockEntity.getStack(i);
            if (stack.isEmpty()) continue;

            AspectComponent component = stack.getOrDefault(ModComponents.ASPECT, AspectComponent.DEFAULT);
            Object2IntOpenHashMap<RegistryEntry<Aspect>> aspects = new Object2IntOpenHashMap<>(component.getMap());

            boolean modified = false;

            for (Object2IntMap.Entry<RegistryEntry<Aspect>> entry : remaining.object2IntEntrySet()) {
                RegistryEntry<Aspect> aspect = entry.getKey();
                int needed = entry.getIntValue();
                if (needed <= 0) continue;

                int present = aspects.getInt(aspect);
                int deduct = Math.min(present, needed);

                if (deduct > 0) {
                    aspects.put(aspect, present - deduct);
                    remaining.put(aspect, needed - deduct);
                    modified = true;
                }
            }

            if (modified) {
                ItemStack newStack = stack.copy();
                newStack.set(ModComponents.ASPECT, new AspectComponent(aspects));
                blockEntity.setStack(i, newStack);
            }

            if (remaining.values().intStream().allMatch(v -> v <= 0)) {
                break;
            }
        }

        // Remove empty aspect items by iterating backwards to avoid index issues
        for (int i = blockEntity.size() - 1; i >= 0; i--) {
            ItemStack stack = blockEntity.getStack(i);
            AspectComponent component = stack.getOrDefault(ModComponents.ASPECT, AspectComponent.DEFAULT);
            if (component.getMap().isEmpty()) {
                blockEntity.removeStack(i);
            }
        }

        return remaining.values().intStream().allMatch(v -> v <= 0);
    }

    private ActionResult handleIngredient(PlayerEntity player, Hand hand, ItemStack stack, VesselBlockEntity blockEntity) {

        World world = player.getWorld();
        BlockPos vesselPos = blockEntity.getPos();
        BlockState currentState = world.getBlockState(vesselPos);
        if (currentState.get(FLUID_TYPE) == FluidType.EMPTY || !isHeatSource(world.getBlockState(vesselPos.down()))) {
            player.sendMessage(Text.translatable("block.thaumaturge.vessel.no_heat_or_fluid"), true);
            return ActionResult.CONSUME;
        }

        if (blockEntity.addStack(stack)) {
            if (!player.isCreative()) stack.decrement(1);
            return ActionResult.SUCCESS;
        } else {
            player.sendMessage(Text.translatable("block.thaumaturge.vessel.inventory_full"), true);
            return ActionResult.CONSUME;
        }
    }

    @Override
    public void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        super.onStateReplaced(state, world, pos, moved);
    }
}
