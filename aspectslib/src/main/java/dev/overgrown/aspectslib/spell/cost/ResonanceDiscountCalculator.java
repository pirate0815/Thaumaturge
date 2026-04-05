package dev.overgrown.aspectslib.spell.cost;

import dev.overgrown.aspectslib.aspects.data.AspectTier;
import dev.overgrown.aspectslib.aspects.data.AspectTierRegistry;
import dev.overgrown.aspectslib.resonance.Resonance;
import dev.overgrown.aspectslib.resonance.ResonanceManager;
import net.minecraft.util.Identifier;

import java.util.*;

/**
 * Computes the Resonance discount fraction {@code A} for a spell's Aspect set,
 * used as the final multiplier in
 * {@link AetherCostCalculator#compute(SpellCostParams)}.
 *
 * <h3>Discount table</h3>
 * <table border="1">
 *   <tr><th>Pair type</th><th>Reduction</th></tr>
 *   <tr><td>Parent–child ⊕</td><td>−15 % per pair</td></tr>
 *   <tr><td>Sibling ⊕</td><td>−8 % per pair</td></tr>
 *   <tr><td>Extended-kin ⊕</td><td>−3 % per pair</td></tr>
 *   <tr><td>Ley Node environment</td><td>−25 % (stacks)</td></tr>
 *   <tr><td>Maximum cap</td><td>50 % total</td></tr>
 * </table>
 *
 * <p>The classification of pairs (parent-child vs. sibling vs. extended)
 * is derived from {@link AspectTierRegistry} tier proximity: pairs whose
 * combined tier-delta is 1 are parent–child; delta 2 is sibling; delta 3+
 * is extended.
 */
public final class ResonanceDiscountCalculator {

    private ResonanceDiscountCalculator() {}

    /**
     * Computes the total Resonance discount fraction for the given Aspect set.
     *
     * @param aspectIds  all Aspects present in the spell
     * @param atLeyNode  whether the cast origin is at a Ley Node
     * @return discount fraction in [0.0, 0.50]
     */
    public static double compute(Collection<Identifier> aspectIds, boolean atLeyNode) {
        List<Identifier> ids = new ArrayList<>(aspectIds);
        double total = 0.0;

        for (int i = 0; i < ids.size(); i++) {
            for (int j = i + 1; j < ids.size(); j++) {
                Identifier a = ids.get(i);
                Identifier b = ids.get(j);
                total += discountForPair(a, b);
            }
        }

        if (atLeyNode) total += 0.25;

        return Math.min(0.50, total);
    }

    /**
     * Returns the discount fraction for a single Aspect pair, or 0.0 if
     * the pair is not Amplifying or is not registered in
     * {@link ResonanceManager}.
     */
    public static double discountForPair(Identifier a, Identifier b) {
        // Check if the pair has an AMPLIFYING resonance registered
        List<Resonance> resonancesForA = ResonanceManager.RESONANCE_MAP.getOrDefault(a, List.of());
        boolean amplifying = resonancesForA.stream()
                .anyMatch(r -> r.matches(a, b) && r.type() == Resonance.Type.AMPLIFYING);

        if (!amplifying) return 0.0;

        // Classify pair by tier relationship
        AspectTier tierA = AspectTierRegistry.getTier(a);
        AspectTier tierB = AspectTierRegistry.getTier(b);
        int tierDelta = Math.abs(tierA.getNumber() - tierB.getNumber());

        return switch (tierDelta) {
            case 1 -> 0.15;  // parent–child
            case 2 -> 0.08;  // sibling (same grandparent tier)
            default -> 0.03; // extended-family (shared distant ancestor)
        };
    }

    /**
     * Builds a {@link SpellCostParams} with the resonance discount pre-computed
     * and a Ley Node flag applied.
     *
     * <p>This is the recommended way to construct cost params when the caller
     * already knows the Aspect set and location.
     */
    public static SpellCostParams.Builder applyTo(SpellCostParams.Builder builder,
                                                  Collection<Identifier> aspectIds,
                                                  boolean atLeyNode) {
        double discount = compute(aspectIds, atLeyNode);
        return builder.resonanceDiscount(discount);
    }
}