package dev.overgrown.thaumaturge.spell.tier;

import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Delivery for area-of-effect spells.
 */
public final class AoeSpellDelivery implements SpellDelivery {

    private final ServerPlayerEntity caster;
    private final ServerWorld world;
    private final BlockPos center;
    private final float radius;

    private List<ModifierEffect> modifiers = List.of();

    public AoeSpellDelivery(ServerPlayerEntity caster, BlockPos center, float radius) {
        this.caster = Objects.requireNonNull(caster, "caster");
        this.world  = (ServerWorld) caster.getWorld();
        this.center = Objects.requireNonNull(center, "center");
        this.radius = Math.max(0f, Math.min(radius, 32f));
    }

    // Back-compat with old callsites that passed unused context args.
    @SuppressWarnings("unused")
    public AoeSpellDelivery(ServerPlayerEntity caster, Object _unused1, Object _unused2, BlockPos center, float radius) {
        this(caster, center, radius);
    }

    public ServerPlayerEntity getCaster() { return caster; }
    public ServerWorld getWorld() { return world; }
    public BlockPos getCenter() { return center; }
    public float getRadius() { return radius; }

    public List<ModifierEffect> getModifiers() { return modifiers; }
    public void setModifiers(List<ModifierEffect> mods) {
        if (mods == null || mods.isEmpty()) {
            this.modifiers = List.of();
        } else {
            this.modifiers = List.copyOf(new ArrayList<>(mods));
        }
    }

    /** Helper mirroring original usage in Ignis: query entities in a radius AABB with a filter. */
    public <T extends Entity> List<T> getEntitiesInAabb(Class<T> type, Predicate<T> filter) {
        double cx = center.getX() + 0.5;
        double cy = center.getY() + 0.5;
        double cz = center.getZ() + 0.5;
        Box box = new Box(cx - radius, cy - 2, cz - radius, cx + radius, cy + 2, cz + radius);
        return world.getEntitiesByClass(type, box, filter);
    }
}
