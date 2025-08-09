package dev.overgrown.thaumaturge.item.focus;

import dev.overgrown.thaumaturge.registry.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public interface FocusItem {
    String getTier();
    Identifier getAspect(ItemStack stack);
    void setModifier(ItemStack stack, Identifier modifier);

    static ItemStack createFocus(Identifier aspect, Identifier modifier) {
        // Create the focus item stack (using advanced focus as default)
        ItemStack stack = new ItemStack(ModItems.ADVANCED_FOCUS);
        NbtCompound nbt = stack.getOrCreateNbt();

        // Correct NBT structure
        NbtCompound aspectsLibData = new NbtCompound();
        NbtCompound aspectData = new NbtCompound();

        // Store aspect/modifier directly
        aspectData.putString("Aspect", aspect.toString());
        aspectData.putString("Modifier", modifier.toString());

        aspectsLibData.put("AspectData", aspectData);
        nbt.put("AspectsLibData", aspectsLibData);

        return stack;
    }

    // Add a default modifier getter
    static Identifier getDefaultModifier() {
        return new Identifier("thaumaturge", "stable");
    }

    // Removed @Override annotation and added null check
    default Identifier getModifier(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains("Modifier")) {
            return new Identifier(nbt.getString("Modifier"));
        }
        return getDefaultModifier();
    }
}