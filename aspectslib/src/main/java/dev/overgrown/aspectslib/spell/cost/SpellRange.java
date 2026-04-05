package dev.overgrown.aspectslib.spell.cost;

/**
 * Canonical range classifications for spells, each carrying the range cost
 * multiplier {@code R} used in
 * {@link AetherCostCalculator#compute(SpellCostParams)}.
 *
 * <p>Range multipliers reflect Law VI (the Locality Principle): projecting and
 * maintaining coherent Aspect expressions over distance costs progressively
 * more Aether.
 *
 * <table border="1">
 *   <tr><th>Code</th><th>Approximate Reach</th><th>Multiplier R</th></tr>
 *   <tr><td>SELF</td><td>0 m (caster only)</td><td>×0.5</td></tr>
 *   <tr><td>TOUCH</td><td>contact</td><td>×0.75</td></tr>
 *   <tr><td>NEAR</td><td>≤5 m</td><td>×1.0</td></tr>
 *   <tr><td>FAR</td><td>≤20 m</td><td>×1.5</td></tr>
 *   <tr><td>DIST</td><td>≤100 m</td><td>×2.5</td></tr>
 *   <tr><td>EXTREME</td><td>&gt;100 m</td><td>×5.0</td></tr>
 *   <tr><td>AREA</td><td>radius-scaled</td><td>×1.0 + 0.4 per 5 m radius</td></tr>
 * </table>
 *
 * <p>{@link #AREA} uses {@link #areaMultiplier(float)} instead of the static
 * {@link #getMultiplier()} so callers must handle it specially.
 */
public enum SpellRange {

    SELF (0f, 0.50),
    TOUCH (1f, 0.75),
    NEAR (5f, 1.00),
    FAR (20f, 1.50),
    DIST (100f, 2.50),
    EXTREME (999f, 5.00),
    /**
     * Area-of-effect – use {@link #areaMultiplier(float)} for the actual cost
     * multiplier; {@link #getMultiplier()} returns the base of 1.0.
     */
    AREA (0f, 1.00);

    private final float maxReachMeters;
    private final double multiplier;

    SpellRange(float maxReachMeters, double multiplier) {
        this.maxReachMeters = maxReachMeters;
        this.multiplier = multiplier;
    }

    /** Maximum reach in metres for non-AREA ranges; 0 for AREA. */
    public float getMaxReachMeters() {
        return maxReachMeters;
    }

    /**
     * The cost multiplier {@code R}.  For {@link #AREA}, use
     * {@link #areaMultiplier(float)} instead.
     */
    public double getMultiplier() {
        return multiplier;
    }

    /**
     * Computes the range multiplier for an AREA spell with the given radius.
     *
     * <pre>R = 1.0 + 0.4 × (radius / 5)</pre>
     *
     * @param radiusMeters sphere/cylinder radius in metres
     */
    public static double areaMultiplier(float radiusMeters) {
        return 1.0 + 0.4 * (radiusMeters / 5.0);
    }

    /**
     * Selects the appropriate non-AREA {@link SpellRange} for a given reach in
     * metres (nearest matching tier; errs upward).
     */
    public static SpellRange forReach(float meters) {
        if (meters <= 0) return SELF;
        if (meters <= 1) return TOUCH;
        if (meters <= 5) return NEAR;
        if (meters <= 20) return FAR;
        if (meters <= 100) return DIST;
        return EXTREME;
    }
}