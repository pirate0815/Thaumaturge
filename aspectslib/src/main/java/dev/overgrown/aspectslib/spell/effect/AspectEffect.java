package dev.overgrown.aspectslib.spell.effect;

import dev.overgrown.aspectslib.spell.SpellContext;
import dev.overgrown.aspectslib.spell.networking.SpellCastC2SPacket.CastMode;

/**
 * Defines the per-aspect spell effect applied when a Focus containing the
 * corresponding aspect is cast.
 *
 * <p>Implementations are registered in {@link AspectEffectRegistry} and keyed
 * by the Aspect's {@link net.minecraft.util.Identifier}. Registration is
 * performed by consuming mods (e.g., Thaumaturge) during their own init.
 */
public interface AspectEffect {

    /**
     * Applies this effect using the provided spell context and cast mode.
     *
     * @param ctx  the fully-resolved casting context (metadata is already
     *             modifier-adjusted and resonance-scaled)
     * @param mode whether the cast was SELF, TARGETED, or AOE
     * @return {@code true} if the effect did something meaningful;
     *         {@code false} if it was a no-op (no valid targets, etc.)
     */
    boolean apply(SpellContext ctx, CastMode mode);
}