package dev.overgrown.thaumaturge.spell.tier;

import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Objects;

/**
 * Delivery strategy for area-of-effect spells.
 * Server-side only; networking already validated chunk/border and range.
 */
public final class AoeSpellDelivery {

    private final ServerPlayerEntity caster;
    private final BlockPos center;
    private final float radius;

    public AoeSpellDelivery(ServerPlayerEntity caster, BlockPos center, float radius) {
        this.caster = Objects.requireNonNull(caster, "caster");
        this.center = Objects.requireNonNull(center, "center");
        this.radius = Math.max(0f, Math.min(radius, 32f));
    }

    // Back-compat with older call sites that passed unused context args.
    @SuppressWarnings("unused")
    public AoeSpellDelivery(ServerPlayerEntity caster, Object _unused1, Object _unused2, BlockPos center, float radius) {
        this(caster, center, radius);
    }

    public void deliver(AspectEffect aspect, List<ModifierEffect> modifiers) {
        aspect.castAoe(caster, center, radius, modifiers == null ? List.of() : modifiers);
    }

    public ServerPlayerEntity getCaster() { return caster; }
    public BlockPos getCenter() { return center; }
    public float getRadius() { return radius; }
}
