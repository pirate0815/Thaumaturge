package dev.overgrown.aspectslib.spell;

import dev.overgrown.aspectslib.aether.AetherAPI;
import dev.overgrown.aspectslib.aspects.data.AspectData;
import dev.overgrown.aspectslib.spell.modifier.SpellModifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * {@code Spell} is the abstract base class for every spell.
 *
 * <h3>Lifecycle of a single cast</h3>
 * <ol>
 *   <li>The conduit (gauntlet / wand / etc.) resolves which spell to fire and
 *       builds a {@link SpellContext} — including the caster, targets, and any
 *       active {@link SpellModifier}s.</li>
 *   <li>Each modifier's {@link SpellModifier#modifyMetadata} is called in
 *       order, layering stat changes onto a working copy of the spell's base
 *       {@link SpellMetadata}.</li>
 *   <li>{@link #canCast(SpellContext)} is called. If it returns {@code false}
 *       the cast is rejected and feedback is sent to the caster.</li>
 *   <li>Each modifier's {@link SpellModifier#onPreExecute} hook runs.</li>
 *   <li>{@link #execute(SpellContext)} is called — this is where the spell
 *       does its work. It returns {@code true} on success.</li>
 *   <li>Each modifier's {@link SpellModifier#onPostExecute} hook runs.</li>
 * </ol>
 *
 * <h3>Implementing a spell</h3>
 * <pre>{@code
 * public final class IgnisSpell extends Spell {
 *     public static final Identifier ID = AspectsLib.identifier("ignis");
 *
 *     public IgnisSpell() {
 *         super(new SpellMetadata.Builder()
 *                 .potency(2.0)
 *                 .aetherCost(15.0)
 *                 .range(16)
 *                 .build(),
 *             SpellShape.sphere(3.0f));
 *     }
 *
 *     @Override public Identifier getId() { return ID; }
 *
 *     @Override
 *     public boolean execute(SpellContext ctx) {
 *         // deal fire damage to all entity targets
 *         ctx.getEntityTargets().forEach(e -> e.setOnFireFor(5));
 *         return true;
 *     }
 * }
 * }</pre>
 */
public abstract class Spell {

    // Fields
    /** Base stats before any modifier is applied. */
    private final SpellMetadata baseMetadata;

    /**
     * The default geometric area this spell operates in. Individual
     * implementations may ignore this (e.g. targeted-only spells) or use it
     * to collect entity/block targets automatically.
     */
    private final SpellShape shape;

    // Constructor
    /**
     * @param baseMetadata the unmodified stat block for this spell
     * @param shape        the default area-of-effect shape
     */
    protected Spell(SpellMetadata baseMetadata, SpellShape shape) {
        if (baseMetadata == null) throw new IllegalArgumentException("baseMetadata must not be null");
        if (shape        == null) throw new IllegalArgumentException("shape must not be null");
        this.baseMetadata = baseMetadata;
        this.shape        = shape;
    }

    /** Convenience constructor for spells that use a single-point shape. */
    protected Spell(SpellMetadata baseMetadata) {
        this(baseMetadata, SpellShape.point());
    }

    // Abstract contract
    /**
     * The unique registry identifier for this spell type, e.g.
     * {@code AspectsLib.identifier("ignis")}.
     */
    public abstract Identifier getId();

    /**
     * Runs the spell's effect using the fully-prepared {@code ctx}.
     *
     * <p>By the time this is called:
     * <ul>
     *   <li>All modifier metadata adjustments have been applied.</li>
     *   <li>{@link #canCast} has returned {@code true}.</li>
     *   <li>Aether has been consumed from the environment.</li>
     * </ul>
     *
     * @param ctx fully-populated casting context
     * @return {@code true} if the spell produced an effect; {@code false} if
     *         it was a no-op (e.g. no valid targets were found).  A return
     *         value of {@code false} does <em>not</em> refund Aether.
     */
    public abstract boolean execute(SpellContext ctx);

    // Overridable hooks
    /**
     * Called before {@link #execute} to determine whether the cast is legal.
     *
     * <p>The default implementation checks:
     * <ol>
     *   <li>The casting chunk is not a dead zone.</li>
     *   <li>The chunk has enough total Aether to cover the (modified) cost.</li>
     *   <li>The stability check passes (random roll against stability stat).</li>
     * </ol>
     *
     * Override to add spell-specific preconditions (cooldowns, reagents, etc.).
     *
     * @param ctx context containing the already-modified metadata
     * @return {@code true} if the spell may proceed
     */
    public boolean canCast(SpellContext ctx) {
        BlockPos origin = BlockPos.ofFloored(ctx.getCastOrigin());

        // 1. Dead-zone guard
        if (AetherAPI.isDeadZone(ctx.getWorld(), origin)) {
            notifyCaster(ctx, "spell.aspectslib.fail.dead_zone");
            return false;
        }

        // 2. Aether availability
        double cost = ctx.getMetadata().getAetherCost();
        if (!AetherAPI.hasTotalAether(ctx.getWorld(), origin, cost)) {
            notifyCaster(ctx, "spell.aspectslib.fail.no_aether");
            return false;
        }

        // 3. Stability / misfire check
        double stability = ctx.getMetadata().getStability();
        if (stability < 1.0 && ctx.getWorld().random.nextDouble() > stability) {
            notifyCaster(ctx, "spell.aspectslib.fail.misfire");
            onMisfire(ctx);
            return false;
        }

        return true;
    }

    /**
     * Called by the default {@link #canCast} implementation when a stability
     * check fails (misfire).  Override to implement backlash effects such as
     * dealing damage to the caster or spawning a Vitium explosion.
     *
     * <p>The default implementation does nothing beyond the failure message
     * already sent by {@code canCast}.
     *
     * @param ctx the casting context at the time of misfire
     */
    protected void onMisfire(SpellContext ctx) {
        // Default: no backlash. Subclasses may override.
    }

    /**
     * Returns the {@link AspectData} that describes which aspects (and how
     * much of each) this spell draws from the environment beyond the flat
     * Aether cost.  The default returns an empty AspectData (no specific
     * aspect requirement).
     *
     * <p>Override when your spell has hard aspect requirements, e.g. an Ignis
     * spell that <em>must</em> draw from Ignis in the environment.
     */
    public AspectData getRequiredAspects() {
        return AspectData.DEFAULT;
    }

    /**
     * Human-readable name shown in tooltips and command output.  Defaults to
     * the path segment of {@link #getId()}.
     */
    public String getDisplayName() {
        return getId().getPath();
    }

    // Full cast pipeline
    /**
     * Runs the full cast pipeline described in the class Javadoc.
     *
     * <p>Callers (conduit items, command handlers) should call this method
     * rather than {@link #execute} directly so that modifiers and Aether
     * consumption are handled correctly.
     *
     * @param ctx a context whose metadata has <em>not</em> yet been modified
     *            by the active modifiers (this method applies them)
     * @return {@code true} if the spell executed successfully
     */
    public final boolean cast(SpellContext ctx) {
        // 1. Apply modifier metadata transforms in order.
        //    Each modifier receives the running copy and may return either the
        //    same (mutated) object or a fresh one - both patterns are valid.
        SpellMetadata workingMeta = baseMetadata.copy();
        for (SpellModifier modifier : ctx.getModifiers()) {
            workingMeta = modifier.modifyMetadata(workingMeta, ctx);
        }
        // Push the modifier-adjusted stats back into ctx.getMetadata() in-place.
        // We cannot replace ctx.metadata (it is final), but SpellMetadata is
        // mutable, so importFrom() copies every entry from workingMeta -> ctx.
        ctx.getMetadata().importFrom(workingMeta);

        // 2. Precondition check
        if (!canCast(ctx)) return false;

        // 3. Consume Aether
        double cost = ctx.getMetadata().getAetherCost();
        BlockPos origin = BlockPos.ofFloored(ctx.getCastOrigin());
        if (cost > 0) {
            AetherAPI.castSpell(ctx.getWorld(), origin,
                    buildAetherCostData(cost));
        }

        // 4. Pre-execute hooks
        for (SpellModifier modifier : ctx.getModifiers()) {
            modifier.onPreExecute(ctx);
        }

        // 5. Execute
        boolean succeeded = false;
        try {
            succeeded = execute(ctx);
        } finally {
            // 6. Post-execute hooks (always run, even on exception)
            for (SpellModifier modifier : ctx.getModifiers()) {
                modifier.onPostExecute(ctx, succeeded);
            }
        }

        return succeeded;
    }

    // Accessors
    /** Returns the unmodified base metadata. */
    public SpellMetadata getBaseMetadata() {
        return baseMetadata;
    }

    /** Returns the default area-of-effect shape. */
    public SpellShape getShape() {
        return shape;
    }

    // Internal helpers
    /**
     * Sends a translatable failure message to the caster if they are a
     * player.
     */
    private static void notifyCaster(SpellContext ctx, String translationKey) {
        if (ctx.getCaster() instanceof ServerPlayerEntity player) {
            player.sendMessage(Text.translatable(translationKey), true);
        }
    }

    /**
     * Builds a minimal {@link AspectData} that represents a flat Aether cost
     * of {@code totalRU} units.  We use the spell's required aspects if any
     * are declared; otherwise we let {@code AetherAPI#castSpell} distribute
     * the draw proportionally across whatever aspects the chunk holds.
     *
     * <p>Currently returns {@link AspectData#DEFAULT} so the caller's
     * proportional-draw logic in {@code AetherAPI} handles everything.
     */
    private AspectData buildAetherCostData(double totalRU) {
        // Flat cost - the AetherAPI's proportional draw handles distribution.
        // Subclasses that require specific aspects should override canCast and
        // consume them explicitly via AetherAPI.castSpell(world, pos, aspectCost).
        return AspectData.DEFAULT;
    }

    // Object
    @Override
    public String toString() {
        return "Spell{id=" + getId() + ", shape=" + shape + "}";
    }
}