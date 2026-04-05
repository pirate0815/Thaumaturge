package dev.overgrown.aspectslib.spell.law;

import dev.overgrown.aspectslib.aether.AetherManager;
import dev.overgrown.aspectslib.spell.SpellContext;
import dev.overgrown.aspectslib.spell.SpellMetadata;
import dev.overgrown.aspectslib.spell.aether.PersonalAetherPool;
import dev.overgrown.aspectslib.spell.notation.NotationFormula;
import dev.overgrown.aspectslib.spell.resonance.VolatileResonancePair;
import dev.overgrown.aspectslib.spell.resonance.VolatileResonanceRegistry;
import dev.overgrown.aspectslib.spell.unraveling.UnravelingStage;
import dev.overgrown.aspectslib.spell.unraveling.UnravelingTracker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Enforces the six Universal Laws of Spellcraft as runtime checks during the casting pipeline.
 *
 * <p>Call {@link #validate(SpellContext)} before executing any spell.  The result
 * lists any Law violations and whether the cast should be blocked ({@code hard}
 * violations) or allowed with consequences ({@code soft} violations).
 *
 * <h3>The six laws</h3>
 * <ol>
 *   <li><b>Conservation</b> — Aspects are never created from nothing.
 *       The environment or the caster must supply them (checked via Aether availability).</li>
 *   <li><b>Fames</b> — Every spell has a cost; there is no free magic.
 *       The cost must be payable.</li>
 *   <li><b>Ordo / Termination</b> — Non-instant spells must have a termination condition.</li>
 *   <li><b>Resonance</b> — Volatile pairs must be acknowledged.</li>
 *   <li><b>Signature</b> — The cast is attributed to the caster (audit trail).</li>
 *   <li><b>Locality</b> — Range increases cost; the cost must be affordable.</li>
 * </ol>
 */
public final class SpellLawValidator {

    private SpellLawValidator() {}

    /**
     * Validates the casting context against all six laws.
     *
     * @param ctx the fully-prepared (modifier-adjusted) context
     * @return a {@link ValidationReport} listing all violations
     */
    public static ValidationReport validate(SpellContext ctx) {
        List<Violation> violations = new ArrayList<>();

        checkLaw1_Conservation(ctx, violations);
        checkLaw2_Fames(ctx, violations);
        checkLaw3_Termination(ctx, violations);
        checkLaw4_Resonance(ctx, violations);
        checkLaw5_Signature(ctx, violations);
        checkLaw6_Locality(ctx, violations);

        return new ValidationReport(violations);
    }

    /**
     * Law I: Conservation of Aspect.
     * Verifies the local environment or Personal pool can supply the required Aspects.
     * Fails softly (cast proceeds but costs more) if only partially available.
     */
    private static void checkLaw1_Conservation(SpellContext ctx, List<Violation> out) {
        BlockPos origin   = BlockPos.ofFloored(ctx.getCastOrigin());
        ChunkPos chunkPos = new ChunkPos(origin);

        if (AetherManager.isDeadZone(ctx.getWorld(), chunkPos)) {
            // Dead Zone is a hard block unless drawing entirely from Personal Aether
            boolean hasSufficientPersonal = false;
            if (ctx.getCaster() instanceof PersonalAetherPool pool) {
                hasSufficientPersonal = pool.aspectslib$getPersonalAether()
                        >= ctx.getMetadata().getAetherCost();
            }
            if (!hasSufficientPersonal) {
                out.add(Violation.hard("Law I",
                        "Conservation violation: casting in a Dead Zone with insufficient "
                                + "Personal Aether. Ambient Aspects are absent; the spell cannot proceed."));
            } else {
                out.add(Violation.soft("Law I",
                        "Casting in a Dead Zone — drawing entirely from Personal Aether. "
                                + "Cost is not discounted by ambient contribution."));
            }
        }
    }

    /**
     * Law II: The Fames Principle (Cost).
     * Verifies total available Aether (personal + ambient) covers the spell's cost.
     */
    private static void checkLaw2_Fames(SpellContext ctx, List<Violation> out) {
        double cost = ctx.getMetadata().getAetherCost();
        if (cost <= 0) return;

        BlockPos origin = BlockPos.ofFloored(ctx.getCastOrigin());
        ChunkPos chunkPos = new ChunkPos(origin);
        double    personal = 0;

        if (ctx.getCaster() instanceof PersonalAetherPool pool) {
            personal = pool.aspectslib$getPersonalAether();
        }

        // Check total (ambient + personal) availability
        if (!dev.overgrown.aspectslib.aether.AetherAPI.hasTotalAether(ctx.getWorld(), origin, cost)
                && personal < cost) {
            out.add(Violation.hard("Law II",
                    "Fames violation: insufficient Aether (cost=" + String.format("%.1f", cost)
                            + ", personal=" + String.format("%.1f", personal)
                            + "). Magic always costs; there is no free cast."));
        }
    }

    /**
     * Law III: Ordo / Termination.
     * Warns (soft) if the spell has a sustained duration but no termination condition.
     * This cannot be a hard block at runtime (the formula may have been set elsewhere),
     * but it is flagged prominently.
     */
    private static void checkLaw3_Termination(SpellContext ctx, List<Violation> out) {
        NotationFormula formula = ctx.getData("notation_formula", null);
        int durationTicks = ctx.getMetadata().getDuration();

        if (durationTicks > 0 && formula == null) {
            // A sustained spell with no NotationFormula attached — warn but allow
            out.add(Violation.soft("Law III",
                    "Termination Principle: spell has duration=" + durationTicks
                            + " ticks but no NotationFormula with a ⊥ condition attached. "
                            + "Ensure the executing spell handles its own termination; "
                            + "uncontrolled dissolution may produce Resonance byproducts."));
        }

        if (formula != null && durationTicks > 0) {
            NotationFormula.ValidationResult result = formula.validate();
            for (String err : result.errors()) {
                out.add(Violation.hard("Law III", err));
            }
            for (String warn : result.warnings()) {
                out.add(Violation.soft("Law III/IV", warn));
            }
        }
    }

    /**
     * Law IV: Resonance.
     * Detects Volatile pairs in the spell's Aspect set and applies stability
     * penalties for unmanaged ones.
     */
    private static void checkLaw4_Resonance(SpellContext ctx, List<Violation> out) {
        if (ctx.getPattern() == null) return;

        var aspectIds = ctx.getPattern().aspects().keySet();
        List<VolatileResonancePair> pairs = VolatileResonanceRegistry.detectPairs(aspectIds);

        NotationFormula formula = ctx.getData("notation_formula", null);

        for (VolatileResonancePair vp : pairs) {
            boolean managed = formula != null && formula.isPairManaged(vp.aspect1(), vp.aspect2());
            if (!managed) {
                // Apply byproduct and stability penalty
                VolatileResonanceRegistry.applyVolatileByproduct(ctx, vp, 5, 5); // default intensity
                out.add(Violation.soft("Law IV",
                        "Resonance: unmanaged ⚡ pair " + vp.aspect1() + "+" + vp.aspect2()
                                + " detected. Stability reduced by 15 %. Byproduct: "
                                + vp.getByproductAspect().map(Object::toString).orElse("energy burst")));
            }
        }
    }

    /**
     * Law V: Signature.
     * Records the cast in the Spell audit trail (no blocking conditions,
     * just attribution for forensic analysis).
     */
    private static void checkLaw5_Signature(SpellContext ctx, List<Violation> out) {
        // Store caster UUID in context for downstream forensic use
        ctx.putData("cast_signature", ctx.getCaster().getUuid().toString());
        // No violations to raise; signature is always present
    }

    /**
     * Law VI: Locality.
     * If the caster is in an advanced Unraveling stage, range spells become
     * more dangerous (soft warning or hard block at OPENING stage).
     */
    private static void checkLaw6_Locality(SpellContext ctx, List<Violation> out) {
        UnravelingStage stage = UnravelingTracker.getStage(ctx.getCaster());

        if (stage == UnravelingStage.OPENING) {
            out.add(Violation.hard("Law VI",
                    "Locality violation: caster is in OPENING-stage Unraveling. "
                            + "Casting any spell risks catastrophic membrane interaction. "
                            + "Cast blocked for safety."));
        } else if (stage == UnravelingStage.UNRAVELING) {
            // Apply extra cost and soft-warn
            double currentCost = ctx.getMetadata().getAetherCost();
            ctx.getMetadata().set(SpellMetadata.AETHER_COST, currentCost * 1.5);
            out.add(Violation.soft("Law VI",
                    "Locality: caster is in UNRAVELING stage. Spell cost increased ×1.5; "
                            + "Aspect coherence over distance is compromised."));
        }
    }

    /**
     * A single Law violation.
     *
     * @param law         the law number/name that was violated
     * @param message     human-readable description
     * @param isHardBlock {@code true} = cast must be refused; {@code false} = cast
     *                    may proceed with consequence already applied
     */
    public record Violation(String law, String message, boolean isHardBlock) {
        public static Violation hard(String law, String msg) {
            return new Violation(law, msg, true);
        }

        public static Violation soft(String law, String msg) {
            return new Violation(law, msg, false);
        }
    }

    /**
     * Aggregate result of {@link #validate}.
     */
    public record ValidationReport(List<Violation> violations) {

        /** Returns {@code true} if any hard-block violations are present. */
        public boolean hasHardBlock() {
            return violations.stream().anyMatch(Violation::isHardBlock);
        }

        /** Returns all hard violations. */
        public List<Violation> hardViolations() {
            return violations.stream().filter(Violation::isHardBlock).toList();
        }

        /** Returns all soft violations. */
        public List<Violation> softViolations() {
            return violations.stream().filter(v -> !v.isHardBlock()).toList();
        }
    }
}