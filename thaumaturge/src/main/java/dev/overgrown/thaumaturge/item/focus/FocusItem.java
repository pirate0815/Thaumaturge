package dev.overgrown.thaumaturge.item.focus;

import dev.overgrown.aspectslib.AspectsLib;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

/**
 * Contract for all three focus tiers (Lesser, Advanced, Greater).
 *
 * <h3>Data stored in NBT</h3>
 * <ul>
 *   <li>{@code "Modifier"}: String identifier of an AspectsLib {@link
 *       dev.overgrown.aspectslib.spell.modifier.SpellModifier} (defaults to
 *       {@code thaumaturge:stable}).</li>
 *   <li>{@code "SpellTree"}: The serialized spell node tree built in the
 *       Focal Manipulator.</li>
 *   <li>{@code "SpellComplexity"}: Cached total complexity of the spell tree.</li>
 * </ul>
 *
 * <p>The <em>aspect</em> itself is read from the stack's
 * {@link dev.overgrown.aspectslib.aspects.data.AspectData}, populated when an
 * aspect shard is placed into the focus via the Focal Manipulator.
 *
 * <h3>Focus tier -> complexity capacity</h3>
 * <p>Each tier limits the maximum complexity of patterns that can be inscribed.
 * Targeting and delivery are determined by the medium chain in the spell tree,
 * not by the focus tier.
 * <table border="1">
 *   <tr><th>Tier</th><th>Complexity Limit</th></tr>
 *   <tr><td>lesser</td><td>10</td></tr>
 *   <tr><td>advanced</td><td>25</td></tr>
 *   <tr><td>greater</td><td>50</td></tr>
 * </table>
 */
public interface FocusItem {

    /** Tiers: One of {@code "lesser"}, {@code "advanced"}, {@code "greater"}. */
    String getTier();

    /**
     * Returns the primary Aspect ID stored on this focus stack, or
     * {@code thaumaturge:null} if the focus is empty.
     *
     * <p>The default implementation reads the first aspect from the stack's
     * {@link dev.overgrown.aspectslib.aspects.data.AspectData}.
     */
    Identifier getAspect(ItemStack stack);

    /** Returns the AspectsLib modifier ID stored in NBT, or the stable default. */
    default Identifier getModifier(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains("Modifier")) {
            Identifier parsed = Identifier.tryParse(nbt.getString("Modifier"));
            if (parsed != null) return parsed;
        }
        return getDefaultModifier();
    }

    /** Stores the modifier ID in the stack's NBT. */
    default void setModifier(ItemStack stack, Identifier modifier) {
        stack.getOrCreateNbt().putString("Modifier",
                modifier != null ? modifier.toString() : getDefaultModifier().toString());
    }

    /** The default modifier used when none has been applied. */
    static Identifier getDefaultModifier() {
        return AspectsLib.identifier("stable");
    }
}