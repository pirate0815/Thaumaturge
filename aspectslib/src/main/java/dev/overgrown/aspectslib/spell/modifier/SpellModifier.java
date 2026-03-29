package dev.overgrown.aspectslib.spell.modifier;

import dev.overgrown.aspectslib.spell.SpellContext;
import dev.overgrown.aspectslib.spell.SpellMetadata;
import net.minecraft.util.Identifier;

/**
 * {@code SpellModifier} is the core interface for all spell modifiers.
 * A modifier can alter a spell in three stages:
 *
 * <ol>
 *   <li><b>Metadata modification</b> – before any other checks, the modifier
 *       can change the spell's {@link SpellMetadata} (potency, cost, range,
 *       stability, etc.).  This is done in {@link #modifyMetadata}.</li>
 *   <li><b>Pre‑execute hook</b> – right before the spell's main execution,
 *       {@link #onPreExecute} is called.  Modifiers can use this to spawn
 *       particles, play sounds, or store data in the context.</li>
 *   <li><b>Post‑execute hook</b> – after the spell has run,
 *       {@link #onPostExecute} is called with a flag indicating success.
 *       This is where cleanup or follow‑up effects (like echoes) can be
 *       triggered.</li>
 * </ol>
 *
 * <p>Modifiers are stateless singletons that are registered in the
 * {@link ModifierRegistry}.  Any per‑cast state must be stored in the
 * {@link SpellContext}'s data map.
 *
 * @see ModifierRegistry
 * @see SpellContext
 */
public interface SpellModifier {

    /**
     * Returns the unique identifier of this modifier (e.g.,
     * {@code aspectslib:power}).
     */
    Identifier getId();

    /**
     * Modifies the spell's metadata.  This method is called <em>before</em>
     * any other checks (including {@link dev.overgrown.aspectslib.spell.Spell#canCast}).
     * The modifier may mutate the given metadata object and should return it
     * (or a new instance) for further processing.
     *
     * <p>Typical uses:
     * <ul>
     *   <li>Increase potency ({@link SpellMetadata#POTENCY})</li>
     *   <li>Change Aether cost ({@link SpellMetadata#AETHER_COST})</li>
     *   <li>Adjust stability ({@link SpellMetadata#STABILITY})</li>
     *   <li>Set cast time or duration</li>
     * </ul>
     *
     * @param metadata the working copy of the spell's metadata
     * @param ctx      the current spell context (read‑only at this stage)
     * @return the (possibly modified) metadata
     */
    SpellMetadata modifyMetadata(SpellMetadata metadata, SpellContext ctx);

    /**
     * Called immediately before the spell's {@code execute} method, after
     * Aether has been consumed.  Modifiers can use this to perform actions
     * that must happen just before the spell effect, such as spawning
     * particles, playing sounds, or storing data in the context for later use.
     *
     * @param ctx the fully prepared spell context
     */
    void onPreExecute(SpellContext ctx);

    /**
     * Called after the spell's {@code execute} method, regardless of whether
     * it succeeded.  The {@code success} flag indicates whether the spell's
     * execution returned {@code true}.
     *
     * <p>Modifiers can use this to clean up temporary state, or to trigger
     * follow‑up effects (like {@code EchoModifier}).
     *
     * @param ctx     the spell context (no longer mutable)
     * @param success {@code true} if the spell executed successfully
     */
    void onPostExecute(SpellContext ctx, boolean success);
}