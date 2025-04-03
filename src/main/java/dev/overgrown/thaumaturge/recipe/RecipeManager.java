package dev.overgrown.thaumaturge.recipe;

import dev.overgrown.thaumaturge.block.vessel.VesselBlock;
import dev.overgrown.thaumaturge.component.AspectComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecipeManager {
    private static final List<Recipe> recipes = new ArrayList<>();

    public static void addRecipe(Recipe recipe) {
        recipes.add(recipe);
    }

    public static void removeRecipe(Recipe recipe) {
        recipes.remove(recipe);
    }

    public static boolean isCatalyst(Item item) {
        return recipes.stream().anyMatch(recipe -> recipe.getCatalyst() == item);
    }

    public static Optional<Recipe> findMatchingRecipe(ItemStack catalystStack, AspectComponent totalAspects, VesselBlock.FluidType fluidType, int fluidLevel) {
        return recipes.stream()
                .filter(recipe -> recipe.matches(catalystStack, totalAspects, fluidType, fluidLevel))
                .findFirst();
    }
}