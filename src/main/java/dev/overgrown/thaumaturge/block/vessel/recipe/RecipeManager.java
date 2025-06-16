package dev.overgrown.thaumaturge.block.vessel.recipe;

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

    public static boolean isCatalyst(Item item) {
        return recipes.stream().anyMatch(r -> r.getCatalyst() == item);
    }

    public static Optional<Recipe> findMatchingRecipe(ItemStack catalyst, AspectComponent aspects, VesselBlock.FluidType fluidType, int fluidLevel) {
        return recipes.stream()
                .filter(recipe -> recipe.matches(catalyst, aspects, fluidType, fluidLevel))
                .findFirst();
    }
}