package dev.overgrown.thaumaturge.spell.modifier;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * "Scatter" modifier for multi-ray or multi-projectile aspects.
 *
 * Semantics:
 *  - {@code rays} = total directions to produce (including the center ray if {@code includeCenter} is true).
 *  - {@code coneDegrees} = maximum half-angle of the cone around the base direction.
 *  - {@code includeCenter} = whether the unmodified base direction is included in the output.
 *
 * Typical usage inside an AspectEffect:
 * <pre>
 * var scatter = delivery.getModifiers().stream()
 *     .filter(m -> m instanceof ScatterModifierEffect)
 *     .map(m -> (ScatterModifierEffect) m)
 *     .findFirst().orElse(null);
 *
 * List&lt;Vec3d&gt; dirs = (scatter != null)
 *     ? scatter.scatterAround(baseDir, delivery.getWorld().random)
 *     : List.of(baseDir);
 * </pre>
 */
public final class ScatterModifierEffect implements ModifierEffect {
    private final int rays;            // total rays to generate
    private final float coneDegrees;   // max half-angle
    private final boolean includeCenter;

    public ScatterModifierEffect() {
        this(3, 10.0f, true);
    }

    public ScatterModifierEffect(int rays, float coneDegrees, boolean includeCenter) {
        // sanitize
        this.rays = Math.max(1, Math.min(rays, 64));
        this.coneDegrees = Math.max(0.0f, Math.min(coneDegrees, 90.0f));
        this.includeCenter = includeCenter;
    }

    public int getRays() { return rays; }

    public float getConeDegrees() { return coneDegrees; }

    public boolean isIncludeCenter() { return includeCenter; }

    /**
     * Produce directions within a cone around {@code baseDir}.
     * If {@code includeCenter} is true, the first element is exactly {@code baseDir}.
     */
    public List<Vec3d> scatterAround(Vec3d baseDir, Random random) {
        if (baseDir == null) return Collections.emptyList();
        Vec3d axis = safeNormalize(baseDir);
        if (axis.lengthSquared() == 0.0) return Collections.emptyList();

        int toGenerate = includeCenter ? (rays - 1) : rays;
        if (toGenerate <= 0) {
            return includeCenter ? List.of(axis) : Collections.emptyList();
        }

        // Build orthonormal basis (axis, u, v)
        Vec3d ref = (Math.abs(axis.x) < 0.1 && Math.abs(axis.z) < 0.1) ? new Vec3d(1, 0, 0) : new Vec3d(0, 1, 0);
        Vec3d u = safeNormalize(axis.crossProduct(ref));
        Vec3d v = safeNormalize(axis.crossProduct(u));

        double maxTheta = Math.toRadians(this.coneDegrees);

        List<Vec3d> out = new ArrayList<>(rays);
        if (includeCenter) out.add(axis);

        for (int i = 0; i < toGenerate; i++) {
            // Sample within cone: theta in [0, maxTheta], phi in [0, 2π)
            double phi = random.nextDouble() * Math.PI * 2.0;
            // Bias towards the axis so we don't over-weight wide angles
            // Sample cos(theta) uniformly in [cos(max), 1]
            double cosMax = Math.cos(maxTheta);
            double cosTheta = cosMax + (1.0 - cosMax) * random.nextDouble();
            double sinTheta = Math.sqrt(Math.max(0.0, 1.0 - cosTheta * cosTheta));

            Vec3d dir = axis.multiply(cosTheta)
                    .add(u.multiply(sinTheta * Math.cos(phi)))
                    .add(v.multiply(sinTheta * Math.sin(phi)));

            out.add(safeNormalize(dir));
        }

        return out;
    }

    private static Vec3d safeNormalize(Vec3d v) {
        double len = v.length();
        if (len < 1.0e-6) return new Vec3d(0, 0, 0);
        return new Vec3d(v.x / len, v.y / len, v.z / len);
    }
}
