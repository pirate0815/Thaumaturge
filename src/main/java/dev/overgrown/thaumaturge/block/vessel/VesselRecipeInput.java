package dev.overgrown.thaumaturge.block.vessel;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.item.Item;

import java.util.List;

public class VesselRecipeInput implements RecipeInput {
    private final List<Item> sequence;
    private final VesselBlock.FluidType fluidType;
    private final int level;

    public VesselRecipeInput(List<Item> sequence, VesselBlock.FluidType fluidType, int level) {
        this.sequence = sequence;
        this.fluidType = fluidType;
        this.level = level;
    }

    public List<Item> getSequence() {
        return sequence;
    }

    public VesselBlock.FluidType getFluidType() {
        return fluidType;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }
}