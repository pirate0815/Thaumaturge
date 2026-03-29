package dev.overgrown.aspectslib.spell.conduit;

import dev.overgrown.aspectslib.spell.Spell;
import dev.overgrown.aspectslib.spell.SpellContext;
import dev.overgrown.aspectslib.spell.SpellRegistry;
import dev.overgrown.aspectslib.spell.modifier.SpellModifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * A {@code Conduit} is the only type of item through which spells can be cast.
 * Any {@link net.minecraft.item.Item} implementation may
 * become a conduit by implementing this interface.
 *
 * <h3>Contract</h3>
 * <ul>
 *   <li>The spell system checks {@link #canCast} before attempting to cast.
 *       If it returns {@code false} the cast is silently refused.</li>
 *   <li>{@link #getStoredSpell} returns the spell currently "loaded" into
 *       this conduit (from NBT or item design).</li>
 *   <li>{@link #getStoredModifiers} returns the modifier list that should be
 *       applied to any spell cast through this conduit.</li>
 *   <li>{@link #onSpellCast} is called after a successful cast so the conduit
 *       can apply durability damage, cooldowns, or visual feedback.</li>
 * </ul>
 *
 * <h3>Example implementation skeleton</h3>
 * <pre>{@code
 * public class WandItem extends Item implements IConduit {
 *     \@Override
 *     public boolean canCast(ItemStack stack, LivingEntity caster) {
 *         return !stack.isDamaged() || stack.getDamage() < stack.getMaxDamage();
 *     }
 *     \@Override
 *     public void onSpellCast(ItemStack stack, SpellContext ctx) {
 *         stack.damage(1, ctx.getCaster(), e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
 *     }
 *     // ... other overrides
 * }
 * }</pre>
 */
public interface IConduit {

    /**
     * Returns {@code true} if this conduit is currently capable of casting a
     * spell (not broken, charged, cooldown elapsed, etc.).
     */
    boolean canCast(ItemStack stack, LivingEntity caster);

    /**
     * Returns the identifier of the spell stored/bound to this conduit, or
     * {@code null} if none.
     */
    Identifier getStoredSpellId(ItemStack stack);

    /**
     * Resolves the stored spell identifier against the {@link SpellRegistry} and
     * returns the {@link Spell} instance, or {@code null} when not found.
     */
    default Spell getStoredSpell(ItemStack stack) {
        Identifier id = getStoredSpellId(stack);
        if (id == null) return null;
        return SpellRegistry.get(id).orElse(null);
    }

    /**
     * Returns the ordered list of {@link SpellModifier}s that this conduit
     * contributes to every cast.  May be empty; never null.
     */
    List<SpellModifier> getStoredModifiers(ItemStack stack);

    /**
     * Called by the spell system after a successful cast.  Use it to apply
     * durability damage, start a cooldown, spawn particles, etc.
     *
     * <p>The default implementation does nothing.
     */
    default void onSpellCast(ItemStack stack, SpellContext ctx) {}

    /**
     * Optional: returns a display name for the bound spell shown in tooltips.
     * Default delegates to {@link Spell#getDisplayName()} when the spell is
     * resolved.
     */
    default String getSpellDisplayName(ItemStack stack) {
        Spell spell = getStoredSpell(stack);
        return spell != null ? spell.getDisplayName() : "—";
    }
}