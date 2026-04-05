package dev.overgrown.aspectslib.aspects.data;

/**
 * The four tiers of Aspect complexity, each carrying a different per-intensity
 * Aether cost multiplier used by {@link dev.overgrown.aspectslib.spell.cost.AetherCostCalculator}.
 *
 * <pre>
 *   C_base = Σ( intensity_n × tier_multiplier_n )
 * </pre>
 */
public enum AspectTier {

    /** Tier I: Aer, Aqua, Terra, Ignis, Ordo, Perditio. */
    PRIMAL(1, 5.0),

    /** Tier II: Gelum, Lux, Metallum, Mortuus, Motus, Permutatio, Potentia, Vacuos, Victus, Vitreus. */
    SECONDARY(2, 8.0),

    /** Tier III: Bestia, Exanimis, Fames, Herba, Instrumentum, Praecantatio, Spiritus, Tenebrae, Vinculum, Volatus. */
    TERTIARY(3, 12.0),

    /** Tier IV: Alienis, Alkimia, Auram, Aversio, Cognitio, Desiderium, Fabrico, Humanus, Machina, Praemunio, Sensus, Vitium. */
    QUATERNARY(4, 20.0);

    /** Numeric tier (1–4). */
    private final int number;

    /**
     * Aether cost per intensity point for this tier.
     * Multiply by the spell's declared intensity for this Aspect to get the
     * Aspect's contribution to {@code C_base}.
     */
    private final double aetherCostPerIntensity;

    AspectTier(int number, double aetherCostPerIntensity) {
        this.number = number;
        this.aetherCostPerIntensity = aetherCostPerIntensity;
    }

    public int getNumber() {
        return number;
    }

    public double getAetherCostPerIntensity() {
        return aetherCostPerIntensity;
    }

    /**
     * Returns the {@link AspectTier} for the given tier number (1–4),
     * or {@link #PRIMAL} when out of range.
     */
    public static AspectTier fromNumber(int n) {
        for (AspectTier t : values()) {
            if (t.number == n) return t;
        }
        return PRIMAL;
    }
}