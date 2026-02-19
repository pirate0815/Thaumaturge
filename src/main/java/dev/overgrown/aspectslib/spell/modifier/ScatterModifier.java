package dev.overgrown.aspectslib.spell.modifier;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.spell.SpellContext;
import dev.overgrown.aspectslib.spell.SpellMetadata;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;

/**
 * Scatter modifier: Marks the spell as "scattered". Spells that support
 * multiple projectiles should check for this modifier and call
 * {@link #scatterDirections(Vec3d, Random)} to obtain the actual directions.
 *
 * <p>By default, produces 3 rays within a 10° cone, including the center.
 */
public final class ScatterModifier implements SpellModifier {

    private final int rays;
    private final float coneDegrees;
    private final boolean includeCenter;

    @Override
    public Identifier getId() {
        return AspectsLib.identifier("scatter");
    }

    public ScatterModifier() {
        this(3, 10.0f, true);
    }

    public ScatterModifier(int rays, float coneDegrees, boolean includeCenter) {
        this.rays = Math.max(1, Math.min(64, rays));
        this.coneDegrees = Math.max(0, Math.min(90, coneDegrees));
        this.includeCenter = includeCenter;
    }

    @Override
    public SpellMetadata modifyMetadata(SpellMetadata metadata, SpellContext ctx) {
        // Scatter often reduces individual projectile potency, so we lower
        // the base damage slightly.
        double current = metadata.getPotency();
        metadata.set(SpellMetadata.POTENCY, current * 0.7);
        return metadata;
    }

    @Override
    public void onPreExecute(SpellContext ctx) {
        // No immediate action; the spell itself will query the directions.
    }

    @Override
    public void onPostExecute(SpellContext ctx, boolean success) {}

    /**
     * Generates a list of directions within a cone around {@code baseDir}.
     * @param baseDir the central direction (must be normalized)
     * @param random  a random source
     * @return a list of normalized direction vectors
     */
    public List<Vec3d> scatterDirections(Vec3d baseDir, Random random) {
        if (baseDir == null) return List.of();
        Vec3d axis = baseDir.normalize();
        if (axis.lengthSquared() == 0) return List.of();

        int toGenerate = includeCenter ? rays - 1 : rays;
        if (toGenerate <= 0) {
            return includeCenter ? List.of(axis) : List.of();
        }

        // Build orthonormal basis
        Vec3d ref = (Math.abs(axis.x) < 0.1 && Math.abs(axis.z) < 0.1)
                ? new Vec3d(1, 0, 0)
                : new Vec3d(0, 1, 0);
        Vec3d u = axis.crossProduct(ref).normalize();
        Vec3d v = axis.crossProduct(u).normalize();

        double maxTheta = Math.toRadians(coneDegrees);
        List<Vec3d> out = new ArrayList<>(rays);
        if (includeCenter) out.add(axis);

        for (int i = 0; i < toGenerate; i++) {
            double phi = random.nextDouble() * 2 * Math.PI;
            double cosMax = Math.cos(maxTheta);
            double cosTheta = cosMax + (1 - cosMax) * random.nextDouble();
            double sinTheta = Math.sqrt(Math.max(0, 1 - cosTheta * cosTheta));

            Vec3d dir = axis.multiply(cosTheta)
                    .add(u.multiply(sinTheta * Math.cos(phi)))
                    .add(v.multiply(sinTheta * Math.sin(phi)));
            out.add(dir.normalize());
        }
        return out;
    }

    public int getRays() {
        return rays;
    }

    public float getConeDegrees() {
        return coneDegrees;
    }

    public boolean isIncludeCenter() {
        return includeCenter;
    }
}