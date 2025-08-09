package dev.overgrown.thaumaturge.spell.tier;

import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect; // corrected package
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.Objects;

/**
 * Delivery strategy for targeted (entity) spells.
 * Server-only execution; networking already validated range and entity aliveness.
 */
public final class TargetedSpellDelivery {

    private final ServerPlayerEntity caster;
    private final Entity target;

    public TargetedSpellDelivery(ServerPlayerEntity caster, Entity target) {
        this.caster = Objects.requireNonNull(caster, "caster");
        this.target = Objects.requireNonNull(target, "target");
    }

    // Back-compat with older call sites that passed unused context args.
    @SuppressWarnings("unused")
    public TargetedSpellDelivery(ServerPlayerEntity caster, Object _unused1, Object _unused2, Entity target) {
        this(caster, target);
    }

    public void deliver(AspectEffect aspect, List<ModifierEffect> modifiers) {
        if (target.isRemoved() || !target.isAlive()) return;
        aspect.castOnEntity(caster, target, modifiers == null ? List.of() : modifiers);
    }

    public ServerPlayerEntity getCaster() { return caster; }
    public Entity getTarget() { return target; }
}
