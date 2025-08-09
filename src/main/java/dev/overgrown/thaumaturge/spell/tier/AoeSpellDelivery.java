package dev.overgrown.thaumaturge.spell.tier;

import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Delivery data for AOE spells (center + radius).
 * Carries the caster, resolved aspect, and modifiers.
 */
public final class AoeSpellDelivery {
    private final ServerPlayerEntity caster;
    private final @Nullable AspectEffect aspect;
    private final List<ModifierEffect> modifiers;

    private final BlockPos center;
    private final float radius;

    public AoeSpellDelivery(ServerPlayerEntity caster,
                            @Nullable AspectEffect aspect,
                            @Nullable List<ModifierEffect> modifiers,
                            BlockPos center,
                            float radius) {
        this.caster = caster;
        this.aspect = aspect;
        this.modifiers = (modifiers == null) ? Collections.emptyList() : List.copyOf(modifiers);
        this.center = center.toImmutable();
        this.radius = Math.max(0f, radius);
    }

    /** Returns a new delivery with identical target but provided aspect/modifiers. */
    public AoeSpellDelivery withContext(AspectEffect aspect, List<ModifierEffect> modifiers) {
        return new AoeSpellDelivery(this.caster, aspect, modifiers, this.center, this.radius);
    }

    // --- Accessors ---

    public ServerPlayerEntity getCaster() { return caster; }

    public @Nullable AspectEffect getAspect() { return aspect; }

    public List<ModifierEffect> getModifiers() { return modifiers; }

    public BlockPos getCenter() { return center; }

    public Vec3d getCenterVec() { return Vec3d.ofCenter(center); }

    public float getRadius() { return radius; }

    public ServerWorld getWorld() { return caster.getServerWorld(); }

    /** Axis-aligned box that fully contains the sphere. */
    public Box getBoundingBox() {
        double r = radius;
        Vec3d c = getCenterVec();
        return new Box(c.x - r, c.y - r, c.z - r, c.x + r, c.y + r, c.z + r);
    }

    /** Convenience: fetch entities inside the AABB; caller can distance-check for true sphere if desired. */
    public <T extends Entity> List<T> getEntitiesInAabb(Class<T> type, Predicate<? super T> filter) {
        return getWorld().getEntitiesByClass(type, getBoundingBox(), filter);
    }
}
