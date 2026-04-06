package dev.overgrown.aspectslib.spell.cost;

import dev.overgrown.aspectslib.aspects.data.AspectTier;
import dev.overgrown.aspectslib.aspects.data.AspectTierRegistry;
import net.minecraft.util.Identifier;

import java.util.Map;

/**
 * Stateless calculator implementing the canonical Aether cost formula.
 *
 * <h3>Formula</h3>
 * <pre>
 *   Total_Cost = C_base × R × D × K × E × (1 − A)
 * </pre>
 *
 * <table border="1">
 *   <tr><th>Symbol</th><th>Name</th><th>Source</th></tr>
 *   <tr><td>C_base</td><td>Base Aspect Cost</td>
 *       <td>Σ(intensity × tier_multiplier) per Aspect</td></tr>
 *   <tr><td>R</td><td>Range Modifier</td>
 *       <td>{@link SpellRange#getMultiplier()} or {@link SpellRange#areaMultiplier}</td></tr>
 *   <tr><td>D</td><td>Duration Modifier</td>
 *       <td>{@link SpellDuration#getMultiplier()}</td></tr>
 *   <tr><td>K</td><td>Complexity Modifier</td>
 *       <td>Scales with number of distinct Aspects; see {@link #complexityModifier}</td></tr>
 *   <tr><td>E</td><td>Environmental Opposition</td>
 *       <td>Provided by caller; see {@link SpellCostParams.Builder#environmentOpposition}</td></tr>
 *   <tr><td>A</td><td>Resonance Discount</td>
 *       <td>Fractional reduction (0.0–0.50); see {@link SpellCostParams.Builder#resonanceDiscount(double)}</td></tr>
 * </table>
 *
 * <h3>Complexity modifier table</h3>
 * <table border="1">
 *   <tr><th>Distinct Aspects</th><th>K</th></tr>
 *   <tr><td>1</td><td>1.0</td></tr>
 *   <tr><td>2</td><td>1.2</td></tr>
 *   <tr><td>3–4</td><td>1.5</td></tr>
 *   <tr><td>5–6</td><td>2.0</td></tr>
 *   <tr><td>7–8</td><td>2.8</td></tr>
 *   <tr><td>9+</td><td>4.0</td></tr>
 * </table>
 *
 * <h3>Ambient Aether fraction</h3>
 * The ambient environment contributes up to 70 % of the total cost, so the
 * Personal Aether draw is {@code Total_Cost × (1 − ambient_fraction)}.
 * {@link #personalAetherDraw} computes this split.
 *
 * <p>Minimum result is 1.0 Aether unit to prevent free casting.
 */
public final class AetherCostCalculator {

    /**
     * Maximum fraction of a spell's cost that the ambient environment can
     * supply (Law II / Fames principle). At least 30 % always comes from
     * Personal Aether.
     */
    public static final double MAX_AMBIENT_FRACTION = 0.70;

    private AetherCostCalculator() {}

    /**
     * Computes the total Aether cost (ambient + personal) for a spell defined
     * by the given parameters.
     *
     * @param params all spell cost inputs; build via {@link SpellCostParams.Builder}
     * @return total Aether cost, always ≥ 1.0
     */
    public static double compute(SpellCostParams params) {
        double cBase        = computeBaseCost(params.getAspectIntensities());
        double rMod         = rangeModifier(params);
        double dMod         = params.getDuration().getMultiplier();
        double kMod         = complexityModifier(params.getAspectIntensities().size());
        double eMod         = params.getEnvironmentOppositionMultiplier();
        double aDiscount    = Math.min(0.50, params.getResonanceDiscountFraction());

        double raw = cBase * rMod * dMod * kMod * eMod * (1.0 - aDiscount);
        return Math.max(1.0, raw);
    }

