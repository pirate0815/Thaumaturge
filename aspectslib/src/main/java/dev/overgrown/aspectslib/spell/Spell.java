package dev.overgrown.aspectslib.spell;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.aether.AetherAPI;
import dev.overgrown.aspectslib.aspects.data.AspectData;
import dev.overgrown.aspectslib.spell.aether.PersonalAetherPool;
import dev.overgrown.aspectslib.spell.cost.AetherCostCalculator;
import dev.overgrown.aspectslib.spell.cost.SpellCostParams;
import dev.overgrown.aspectslib.spell.cost.SpellDuration;
import dev.overgrown.aspectslib.spell.cost.SpellRange;
import dev.overgrown.aspectslib.spell.law.SpellLawValidator;
import dev.overgrown.aspectslib.spell.modifier.SpellModifier;
import dev.overgrown.aspectslib.spell.notation.NotationFormula;
import dev.overgrown.aspectslib.spell.notation.TerminationCondition;
import dev.overgrown.aspectslib.spell.unraveling.UnravelingTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * Abstract base class for every spell in AspectsLib.
 *
 * <h2>Lifecycle of a single cast (updated)</h2>
 * <ol>
 *   <li>The conduit builds a {@link SpellContext} and calls {@link #cast}.</li>
 *   <li>Modifier metadata transforms are applied in order.</li>
 *   <li>The Aether cost is (re)computed by {@link AetherCostCalculator} using
 *       the spell's declared {@link SpellCostParams}.</li>
 *   <li>The {@link SpellLawValidator} checks all six Universal Laws:
 *       <ul>
 *         <li>Hard violations abort the cast immediately.</li>
 *         <li>Soft violations apply consequences but allow the cast to proceed.</li>
 *       </ul>
 *   </li>
 *   <li>{@link #canCast} is called for spell-specific preconditions.</li>
 *   <li>Aether is consumed: ambient portion from the chunk, personal portion
 *       from the caster's {@link PersonalAetherPool}.</li>
 *   <li>{@link SpellModifier#onPreExecute} runs for all modifiers.</li>
 *   <li>{@link #execute} is called.</li>
 *   <li>{@link SpellModifier#onPostExecute} runs for all modifiers.</li>
 *   <li>Unraveling stress is recorded if the caster's pool is under pressure.</li>
 * </ol>
 *
 * <h2>Implementing a spell — minimal example</h2>
 * <pre>{@code
 * public final class FireboltSpell extends Spell {
 *     public static final Identifier ID = AspectsLib.identifier("firebolt");
 *
 *     public FireboltSpell() {
 *         super(SpellShape.point());
 *     }
 *
 *     {@literal @}Override public Identifier getId() { return ID; }
 *
 *     {@literal @}Override
 *     protected SpellCostParams buildCostParams(SpellContext ctx) {
 *         return new SpellCostParams.Builder()
 *             .aspect(AspectsLib.identifier("ignis"),   5)
 *             .aspect(AspectsLib.identifier("potentia"), 3)
 *             .range(SpellRange.FAR)
 *             .duration(SpellDuration.INSTANT)
 *             .build();
 *     }
 *
 *     {@literal @}Override
 *     public TerminationCondition createTerminationCondition() {
 *         return null; // INSTANT — no termination needed
 *     }
 *
 *     {@literal @}Override
 *     public boolean execute(SpellContext ctx) {
 *         ctx.getEntityTargets().forEach(e -> e.setOnFireFor(5));
 *         return true;
 *     }
 * }
 * }</pre>
 */
public abstract class Spell {

    /** Shape of the spell's area of effect. */
    private final SpellShape shape;

    protected Spell(SpellShape shape) {
        if (shape == null) throw new IllegalArgumentException("shape must not be null");
        this.shape = shape;
    }

    protected Spell() {
        this(SpellShape.point());
    }

    /** Unique registry identifier, e.g. {@code AspectsLib.identifier("firebolt")}. */
    public abstract Identifier getId();

    /**
     * Runs the spell's effect. Called after all law checks and Aether draws.
     *
     * @return {@code true} if the spell produced an effect; {@code false} for no-op.
     *         A {@code false} return does <em>not</em> refund Aether.
     */
    public abstract boolean execute(SpellContext ctx);

    /**
     * Builds the {@link SpellCostParams} that drive the full Aether cost formula.
     *
     * <p>Override this (rather than hard-coding {@code aether_cost}) so the
     * calculator can account for tier multipliers, range, duration, complexity,
     * environmental opposition, and resonance discounts automatically.
     *
     * <p>Default: a minimal single-Primal-Aer-#1, NEAR, INSTANT spell (cost ≈ 5).
     * Subclasses should always override this.
     */
    protected SpellCostParams buildCostParams(SpellContext ctx) {
        return new SpellCostParams.Builder()
                .aspect(AspectsLib.identifier("aer"), 1)
                .range(SpellRange.NEAR)
                .duration(SpellDuration.INSTANT)
                .build();
    }

    /**
     * Returns the {@link TerminationCondition} for sustained spells.
     *
     * <p>Returns {@code null} for INSTANT spells (no condition needed).
     * For any non-INSTANT spell this <em>must</em> return a non-null condition
     * or Law III will produce a validation error.
     *
     * <p>The returned condition is attached to the {@link SpellContext} as
     * {@code "notation_formula"} data for downstream use by the
     * {@link SpellLawValidator}.
     */
    public TerminationCondition createTerminationCondition() {
        return null;
    }

    /**
     * Called after Law validation and Aether draw to perform spell-specific
     * precondition checks (cooldowns, reagents, etc.).
     *
     * <p>The default implementation returns {@code true} unconditionally.
     * Override for spell-specific gates.
     */
    public boolean canCast(SpellContext ctx) {
        return true;
    }

    /**
     * Called when {@link #canCast} returns {@code false} due to stability misfire.
     * Override to implement backlash effects (deal damage, spawn Vitium, etc.).
     */
    protected void onMisfire(SpellContext ctx) {}

    /**
     * Returns the {@link AspectData} describing which Aspects this spell
     * preferentially draws from the environment. The default is empty
     * (proportional draw across all available Aspects).
     */
    public AspectData getRequiredAspects() {
        return AspectData.DEFAULT;
    }

    /** Human-readable display name (used in tooltips). */
    public String getDisplayName() {
        return getId().getPath();
    }

    /**
     * Executes the complete cast pipeline. Always call this, never call
     * {@link #execute} directly.
     *
     * @param ctx a context whose metadata has NOT yet been modifier-adjusted
     * @return {@code true} if the spell executed successfully
     */
    public final boolean cast(SpellContext ctx) {

        // ── Step 1: Apply modifier metadata transforms ────────────────────────
        SpellMetadata workingMeta = ctx.getMetadata().copy();
        for (SpellModifier mod : ctx.getModifiers()) {
            workingMeta = mod.modifyMetadata(workingMeta, ctx);
        }
        ctx.getMetadata().importFrom(workingMeta);

        // ── Step 2: Compute full Aether cost via formula ──────────────────────
        SpellCostParams costParams = buildCostParams(ctx);
        double computedCost = AetherCostCalculator.compute(costParams);
        ctx.getMetadata().set(SpellMetadata.AETHER_COST, computedCost);

        // ── Step 3: Attach notation formula / termination condition ───────────
        TerminationCondition termination = createTerminationCondition();
        if (termination != null) {
            // Wrap in a minimal NotationFormula so the Law validator can read it
            NotationFormula formula = buildNotationFormula(costParams, termination);
            ctx.putData("notation_formula", formula);
        }

        // ── Step 4: Universal Law validation ──────────────────────────────────
        SpellLawValidator.ValidationReport lawReport = SpellLawValidator.validate(ctx);
        if (lawReport.hasHardBlock()) {
            for (SpellLawValidator.Violation v : lawReport.hardViolations()) {
                AspectsLib.LOGGER.warn("[{}] Law violation: {} — {}", getId(), v.law(), v.message());
                notifyCaster(ctx, v.message());
            }
            return false;
        }
        // Soft violations already have consequences applied by the validator

        // ── Step 5: Stability / misfire check ────────────────────────────────
        double stability = ctx.getMetadata().getStability();
        if (stability < 1.0 && ctx.getWorld().random.nextDouble() > stability) {
            notifyCaster(ctx, Text.translatable("spell.aspectslib.fail.misfire").getString());
            onMisfire(ctx);
            return false;
        }

        // ── Step 6: Spell-specific canCast check ──────────────────────────────
        if (!canCast(ctx)) return false;

        // ── Step 7: Consume Aether (ambient + personal split) ─────────────────
        boolean aetherConsumed = consumeAether(ctx, computedCost);
        if (!aetherConsumed) {
            notifyCaster(ctx, Text.translatable("spell.aspectslib.fail.no_aether").getString());
            return false;
        }

        // ── Step 8: Pre-execute hooks ─────────────────────────────────────────
        for (SpellModifier mod : ctx.getModifiers()) {
            mod.onPreExecute(ctx);
        }

        // ── Step 9: Execute ───────────────────────────────────────────────────
        boolean succeeded = false;
        try {
            succeeded = execute(ctx);
        } finally {
            // ── Step 10: Post-execute hooks (always run) ─────────────────────
            for (SpellModifier mod : ctx.getModifiers()) {
                mod.onPostExecute(ctx, succeeded);
            }
            // ── Step 11: Record unraveling stress if pool is stressed ─────────
            if (ctx.getCaster() instanceof PersonalAetherPool pool) {
                PersonalAetherPool.PoolState state = pool.aspectslib$getPoolState();
                if (state == PersonalAetherPool.PoolState.CRITICAL
                        || state == PersonalAetherPool.PoolState.EXHAUSTED) {
                    float overdrawFraction = state == PersonalAetherPool.PoolState.EXHAUSTED
                            ? 0.1f
                            : 0.03f;
                    UnravelingTracker.recordOverdraw(ctx.getCaster(), overdrawFraction);
                }
            }
        }

        return succeeded;
    }

    /** Returns the base metadata (before modifier application). */
    public SpellMetadata getBaseMetadata() {
        return SpellMetadata.DEFAULT.copy();
    }

    public SpellShape getShape() {
        return shape;
    }

    /**
     * Draws the computed Aether cost from the ambient field (up to 70%) and
     * the caster's Personal Aether pool (remainder).
     */
    private boolean consumeAether(SpellContext ctx, double totalCost) {
        if (totalCost <= 0) return true;

        BlockPos origin   = BlockPos.ofFloored(ctx.getCastOrigin());

        // Compute ambient fraction from chunk density
        double ambientFraction = computeAmbientFraction(ctx);
        double[] split = AetherCostCalculator.personalAetherDraw(totalCost, ambientFraction);
        double personalDraw = split[0];
        double ambientDraw  = split[1];

        // Check personal pool availability
        if (ctx.getCaster() instanceof PersonalAetherPool pool) {
            if (pool.aspectslib$getPersonalAether() < personalDraw) {
                return false; // Not enough personal Aether
            }
        }

        // Check ambient availability (use flat total since we don't require specific Aspects)
        if (ambientDraw > 0 && !AetherAPI.hasTotalAether(ctx.getWorld(), origin, ambientDraw)) {
            // Fall back to drawing everything from Personal Aether
            if (ctx.getCaster() instanceof PersonalAetherPool pool) {
                if (pool.aspectslib$getPersonalAether() < totalCost) return false;
                pool.aspectslib$drawPersonalAether(totalCost);
                return true;
            }
            return false;
        }

        // Draw ambient portion
        if (ambientDraw > 0) {
            AetherAPI.castSpell(ctx.getWorld(), origin, buildFlatCostData(ambientDraw));
        }

        // Draw personal portion
        if (personalDraw > 0 && ctx.getCaster() instanceof PersonalAetherPool pool) {
            pool.aspectslib$drawPersonalAether(personalDraw);
        }

        return true;
    }

    private double computeAmbientFraction(SpellContext ctx) {
        BlockPos origin = BlockPos.ofFloored(ctx.getCastOrigin());
        var chunkData = dev.overgrown.aspectslib.aether.AetherManager
                .getAetherData(ctx.getWorld(), new net.minecraft.util.math.ChunkPos(origin));
        double totalCurrent = 0, totalMax = 0;
        for (var id : chunkData.getAspectIds()) {
            totalCurrent += chunkData.getCurrentAether(id);
            totalMax     += chunkData.getMaxAether(id);
        }
        if (totalMax == 0) return 0.0;
        return Math.min(1.0, totalCurrent / totalMax);
    }

    /**
     * Builds a minimal AspectData representing a flat Aether draw
     * (no specific Aspect requirement - proportional draw).
     */
    private AspectData buildFlatCostData(double amount) {
        // Use Aer as a proxy for a flat undifferentiated draw.
        // AetherAPI.castSpell distributes proportionally when given
        // an Aspect that may not be present; downstream can override
        // by setting getRequiredAspects().
        var required = getRequiredAspects();
        return required.isEmpty() ? AspectData.DEFAULT : required;
    }

    /**
     * Wraps the cost params and termination condition into a minimal
     * {@link NotationFormula} for Law III validation.
     */
    private NotationFormula buildNotationFormula(SpellCostParams params, TerminationCondition tc) {
        NotationFormula.Builder b = new NotationFormula.Builder()
                .range(params.getRange())
                .duration(params.getDuration())
                .termination(tc);
        params.getAspectIntensities().forEach((id, intensity) ->
                b.input(id, intensity, NotationFormula.ResonanceOperator.SIMULTANEOUS));
        return b.build();
    }

    private static void notifyCaster(SpellContext ctx, String message) {
        if (ctx.getCaster() instanceof ServerPlayerEntity player) {
            player.sendMessage(Text.literal(message), true);
        }
    }

    @Override
    public String toString() {
        return "Spell{id=" + getId() + ", shape=" + shape + "}";
    }
}