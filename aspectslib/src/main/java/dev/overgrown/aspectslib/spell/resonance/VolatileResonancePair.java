package dev.overgrown.aspectslib.spell.resonance;

import net.minecraft.util.Identifier;

import java.util.Optional;

/**
 * Describes a Volatile Resonance pair (marked {@code ⚡} in Notation), two
 * Aspects that, when brought into forced contact, do not simply neutralize but
 * release energy and/or spawn a third byproduct Aspect.
 *
 * <p>Unlike Opposing pairs (which suppress each other proportionally), Volatile
 * collisions produce an energetic discharge that the spellcaster must actively
 * manage via the {@code ⊗} (Managed Opposition) operator.  Unmanaged Volatile
 * pairs inside a spell formula are the single most common cause of Vessel
 * explosions and mid-cast backlash.
 *
 * <h3>Canonical pairs</h3>
 * <table border="1">
 *   <tr><th>Pair</th><th>Byproduct</th><th>Notes</th></tr>
 *   <tr><td>AQU + IGN</td><td>AER burst + POT discharge</td>
 *       <td>The archetypal volatile pair; steam + kinetic shockwave</td></tr>
 *   <tr><td>ORD + PDT</td><td>PRM surge</td>
 *       <td>Structure vs. entropy → forced transformation</td></tr>
 *   <tr><td>GEL + IGN</td><td>AQU + AER burst</td>
 *       <td>Faster/smaller than AQU+IGN; abrupt phase transition</td></tr>
 *   <tr><td>LUX + TEN</td><td>VAC residue</td>
 *       <td>Asymmetric — TEN consumes LUX at ~2:3 ratio</td></tr>
 *   <tr><td>VTM + VIC</td><td>EXA pulse</td>
 *       <td>Corruption + life → spreading undeath pulse</td></tr>
 *   <tr><td>VAC + PRC</td><td>ALI resonance</td>
 *       <td>The most feared pair; ALI residue self-propagates</td></tr>
 * </table>
 */
public record VolatileResonancePair(
        Identifier aspect1,
        Identifier aspect2,
        Identifier byproductAspect,
        double byproductIntensityMultiplier,
        boolean asymmetric,
        String notes
) {

    /**
     * Returns {@code true} if this pair involves the given two Aspect IDs,
     * regardless of order.
     */
    public boolean matches(Identifier a, Identifier b) {
        return (aspect1.equals(a) && aspect2.equals(b))
                || (aspect1.equals(b) && aspect2.equals(a));
    }

    /**
     * Returns the byproduct Aspect, if one is produced.
     * Some pairs produce a pure energy discharge rather than a spawned Aspect;
     * in those cases an empty Optional is returned.
     */
    public Optional<Identifier> getByproductAspect() {
        return Optional.ofNullable(byproductAspect);
    }

    /**
     * Computes the intensity of the byproduct Aspect given the minimum of the
     * two colliding intensities (i.e., the smaller quantity is consumed first).
     *
     * <pre>byproduct_intensity = min(i1, i2) × multiplier</pre>
     */
    public int computeByproductIntensity(int intensity1, int intensity2) {
        return (int) Math.max(1, Math.min(intensity1, intensity2) * byproductIntensityMultiplier);
    }

    /**
     * Convenience builder for creating a symmetric pair (most cases).
     */
    public static VolatileResonancePair of(
            Identifier a1, Identifier a2,
            Identifier byproduct, double multiplier, String notes) {
        return new VolatileResonancePair(a1, a2, byproduct, multiplier, false, notes);
    }

    /**
     * Convenience builder for an asymmetric pair where {@code aspect1} consumes
     * {@code aspect2} at greater efficiency (e.g. TEN consumes LUX 2:3).
     */
    public static VolatileResonancePair asymmetric(
            Identifier dominant, Identifier consumed,
            Identifier byproduct, double multiplier, String notes) {
        return new VolatileResonancePair(dominant, consumed, byproduct, multiplier, true, notes);
    }
}