    /**
     * Splits the total cost into the portion drawn from Personal Aether and
     * the portion supplied by ambient Aether, given the current ambient density
     * fraction (0.0 – 1.0).
     *
     * <pre>
     *   ambient_fraction = min(0.70, ambientDensityFraction × 0.70)
     *   personal_draw    = total × (1 − ambient_fraction)
     *   ambient_draw     = total × ambient_fraction
     * </pre>
     *
     * @param totalCost          result of {@link #compute}
     * @param ambientDensityFraction current/max Aether ratio for the chunk
     *                               (0.0 = dead zone, 1.0 = fully charged)
     * @return array of length 2: {@code [personalDraw, ambientDraw]}
     */
    public static double[] personalAetherDraw(double totalCost, double ambientDensityFraction) {
        double ambientFraction = Math.min(MAX_AMBIENT_FRACTION, ambientDensityFraction * MAX_AMBIENT_FRACTION);
        double personal        = totalCost * (1.0 - ambientFraction);
        double ambient         = totalCost * ambientFraction;
        return new double[]{personal, ambient};
    }

    // Sub-calculations
    /**
     * Computes {@code C_base} from the per-Aspect intensity map.
     *
     * <pre>C_base = Σ( intensity_n × tier_multiplier_n )</pre>
     */
    public static double computeBaseCost(Map<Identifier, Integer> aspectIntensities) {
        double base = 0;
        for (Map.Entry<Identifier, Integer> entry : aspectIntensities.entrySet()) {
            AspectTier tier = AspectTierRegistry.getTier(entry.getKey());
            base += entry.getValue() * tier.getAetherCostPerIntensity();
        }
        return base;
    }

    /**
     * Returns the range modifier {@code R} for the given params, handling the
     * special AREA case.
     */
    public static double rangeModifier(SpellCostParams params) {
        if (params.getRange() == SpellRange.AREA) {
            return SpellRange.areaMultiplier(params.getAoeRadiusMeters());
        }
        return params.getRange().getMultiplier();
    }

    /**
     * Returns the complexity modifier {@code K} based on the number of
     * distinct Aspects in the spell.
     *
     * @param distinctAspects number of different Aspects used
     */
    public static double complexityModifier(int distinctAspects) {
        if (distinctAspects <= 1) return 1.0;
        if (distinctAspects == 2) return 1.2;
        if (distinctAspects <= 4) return 1.5;
        if (distinctAspects <= 6) return 2.0;
        if (distinctAspects <= 8) return 2.8;
        return 4.0;  // 9+: master-tier complexity
    }

    /**
     * Convenience: compute the resonance discount for a set of intra-spell
     * Amplifying pairs by tier relationship.
     *
     * <ul>
     *   <li>Each parent-child  ⊕ pair: −15% (0.15)</li>
     *   <li>Each sibling       ⊕ pair: −8%  (0.08)</li>
     *   <li>Each extended-kin  ⊕ pair: −3%  (0.03)</li>
     *   <li>Environmental ×Ley Node:   −25% (0.25, stacks)</li>
     * </ul>
     *
     * The result is clamped to the 0.50 cap before being returned.
     *
     * @param parentChildPairs  number of parent→child Amplifying pairs
     * @param siblingPairs      number of sibling Amplifying pairs
     * @param extendedKinPairs  number of extended-family Amplifying pairs
     * @param atLeyNode         whether the cast occurs at a Ley Node
     */
    public static double resonanceDiscount(
            int parentChildPairs, int siblingPairs, int extendedKinPairs,
            boolean atLeyNode) {
        double discount = parentChildPairs * 0.15
                + siblingPairs          * 0.08
                + extendedKinPairs      * 0.03
                + (atLeyNode ? 0.25 : 0.0);
        return Math.min(0.50, discount);
    }

    // Overdraw helpers
    /**
     * Environmental opposition multiplier for a given opposition level.
     *
     * @param level 0=none/amplifying, 1=light, 2=moderate, 3=heavy, 4=extreme
     */
    public static double environmentMultiplier(int level) {
        return switch (level) {
            case 0  -> 1.0;
            case 1  -> 1.2;
            case 2  -> 1.5;
            case 3  -> 2.0;
            default -> 3.0;  // 4 = extreme
        };
    }

    /**
     * Estimates the Aether pool fraction consumed by a spell, given a caster's
     * current pool size.  Values above 0.9 enter overdraw territory.
     */
    public static double poolFractionConsumed(double totalCost, double ambientFraction,
                                              double personalPool) {
        if (personalPool <= 0) return 1.0;
        double[] draw = personalAetherDraw(totalCost, ambientFraction);
        return draw[0] / personalPool;
    }
}