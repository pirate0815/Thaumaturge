package dev.overgrown.aspectslib.spell;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * {@code SpellShape} describes the three-dimensional area a spell occupies or
 * affects when it resolves.  It is intentionally decoupled from targeting logic:
 * the shape describes <em>what region</em> is involved; the spell or effect
 * decides what to do inside that region.
 *
 * <h3>Supported shapes</h3>
 * <ul>
 *   <li>{@link Type#POINT}    – A single block position (0-radius).</li>
 *   <li>{@link Type#LINE}     – A straight ray of configurable length.</li>
 *   <li>{@link Type#SPHERE}   – A sphere centered on the origin point.</li>
 *   <li>{@link Type#CUBE}     – An axis-aligned cube (half-extent in each
 *       direction).</li>
 *   <li>{@link Type#CONE}     – A forward-facing cone, useful for breath
 *       attacks.</li>
 *   <li>{@link Type#CYLINDER} – A vertical cylinder, good for "pillar" or
 *       "storm" spells.</li>
 * </ul>
 *
 * <h3>Usage example</h3>
 * <pre>{@code
 * SpellShape aoe = SpellShape.sphere(6.0f);
 * List<LivingEntity> targets = aoe.getEntitiesInside(
 *         world, origin, LivingEntity.class, e -> e != caster);
 * }</pre>
 */
public final class SpellShape {

    // Shape Type
    public enum Type {
        POINT, LINE, SPHERE, CUBE, CONE, CYLINDER;

        public static Type fromString(String name) {
            try {
                return Type.valueOf(name.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return POINT;
            }
        }
    }

    // Fields
    private final Type   type;
    private final float  size;      // radius / half-extent / length depending on type
    private final float  height;    // relevant for CYLINDER and CONE
    private final float  angle;     // half-angle in degrees for CONE
    private final Vec3d  offset;    // local offset from the origin before the shape is applied

    // Private constructor
    private SpellShape(Type type, float size, float height, float angle, Vec3d offset) {
        this.type   = type;
        this.size   = Math.max(0, size);
        this.height = Math.max(0, height);
        this.angle  = Math.max(0, Math.min(90, angle));
        this.offset = offset != null ? offset : Vec3d.ZERO;
    }

    // Factory helpers
    public static SpellShape point() {
        return new SpellShape(Type.POINT, 0, 0, 0, Vec3d.ZERO);
    }

    public static SpellShape line(float length) {
        return new SpellShape(Type.LINE, length, 0, 0, Vec3d.ZERO);
    }

    public static SpellShape sphere(float radius) {
        return new SpellShape(Type.SPHERE, radius, 0, 0, Vec3d.ZERO);
    }

    public static SpellShape cube(float halfExtent) {
        return new SpellShape(Type.CUBE, halfExtent, 0, 0, Vec3d.ZERO);
    }

    /**
     * @param halfAngleDeg Half-angle of the cone from its central axis, in degrees.
     * @param length       How far the cone extends.
     */
    public static SpellShape cone(float halfAngleDeg, float length) {
        return new SpellShape(Type.CONE, length, length, halfAngleDeg, Vec3d.ZERO);
    }

    public static SpellShape cylinder(float radius, float height) {
        return new SpellShape(Type.CYLINDER, radius, height, 0, Vec3d.ZERO);
    }

    /** Returns a copy of this shape with an additional local offset applied. */
    public SpellShape withOffset(Vec3d offset) {
        return new SpellShape(type, size, height, angle, offset);
    }

    /** Returns a copy of this shape with its primary size dimension scaled. */
    public SpellShape scaled(float factor) {
        return new SpellShape(type, size * factor, height * factor, angle, offset);
    }

    // Accessors
    public Type  getType()   { return type; }
    public float getSize()   { return size; }
    public float getHeight() { return height; }
    public float getAngle()  { return angle; }
    public Vec3d getOffset() { return offset; }

    // Entity query helpers
    /**
     * Collects all entities of type {@code T} that lie inside this shape when
     * it is centered at {@code origin} and oriented along {@code direction}
     * (used for LINE and CONE; ignored for SPHERE/CUBE/CYLINDER).
     */
    public <T extends Entity> List<T> getEntitiesInside(
            World world,
            Vec3d origin,
            Vec3d direction,
            Class<T> type,
            Predicate<T> filter) {

        Vec3d center = origin.add(offset);

        Box queryBox = switch (this.type) {
            case POINT    -> new Box(BlockPos.ofFloored(center));
            case LINE     -> buildLineBox(center, direction);
            case SPHERE   -> new Box(center.x - size, center.y - size, center.z - size,
                    center.x + size, center.y + size, center.z + size);
            case CUBE     -> new Box(center.x - size, center.y - size, center.z - size,
                    center.x + size, center.y + size, center.z + size);
            case CONE     -> buildLineBox(center, direction); // conservative AABB
            case CYLINDER -> new Box(center.x - size, center.y, center.z - size,
                    center.x + size, center.y + height, center.z + size);
        };

        List<T> candidates = world.getEntitiesByClass(type, queryBox, filter);

        return switch (this.type) {
            case SPHERE   -> candidates.stream()
                    .filter(e -> e.getPos().squaredDistanceTo(center) <= size * size)
                    .toList();
            case CYLINDER -> candidates.stream()
                    .filter(e -> {
                        Vec3d p = e.getPos();
                        double dx = p.x - center.x, dz = p.z - center.z;
                        return (dx * dx + dz * dz) <= size * size
                                && p.y >= center.y && p.y <= center.y + height;
                    })
                    .toList();
            case CONE     -> filterCone(candidates, center, direction);
            default       -> candidates;
        };
    }

    /** Overload that uses Vec3d.ZERO as direction (for isotropic shapes). */
    public <T extends Entity> List<T> getEntitiesInside(
            World world, Vec3d origin, Class<T> type, Predicate<T> filter) {
        return getEntitiesInside(world, origin, Vec3d.ZERO, type, filter);
    }

    // Internal geometry helpers
    private Box buildLineBox(Vec3d origin, Vec3d dir) {
        Vec3d end = origin.add(dir.normalize().multiply(size));
        return new Box(
                Math.min(origin.x, end.x) - 1, Math.min(origin.y, end.y) - 1, Math.min(origin.z, end.z) - 1,
                Math.max(origin.x, end.x) + 1, Math.max(origin.y, end.y) + 1, Math.max(origin.z, end.z) + 1
        );
    }

    private <T extends Entity> List<T> filterCone(List<T> candidates, Vec3d origin, Vec3d axis) {
        if (axis.lengthSquared() < 1e-6) return candidates;
        Vec3d normAxis = axis.normalize();
        double cosHalf = Math.cos(Math.toRadians(angle));
        double sinHalf = Math.sin(Math.toRadians(angle));
        return candidates.stream().filter(e -> {
            Vec3d toEntity = e.getPos().subtract(origin);
            double dist = toEntity.length();
            if (dist > size) return false;
            if (dist < 1e-6) return true;
            double dot = toEntity.normalize().dotProduct(normAxis);
            return dot >= cosHalf;
        }).toList();
    }

    // NBT Data
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("Type",   type.name());
        nbt.putFloat ("Size",   size);
        nbt.putFloat ("Height", height);
        nbt.putFloat ("Angle",  angle);
        nbt.putDouble("OffsetX", offset.x);
        nbt.putDouble("OffsetY", offset.y);
        nbt.putDouble("OffsetZ", offset.z);
        return nbt;
    }

    public static SpellShape fromNbt(NbtCompound nbt) {
        return new SpellShape(
                Type.fromString(nbt.getString("Type")),
                nbt.getFloat("Size"),
                nbt.getFloat("Height"),
                nbt.getFloat("Angle"),
                new Vec3d(nbt.getDouble("OffsetX"),
                        nbt.getDouble("OffsetY"),
                        nbt.getDouble("OffsetZ"))
        );
    }

    @Override
    public String toString() {
        return "SpellShape{type=" + type + ", size=" + size + ", height=" + height + "}";
    }
}