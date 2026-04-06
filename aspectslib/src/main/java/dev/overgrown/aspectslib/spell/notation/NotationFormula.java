package dev.overgrown.aspectslib.spell.notation;

import dev.overgrown.aspectslib.spell.cost.SpellDuration;
import dev.overgrown.aspectslib.spell.cost.SpellRange;
import dev.overgrown.aspectslib.spell.resonance.VolatileResonancePair;
import dev.overgrown.aspectslib.spell.resonance.VolatileResonanceRegistry;
import net.minecraft.util.Identifier;

import java.util.*;

/**
 * A structured representation of an Aspect Notation spell formula.
 *
 * <pre>
 *   Σ( [inputs] ) → {target} : [effect] ⟨duration⟩ ⊥[termination]
 * </pre>
 *
 * <p>{@code NotationFormula} is <em>not</em> a parser for written Notation strings
 * but a programmatic equivalent used by Mages when inscribing spells, and by
 * the casting pipeline for safety validation.
 *
 * <h3>Building a formula</h3>
 * <pre>{@code
 * NotationFormula formula = new NotationFormula.Builder()
 *     .input(IGN, 5, ResonanceOperator.AMPLIFIED_UNION)
 *     .input(POT, 3, ResonanceOperator.AMPLIFIED_UNION)
 *     .range(SpellRange.FAR)
 *     .duration(SpellDuration.SHORT)
 *     .termination(TerminationCondition.onAnyOf(
 *         TerminationCondition.onDurationElapsed(200),
 *         TerminationCondition.onCasterDeath()))
 *     .build();
 *
 * ValidationResult result = formula.validate();
 * if (result.isValid()) {
 *     double cost = formula.computeCost();
 * }
 * }</pre>
 *
 * <h3>Notation operators</h3>
 * See {@link ResonanceOperator} for the set of operators that can govern the
 * relationship between input Aspects.
 */
public final class NotationFormula {

    private final List<InputEntry> inputs;
    private final SpellRange range;
    private final float aoeRadius;
    private final SpellDuration duration;
    private final TerminationCondition termination;
    private final Set<String> managedVolatilePairs; // ⊗ operator applied; keys are pairKey() strings

    private NotationFormula(Builder b) {
        this.inputs = Collections.unmodifiableList(new ArrayList<>(b.inputs));
        this.range = b.range;
        this.aoeRadius = b.aoeRadius;
        this.duration = b.duration;
        this.termination = b.termination;
        this.managedVolatilePairs = Collections.unmodifiableSet(new HashSet<>(b.managedPairKeys));
    }

    // Accessors
    public List<InputEntry> getInputs() {
        return inputs;
    }

    public SpellRange getRange() {
        return range;
    }

    public float getAoeRadius() {
        return aoeRadius;
    }

    public SpellDuration getDuration() {
        return duration;
    }

    public TerminationCondition getTermination() {
        return termination;
    }

    /**
     * Returns whether the given Volatile pair has been explicitly marked as
     * managed ({@code ⊗} operator), suppressing byproduct generation at the
     * cost of extra Aether.
     */
    public boolean isPairManaged(Identifier a, Identifier b) {
        return managedVolatilePairs.contains(pairKey(a, b));
    }

    /** Returns a map of Aspect ID → intensity suitable for the cost calculator. */
    public Map<Identifier, Integer> toIntensityMap() {
        Map<Identifier, Integer> map = new LinkedHashMap<>();
        for (InputEntry e : inputs) map.put(e.aspectId(), e.intensity());
        return map;
    }

