package dev.overgrown.thaumaturge.block.vessel;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.item.Item;

import java.util.List;

public record VesselRecipeInput(List<Item> sequence, VesselBlock.FluidType fluidType,
                                int level) implements RecipeInput {

    @Override
    public ItemStack getStackInSlot(int slot) {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }
}