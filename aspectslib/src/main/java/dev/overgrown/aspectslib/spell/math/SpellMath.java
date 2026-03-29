package dev.overgrown.aspectslib.spell.math;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Static utility class providing the mathematical operations commonly needed
 * when building or evaluating spell effects.
 *
 * <h3>Categories</h3>
 * <ul>
 *   <li><b>Scaling</b>  – linear, clamped, potency-aware.</li>
 *   <li><b>Falloff</b>  – distance-based reduction functions.</li>
 *   <li><b>Geometry</b> – circle/sphere point generation, cone tests.</li>
 *   <li><b>Resonance</b> – applying amplification/barrier to values.</li>
 *   <li><b>Randomness Helpers</b> – seeded spread, chaos factor.</li>
 * </ul>
 */
public final class SpellMath {

    private SpellMath() {}

    // Simple Scaling
    /**
     * Scales {@code base} by {@code potency}, clamped to [{@code min}, {@code max}].
     * <pre>result = clamp(base * potency, min, max)</pre>
     */
    public static float scale(float base, float potency, float min, float max) {
        return MathHelper.clamp(base * potency, min, max);
    }

    /** Integer variant of {@link #scale}. */
    public static int scaleInt(float base, float potency, int min, int max) {
        return MathHelper.clamp(Math.round(base * potency), min, max);
    }

    /**
     * Linear interpolation between {@code a} and {@code b} by {@code t ∈ [0,1]}.
     */
    public static float lerp(float a, float b, float t) {
        return a + (b - a) * MathHelper.clamp(t, 0, 1);
    }

    // Falloff
    /**
     * Linear falloff: Returns 1 at the origin, 0 at and beyond {@code maxRange}.
     */
    public static float linearFalloff(double distance, double maxRange) {
        if (maxRange <= 0) return 0;
        return (float) MathHelper.clamp(1.0 - (distance / maxRange), 0, 1);
    }

    /**
     * Quadratic (inverse-square) falloff, physically realistic for point sources.
     * Returns 1 at distance 0, falls to 0 at {@code maxRange}.
     */
    public static float quadraticFalloff(double distance, double maxRange) {
        if (maxRange <= 0) return 0;
        float t = linearFalloff(distance, maxRange);
        return t * t;
    }

    /**
     * Smoothstep falloff: Starts and ends with zero slope for a softer look.
     */
    public static float smoothstepFalloff(double distance, double maxRange) {
        float t = linearFalloff(distance, maxRange);
        return t * t * (3 - 2 * t); // Hermite interpolation
    }

    /**
     * Applies a falloff multiplier to {@code baseValue} based on the entity's
     * distance from the spell origin.
     */
    public static float applyFalloff(float baseValue, Vec3d origin, Vec3d entityPos,
                                     double maxRange, FalloffType falloff) {
        double dist = origin.distanceTo(entityPos);
        float factor = switch (falloff) {
            case LINEAR    -> linearFalloff(dist, maxRange);
            case QUADRATIC -> quadraticFalloff(dist, maxRange);
            case SMOOTHSTEP -> smoothstepFalloff(dist, maxRange);
            case NONE      -> 1.0f;
        };
        return baseValue * factor;
    }

    public enum FalloffType {
        NONE,
        LINEAR,
        QUADRATIC,
        SMOOTHSTEP
    }

    // Resonance Application
    /**
     * Applies an amplification factor to a base value.
     * {@code amplified = base * amplificationFactor}
     */
    public static float amplify(float base, double amplificationFactor) {
        return (float) (base * amplificationFactor);
    }

    /**
     * Reduces a base value by the barrier cost (opposing resonance).
     * The result is never negative.
     * {@code result = max(0, base - barrierCost)}
     */
    public static float applyBarrier(float base, double barrierCost) {
        return (float) Math.max(0, base - barrierCost);
    }

    /**
     * Full resonance application: Amplify then subtract barrier.
     */
    public static float applyResonance(float base, double amplificationFactor, double barrierCost) {
        return applyBarrier(amplify(base, amplificationFactor), barrierCost);
    }

    // Geometry
    /**
     * Generates {@code count} evenly-spaced positions on a horizontal ring of
     * {@code radius} centred at {@code origin}.
     */
    public static List<Vec3d> ringPositions(Vec3d origin, double radius, int count) {
        List<Vec3d> positions = new ArrayList<>(count);
        double step = (2 * Math.PI) / count;
        for (int i = 0; i < count; i++) {
            double angle = i * step;
            positions.add(new Vec3d(
                    origin.x + radius * Math.cos(angle),
                    origin.y,
                    origin.z + radius * Math.sin(angle)
            ));
        }
        return positions;
    }

    /**
     * Returns all {@link BlockPos} positions within a sphere of {@code radius}
     * centred on {@code centre}.
     */
    public static List<BlockPos> spherePositions(BlockPos centre, int radius) {
        List<BlockPos> positions = new ArrayList<>();
        int r2 = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz <= r2) {
                        positions.add(centre.add(dx, dy, dz));
                    }
                }
            }
        }
        return positions;
    }

    /**
     * Returns {@code true} if {@code point} lies within the cone defined by
     * an {@code apex}, a normalised {@code axis}, a half-angle {@code halfAngleDeg},
     * and a {@code length}.
     */
    public static boolean isInCone(Vec3d point, Vec3d apex, Vec3d axis,
                                   float halfAngleDeg, float length) {
        Vec3d toPoint = point.subtract(apex);
        double dist = toPoint.length();
        if (dist > length) return false;
        if (dist < 1e-6) return true;
        double cosHalf = Math.cos(Math.toRadians(halfAngleDeg));
        return toPoint.normalize().dotProduct(axis.normalize()) >= cosHalf;
    }

    /**
     * Computes the reflection of vector {@code d} off a surface with normal
     * {@code n}.  Both inputs should be normalized.
     */
    public static Vec3d reflect(Vec3d d, Vec3d n) {
        return d.subtract(n.multiply(2.0 * d.dotProduct(n)));
    }

    // Aether Cost Helpers
    /**
     * Computes a final Aether cost factoring in potency, complexity, and
     * an optional environmental resonance multiplier.
     *
     * <pre>cost = baseCost * (complexity ^ 0.5) * potency * resonanceMult</pre>
     */
    public static double computeAetherCost(double baseCost, double complexity,
                                           double potency, double resonanceMultiplier) {
        return baseCost * Math.sqrt(Math.max(1, complexity)) * potency * resonanceMultiplier;
    }

    /**
     * Returns the "stability roll" result given a stability value [0, 1] and a
     * random float [0, 1].  Returns {@code true} when the spell succeeds
     * (i.e. the roll is below the stability threshold).
     */
    public static boolean stabilityCheck(float stability, float randomValue) {
        return randomValue <= MathHelper.clamp(stability, 0, 1);
    }

    // Entropy (chaos) helpers
    /**
     * Maps a random double in [0, 1] to a "chaos factor" that can be used to
     * vary spell parameters unpredictably.
     *
     * <pre>chaos = base * (1 + chaosRange * (random - 0.5) * 2)</pre>
     *
     * @param base       the deterministic base value
     * @param chaosRange fraction of {@code base} that can be randomly added or
     *                   subtracted (0 = no chaos, 1 = ±100 %)
     * @param random     a value in [0, 1]
     */
    public static float applyChaosFactor(float base, float chaosRange, float random) {
        float deviation = chaosRange * (random - 0.5f) * 2.0f;
        return base * (1.0f + deviation);
    }
}