    /**
     * Validates the formula against the six Universal Laws.
     *
     * <h3>Checks performed</h3>
     * <ol>
     *   <li>Non-INSTANT duration requires a termination condition (Law III).</li>
     *   <li>No self-referential loops (PRC feeding back to itself) (Law III).</li>
     *   <li>All Volatile pairs flagged; unmanaged ⚡ pairs are warned (Law IV).</li>
     *   <li>At least one input Aspect (trivially checks Law II).</li>
     * </ol>
     *
     * @return a {@link ValidationResult} listing all errors and warnings
     */
    public ValidationResult validate() {
        List<String> errors   = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Law III: Termination Check
        if (duration.requiresTerminationCondition() && termination == null) {
            errors.add("Law III violation: Duration is " + duration
                    + " but no ⊥ termination condition is defined. "
                    + "This is the Dalvoros Failure pattern — add a termination condition.");
        }

        // At least one input
        if (inputs.isEmpty()) {
            errors.add("Formula has no input Aspects. A spell must consume at least one Aspect.");
        }

        // Volatile pair scan — warn for unmanaged pairs
        Set<Identifier> aspectIds = toIntensityMap().keySet();
        List<VolatileResonancePair> volPairs = VolatileResonanceRegistry.detectPairs(aspectIds);
        for (VolatileResonancePair vp : volPairs) {
            if (!isPairManaged(vp.aspect1(), vp.aspect2())) {
                warnings.add("⚡ Unmanaged Volatile pair detected: "
                        + vp.aspect1() + " + " + vp.aspect2()
                        + " (byproduct: " + vp.getByproductAspect().map(Identifier::toString).orElse("energy burst") + "). "
                        + "Add ⊗ managed-opposition operator to suppress byproduct at extra cost, "
                        + "or accept byproduct and −15% stability.");
            }
        }

        // Self-reference check — PRC in inputs referencing the formula itself (simplified heuristic)
        boolean hasPRC = inputs.stream().anyMatch(e -> e.aspectId().getPath().equals("praecantatio"));
        if (hasPRC && duration == SpellDuration.PERMANENT) {
            errors.add("Self-referential Praecantatio + PERMANENT duration detected. "
                    + "This is a Recursive Descent risk. Reduce duration or remove PRC from inputs.");
        }

        return new ValidationResult(errors, warnings);
    }

    // Inner types
    /**
     * A single Aspect input entry with its intensity and governing Resonance operator.
     */
    public record InputEntry(Identifier aspectId, int intensity, ResonanceOperator operator) {}

    /**
     * Operators from Aspect Notation that govern how inputs relate to each other.
     */
    public enum ResonanceOperator {
        /** {@code +}: Simultaneous, independent contributions. */
        SIMULTANEOUS,
        /** {@code ⊕}: Amplified Union; only valid for Amplifying pairs. */
        AMPLIFIED_UNION,
        /** {@code ⊗}: Managed Opposition; suppresses Volatile byproduct at extra cost. */
        MANAGED_OPPOSITION,
        /** {@code →}: Causal; left produces/transforms into right. */
        CAUSAL
    }

    /**
     * Result of {@link #validate()}.
     *
     * @param errors   hard failures that prevent the spell from being inscribed
     * @param warnings issues that reduce stability or efficiency but do not block inscription
     */
    public record ValidationResult(List<String> errors, List<String> warnings) {
        public boolean isValid() {
            return errors.isEmpty();
        }
    }

    // Builder
    public static final class Builder {
        private final List<InputEntry> inputs = new ArrayList<>();
        private SpellRange range = SpellRange.NEAR;
        private float aoeRadius = 0f;
        private SpellDuration duration = SpellDuration.INSTANT;
        private TerminationCondition termination = null;
        private final Set<String> managedPairKeys = new HashSet<>();

        /**
         * Adds an input Aspect with the given intensity and governing operator.
         */
        public Builder input(Identifier aspectId, int intensity, ResonanceOperator op) {
            inputs.add(new InputEntry(aspectId, Math.max(1, Math.min(10, intensity)), op));
            return this;
        }

        /** Shorthand for {@code SIMULTANEOUS} operator. */
        public Builder input(Identifier aspectId, int intensity) {
            return input(aspectId, intensity, ResonanceOperator.SIMULTANEOUS);
        }

        public Builder range(SpellRange range) {
            this.range = range; return this;
        }

        public Builder aoeRadius(float r) {
            this.aoeRadius = r; return this;
        }

        public Builder duration(SpellDuration dur) {
            this.duration = dur; return this;
        }

        public Builder termination(TerminationCondition tc) {
            this.termination = tc; return this;
        }

        /**
         * Marks a Volatile pair as managed ({@code ⊗} operator applied).
         * The byproduct will be suppressed; the extra Aether cost is accounted
         * for automatically by the cost calculator.
         */
        public Builder managedVolatilePair(Identifier a, Identifier b) {
            managedPairKeys.add(pairKey(a, b));
            return this;
        }

        public NotationFormula build() {
            return new NotationFormula(this);
        }
    }

    // Helpers
    private static String pairKey(Identifier a, Identifier b) {
        // Order-independent key
        List<String> parts = new ArrayList<>(List.of(a.toString(), b.toString()));
        Collections.sort(parts);
        return parts.get(0) + "+" + parts.get(1);
    }
}