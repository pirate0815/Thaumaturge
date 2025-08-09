package dev.overgrown.thaumaturge.spell.tier;

import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Delivery data for targeted spells. Target can be either an entity or a block face.
 * Carries the caster, resolved aspect, and modifiers.
 */
public final class TargetedSpellDelivery {
    private final ServerPlayerEntity caster;

    private final @Nullable AspectEffect aspect;
    private final List<ModifierEffect> modifiers;

    // Exactly one of these is non-null:
    private final @Nullable Entity targetEntity;
    private final @Nullable BlockPos blockPos;
    private final @Nullable Direction face;

    /* ======================= Constructors ======================= */

    /** Entity-target constructor. aspect/modifiers may be null; they'll be normalized. */
    public TargetedSpellDelivery(ServerPlayerEntity caster,
                                 @Nullable AspectEffect aspect,
                                 @Nullable List<ModifierEffect> modifiers,
                                 Entity targetEntity) {
        this.caster = caster;
        this.aspect = aspect;
        this.modifiers = (modifiers == null) ? Collections.emptyList() : List.copyOf(modifiers);
        this.targetEntity = targetEntity;
        this.blockPos = null;
        this.face = null;
    }

    /** Block-target constructor. aspect/modifiers may be null; they'll be normalized. */
    public TargetedSpellDelivery(ServerPlayerEntity caster,
                                 @Nullable AspectEffect aspect,
                                 @Nullable List<ModifierEffect> modifiers,
                                 BlockPos blockPos,
                                 Direction face) {
        this.caster = caster;
        this.aspect = aspect;
        this.modifiers = (modifiers == null) ? Collections.emptyList() : List.copyOf(modifiers);
        this.targetEntity = null;
        this.blockPos = blockPos;
        this.face = face;
    }

    /* ======================= Context binder ======================= */

    /**
     * Returns a new delivery with the same target but the provided aspect/modifiers.
     * Useful when networking passes only the target and the server resolves context.
     */
    public TargetedSpellDelivery withContext(AspectEffect aspect, List<ModifierEffect> modifiers) {
        if (this.targetEntity != null) {
            return new TargetedSpellDelivery(this.caster, aspect, modifiers, this.targetEntity);
        }
        return new TargetedSpellDelivery(this.caster, aspect, modifiers, this.blockPos, this.face);
    }

    /* ======================= Accessors ======================= */

    public ServerPlayerEntity getCaster() { return caster; }

    public @Nullable AspectEffect getAspect() { return aspect; }

    public List<ModifierEffect> getModifiers() { return modifiers; }

    public boolean isEntityTarget() { return targetEntity != null; }

    public boolean isBlockTarget() { return blockPos != null && face != null; }

    public @Nullable Entity getTargetEntity() { return targetEntity; }

    public @Nullable BlockPos getBlockPos() { return blockPos; }

    public @Nullable Direction getFace() { return face; }

    /** World convenience. */
    public net.minecraft.server.world.ServerWorld getWorld() {
        return caster.getServerWorld();
    }

    /** Caster eye position convenience. */
    public Vec3d getCasterEyePos() {
        return new Vec3d(caster.getX(), caster.getEyeY(), caster.getZ());
    }

    /**
     * Approximate hit position for block targets (center of the targeted face).
     * Returns null for entity targets.
     */
    public @Nullable Vec3d getBlockHitPos() {
        if (!isBlockTarget()) return null;
        // center of the block, nudged half a block toward the face normal
        Vec3d center = Vec3d.ofCenter(blockPos);
        Vec3d n = new Vec3d(face.getOffsetX(), face.getOffsetY(), face.getOffsetZ());
        return center.add(n.multiply(0.5));
    }
}
