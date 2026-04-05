package dev.overgrown.aspectslib.spell.cost;

import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable value object that bundles every input needed by
 * {@link AetherCostCalculator#compute(SpellCostParams)}.
 *
 * <h3>Formula</h3>
 * <pre>
 *   Total_Cost = C_base × R × D × K × E × (1 − A)
 *
 *   C_base = Σ( aspect_intensity_n × tier_multiplier_n )
 *   R = range modifier
 *   D = duration modifier
 *   K = complexity modifier   (scales with distinct Aspect count)
 *   E = environmental opposition modifier
 *   A = resonance discount    (applied as a percentage reduction, capped at 50%)
 * </pre>
 *
 * <p>Build instances through {@link Builder} to avoid parameter-order mistakes.
 */
public final class SpellCostParams {

    /**
     * Ordered map of Aspect ID → intensity (1–10 per the Notation spec).
     * Insertion order determines display and analysis ordering.
     */
    private final Map<Identifier, Integer> aspectIntensities;
    private final SpellRange  range;
    private final SpellDuration duration;
    private final double environmentOppositionMultiplier;
    private final double resonanceDiscountFraction;
    /** Optional area-of-effect radius in metres. Only used when range == AREA. */
    private final float aoeRadiusMeters;

    private SpellCostParams(Builder b) {
        this.aspectIntensities              = Collections.unmodifiableMap(new LinkedHashMap<>(b.aspects));
        this.range                          = b.range;
        this.duration                       = b.duration;
        this.environmentOppositionMultiplier = b.envOpposition;
        this.resonanceDiscountFraction      = b.resonanceDiscount;
        this.aoeRadiusMeters               = b.aoeRadius;
    }

    public Map<Identifier, Integer> getAspectIntensities()           { return aspectIntensities; }
    public SpellRange               getRange()                        { return range; }
    public SpellDuration            getDuration()                     { return duration; }
    public double                   getEnvironmentOppositionMultiplier() { return environmentOppositionMultiplier; }
    public double                   getResonanceDiscountFraction()    { return resonanceDiscountFraction; }
    public float                    getAoeRadiusMeters()              { return aoeRadiusMeters; }

    public static final class Builder {
        private final Map<Identifier, Integer> aspects = new LinkedHashMap<>();
        private SpellRange    range              = SpellRange.NEAR;
        private SpellDuration duration           = SpellDuration.INSTANT;
        private double        envOpposition      = 1.0;
        private double        resonanceDiscount  = 0.0;
        private float         aoeRadius          = 0f;

        /**
         * Adds an Aspect at the given intensity (1–10).  Intensities outside
         * that range are clamped.
         */
        public Builder aspect(Identifier id, int intensity) {
            Objects.requireNonNull(id, "aspect id");
            this.aspects.put(id, Math.max(1, Math.min(10, intensity)));
            return this;
        }

        public Builder range(SpellRange range) {
            this.range = Objects.requireNonNull(range);
            return this;
        }

        public Builder duration(SpellDuration duration) {
            this.duration = Objects.requireNonNull(duration);
            return this;
        }

        /**
         * Environmental opposition multiplier {@code E}.
         * Use values from the table:
         * <ul>
         *   <li>1.0 = neutral / amplifying environment</li>
         *   <li>1.2 = light opposition</li>
         *   <li>1.5 = moderate opposition</li>
         *   <li>2.0 = heavy opposition</li>
         *   <li>3.0 = extreme opposition (dominant opposing Aspect in region)</li>
         * </ul>
         */
        public Builder environmentOpposition(double multiplier) {
            this.envOpposition = Math.max(0.1, multiplier);
            return this;
        }

        /**
         * Resonance discount fraction {@code A} (0.0–0.50).  The total cost
         * is multiplied by {@code (1 − A)}.  Values above 0.50 are clamped;
         * a 50% cap is hard-enforced by the calculator.
         *
         * <p>Typical sources:
         * <ul>
         *   <li>Each parent-child ⊕ pair in the spell: −15% (0.15)</li>
         *   <li>Each sibling ⊕ pair: −8% (0.08)</li>
         *   <li>Ley Node environment: −25% (0.25)</li>
         * </ul>
         */
        public Builder resonanceDiscount(double fraction) {
            this.resonanceDiscount = Math.max(0.0, Math.min(0.50, fraction));
            return this;
        }

        /**
         * Sets the AoE radius when {@link SpellRange#AREA} is used.
         * Has no effect for other range types.
         */
        public Builder aoeRadius(float radiusMeters) {
            this.aoeRadius = Math.max(0, radiusMeters);
            return this;
        }

        public SpellCostParams build() {
            return new SpellCostParams(this);
        }
    }
}