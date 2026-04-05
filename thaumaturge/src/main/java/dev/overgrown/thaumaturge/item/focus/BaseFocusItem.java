package dev.overgrown.thaumaturge.item.focus;

import dev.overgrown.aspectslib.aspects.api.AspectsAPI;
import dev.overgrown.aspectslib.aspects.data.AspectData;
import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.spell.focal.SpellNode;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

/**
 * Shared base logic for all three focus tiers.
 * Subclass and implement {@link #getTier()}.
 */
abstract class BaseFocusItem extends Item implements FocusItem {

    protected BaseFocusItem(Settings settings) { super(settings); }

    @Override
    public Identifier getAspect(ItemStack stack) {
        // Try AspectData first (legacy / shard-based)
        AspectData data = AspectsAPI.getAspectData(stack);
        if (!data.isEmpty()) {
            return data.getAspectIds().iterator().next();
        }
        // Fall back to extracting the first EFFECT from the SpellTree
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains("SpellTree")) {
            SpellNode tree = SpellNode.fromNbt(nbt.getCompound("SpellTree"));
            if (tree != null) {
                Identifier effectId = tree.findFirstEffect();
                if (effectId != null) return effectId;
            }
        }
        return Thaumaturge.identifier("null");
    }
}