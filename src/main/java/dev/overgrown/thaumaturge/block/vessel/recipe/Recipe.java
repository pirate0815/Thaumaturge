package dev.overgrown.thaumaturge.block.vessel.recipe;

import dev.overgrown.thaumaturge.block.vessel.VesselBlock;
import dev.overgrown.thaumaturge.component.AspectComponent;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import dev.overgrown.thaumaturge.data.Aspect;

public class Recipe {
    private final Item catalyst;
    private final Object2IntMap<RegistryEntry<Aspect>> requiredAspects;
    private final ItemStack output;
    private final VesselBlock.FluidType requiredFluidType;
    private final int requiredFluidLevel;

    public Recipe(Item catalyst, Object2IntMap<RegistryEntry<Aspect>> requiredAspects, ItemStack output, VesselBlock.FluidType requiredFluidType, int requiredFluidLevel) {
        this.catalyst = catalyst;
        this.requiredAspects = new Object2IntOpenHashMap<>(requiredAspects);
        this.output = output;
        this.requiredFluidType = requiredFluidType;
        this.requiredFluidLevel = requiredFluidLevel;
    }

    public boolean matches(ItemStack catalystStack, AspectComponent vesselAspects, VesselBlock.FluidType currentFluidType, int currentFluidLevel) {
        if (catalystStack.getItem() != catalyst) return false;
        if (requiredFluidType != null && requiredFluidType != currentFluidType) return false;
        if (currentFluidLevel < requiredFluidLevel) return false;
        for (Object2IntMap.Entry<RegistryEntry<Aspect>> entry : requiredAspects.object2IntEntrySet()) {
            if (vesselAspects.getMap().getInt(entry.getKey()) < entry.getIntValue()) return false;
        }
        return true;
    }

    public ItemStack getOutput() {
        return output.copy();
    }

    public Item getCatalyst() {
        return catalyst;
    }

    public Object2IntMap<RegistryEntry<Aspect>> getRequiredAspects() {
        return new Object2IntOpenHashMap<>(requiredAspects);
    }

    public static class Builder {
        private Item catalyst;
        private final Object2IntOpenHashMap<RegistryEntry<Aspect>> requiredAspects = new Object2IntOpenHashMap<>();
        private ItemStack output = ItemStack.EMPTY;
        private VesselBlock.FluidType requiredFluidType = null;
        private int requiredFluidLevel = 0;

        public Builder requiresFluid(VesselBlock.FluidType type, int level) {
            this.requiredFluidType = type;
            this.requiredFluidLevel = level;
            return this;
        }

        public Builder catalyst(Item catalyst) {
            this.catalyst = catalyst;
            return this;
        }

        public Builder requires(RegistryEntry<Aspect> aspect, int amount) {
            requiredAspects.put(aspect, amount);
            return this;
        }

        public Builder output(ItemStack output) {
            this.output = output;
            return this;
        }

        public Recipe build() {
            if (catalyst == null) throw new IllegalStateException("Recipe must have a catalyst");
            if (output.isEmpty()) throw new IllegalStateException("Recipe must have an output");
            return new Recipe(catalyst, requiredAspects, output, requiredFluidType, requiredFluidLevel);
        }

        public void register() {
            RecipeManager.addRecipe(build());
        }
    }
}