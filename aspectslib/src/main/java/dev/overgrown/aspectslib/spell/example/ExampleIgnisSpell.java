package dev.overgrown.aspectslib.spell.example;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.spell.Spell;
import dev.overgrown.aspectslib.spell.SpellContext;
import dev.overgrown.aspectslib.spell.SpellShape;
import dev.overgrown.aspectslib.spell.cost.ResonanceDiscountCalculator;
import dev.overgrown.aspectslib.spell.cost.SpellCostParams;
import dev.overgrown.aspectslib.spell.cost.SpellDuration;
import dev.overgrown.aspectslib.spell.cost.SpellRange;
import dev.overgrown.aspectslib.spell.math.SpellMath;
import dev.overgrown.aspectslib.spell.notation.TerminationCondition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * A fully worked example spell implementing the complete Codex spell system.
 *
 * <h3>Notation equivalent</h3>
 * <pre>
 *   Σ( IGN#5 ⊕ POT#3 ) → {FAR, LIVING} : [∂IGN+5 @target_surface] ⟨INSTANT⟩
 * </pre>
 *
 * <h3>Cost analysis (neutral environment)</h3>
 * <pre>
 *   C_base = (5×5) + (3×8) = 25 + 24 = 49 Aether
 *   R      = ×1.5 (FAR)
 *   D      = ×0.5 (INSTANT)
 *   K      = ×1.2 (2 Aspects)
 *   E      = ×1.0 (neutral env)
 *   A      = IGN→POT parent-child ⊕: −15 %
 *
 *   Total  = 49 × 1.5 × 0.5 × 1.2 × 1.0 × 0.85 ≈ 37 Aether
 * </pre>
 *
 * In a Volcanic Caldera (IGN amplified, E ≈ 0.7):
 * <pre>
 *   Total  = 49 × 1.5 × 0.5 × 1.2 × 0.7 × 0.85 ≈ 26 Aether
 * </pre>
 */
public final class ExampleIgnisSpell extends Spell {

    public static final Identifier ID = AspectsLib.identifier("example_ignis");

    private static final Identifier IGN = AspectsLib.identifier("ignis");
    private static final Identifier POT = AspectsLib.identifier("potentia");

    public ExampleIgnisSpell() {
        // POINT shape — the bolt itself is a projectile; AoE is determined
        // by the target acquisition in ConduitDispatcher, not by an AoE shape.
        super(SpellShape.point());
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    // ── Cost declaration ──────────────────────────────────────────────────────

    /**
     * Declares the Aspect inputs and parameters that drive the Aether cost.
     *
     * <p>Note: {@link ResonanceDiscountCalculator} auto-computes the IGN→POT
     * parent-child discount. The Ley Node flag is looked up from context here
     * as a demonstration; in practice you may query the chunk's Auram density
     * from {@link dev.overgrown.aspectslib.aether.AetherManager}.
     */
    @Override
    protected SpellCostParams buildCostParams(SpellContext ctx) {
        boolean atLeyNode = isAtLeyNode(ctx);
        double discount = ResonanceDiscountCalculator.compute(List.of(IGN, POT), atLeyNode);

        return new SpellCostParams.Builder()
                .aspect(IGN, 5)
                .aspect(POT, 3)
                .range(SpellRange.FAR)
                .duration(SpellDuration.INSTANT)
                // Environmental opposition — caller should compute from regional
                // Aspect ecology; 1.0 = neutral for this example
                .environmentOpposition(computeEnvironmentOpposition(ctx))
                .resonanceDiscount(discount)
                .build();
    }

    /**
     * INSTANT duration — no termination condition needed (Law III satisfied).
     */
    @Override
    public TerminationCondition createTerminationCondition() {
        return null; // INSTANT spells are exempt from Law III
    }

    // ── Execution ─────────────────────────────────────────────────────────────

    @Override
    public boolean execute(SpellContext ctx) {
        List<Entity> targets = ctx.getEntityTargets();
        if (targets.isEmpty()) return false;

        double potency    = ctx.getMetadata().getPotency();
        Vec3d  castOrigin = ctx.getCastOrigin();
        double maxRange   = ctx.getMetadata().getRange();

        int targetCount = 0;
        for (Entity target : targets) {
            if (!(target instanceof LivingEntity living)) continue;

            // Environmental Resonance from the EnvironmentalResonance object
            double amp     = ctx.getResonance().getAmplificationFactor(IGN);
            double barrier = ctx.getResonance().getBarrierCost(IGN);

            // Raw damage from potency, scaled, with falloff and resonance applied
            float rawDamage = SpellMath.applyResonance(
                    SpellMath.scale((float) potency, 2.0f, 0.5f, 20.0f), // base = potency×2, clamped [0.5, 20]
                    amp,
                    barrier
            );

            // Apply distance falloff (quadratic)
            float damage = SpellMath.applyFalloff(
                    rawDamage, castOrigin, target.getPos(),
                    maxRange, SpellMath.FalloffType.QUADRATIC
            );

            if (damage > 0) {
                living.setOnFireFor(3);
                living.damage(ctx.getWorld().getDamageSources().magic(), damage);
                targetCount++;
            }
        }

        return targetCount > 0;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private boolean isAtLeyNode(SpellContext ctx) {
        // A real implementation would check chunk Auram density against a threshold.
        // High Auram (e.g. > 200 current) indicates a Ley Node proximity.
        var chunkData = dev.overgrown.aspectslib.aether.AetherManager
                .getAetherData(ctx.getWorld(),
                        new net.minecraft.util.math.ChunkPos(
                                net.minecraft.util.math.BlockPos.ofFloored(ctx.getCastOrigin())));
        int auramCurrent = chunkData.getCurrentAether(AspectsLib.identifier("auram"));
        return auramCurrent >= 200;
    }

    private double computeEnvironmentOpposition(SpellContext ctx) {
        // Check for Gelum (Volatile pair with Ignis) in the environment
        var chunkData = dev.overgrown.aspectslib.aether.AetherManager
                .getAetherData(ctx.getWorld(),
                        new net.minecraft.util.math.ChunkPos(
                                net.minecraft.util.math.BlockPos.ofFloored(ctx.getCastOrigin())));
        int gelumLevel = chunkData.getCurrentAether(AspectsLib.identifier("gelum"));
        int ignisLevel = chunkData.getCurrentAether(AspectsLib.identifier("ignis"));

        if (gelumLevel == 0 && ignisLevel > 0) {
            // Ignis-amplifying environment (e.g. volcanic) → E < 1.0
            return 0.7;
        } else if (gelumLevel > ignisLevel) {
            // Heavy Gelum opposition → E 2.0 or 3.0
            return gelumLevel > ignisLevel * 2 ? 3.0 : 2.0;
        } else if (gelumLevel > 0) {
            return 1.5; // moderate opposition
        }
        return 1.0; // neutral
    }
}