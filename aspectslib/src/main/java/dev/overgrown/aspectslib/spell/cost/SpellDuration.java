package dev.overgrown.aspectslib.spell.cost;

/**
 * Canonical duration classifications for spells, each carrying the duration
 * cost multiplier {@code D} used in
 * {@link AetherCostCalculator#compute(SpellCostParams)}.
 *
 * <p>Sustained effects require Ordo scaffolding to hold Aspect expressions
 * coherent over time — this is the mechanical basis for the increasing cost.
 *
 * <table border="1">
 *   <tr><th>Code</th><th>Duration</th><th>Multiplier D</th><th>Ticks (approx)</th></tr>
 *   <tr><td>INSTANT</td><td>fires once, done</td><td>×0.5</td><td>0</td></tr>
 *   <tr><td>BRIEF</td><td>≤1 minute</td><td>×1.0</td><td>1 200</td></tr>
 *   <tr><td>SHORT</td><td>1–10 minutes</td><td>×1.8</td><td>12 000</td></tr>
 *   <tr><td>SUSTAINED</td><td>10 min–1 hr</td><td>×3.0</td><td>72 000</td></tr>
 *   <tr><td>LONG</td><td>&gt;1 hour</td><td>×6.0</td><td>—</td></tr>
 *   <tr><td>PERMANENT</td><td>indefinite</td><td>×20.0</td><td>—</td></tr>
 * </table>
 *
 * <p><strong>Law III reminder:</strong> every non-INSTANT spell <em>must</em>
 * have a defined termination condition (see
 * {@link dev.overgrown.aspectslib.spell.notation.TerminationCondition}).
 */
public enum SpellDuration {

    INSTANT   (0,       0.5,  "Instantaneous"),
    BRIEF     (1_200,   1.0,  "≤1 minute"),
    SHORT     (12_000,  1.8,  "1–10 minutes"),
    SUSTAINED (72_000,  3.0,  "10 minutes–1 hour"),
    LONG      (-1,      6.0,  ">1 hour"),
    PERMANENT (-1,      20.0, "Permanent");

    /** Approximate maximum duration in ticks; -1 for unbounded categories. */
    private final int maxTicks;
    private final double multiplier;
    private final String description;

    SpellDuration(int maxTicks, double multiplier, String description) {
        this.maxTicks = maxTicks;
        this.multiplier = multiplier;
        this.description = description;
    }

    public int getMaxTicks() {
        return maxTicks;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns true when this duration requires a mandatory termination
     * condition. Only {@link #INSTANT} does not.
     */
    public boolean requiresTerminationCondition() {
        return this != INSTANT;
    }

    /** Selects the cheapest {@link SpellDuration} that fits the given ticks. */
    public static SpellDuration forTicks(int ticks) {
        if (ticks <= 0)        return INSTANT;
        if (ticks <= 1_200)    return BRIEF;
        if (ticks <= 12_000)   return SHORT;
        if (ticks <= 72_000)   return SUSTAINED;
        return LONG;
    }
}