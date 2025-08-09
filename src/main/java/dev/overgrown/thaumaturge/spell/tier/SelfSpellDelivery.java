package dev.overgrown.thaumaturge.spell.tier;

import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Delivery data for self-cast spells.
 * Carries the caster, resolved aspect, and modifiers.
 */
public final class SelfSpellDelivery {
    private final ServerPlayerEntity caster;
    private final @Nullable AspectEffect aspect;
    private final List<ModifierEffect> modifiers;

    public SelfSpellDelivery(ServerPlayerEntity caster,
                             @Nullable AspectEffect aspect,
                             @Nullable List<ModifierEffect> modifiers) {
        this.caster = caster;
        this.aspect = aspect;
        this.modifiers = (modifiers == null) ? Collections.emptyList() : List.copyOf(modifiers);
    }

    /** Returns a new delivery with the same caster but the provided aspect/modifiers. */
    public SelfSpellDelivery withContext(AspectEffect aspect, List<ModifierEffect> modifiers) {
        return new SelfSpellDelivery(this.caster, aspect, modifiers);
    }

    // --- Accessors ---

    public ServerPlayerEntity getCaster() { return caster; }

    public @Nullable AspectEffect getAspect() { return aspect; }

    public List<ModifierEffect> getModifiers() { return modifiers; }

    public ServerWorld getWorld() { return caster.getServerWorld(); }

    public Vec3d getCasterPos() { return caster.getPos(); }

    public Vec3d getCasterEyePos() { return new Vec3d(caster.getX(), caster.getEyeY(), caster.getZ()); }

    public Vec3d getLookVector() { return caster.getRotationVec(1.0F); }
}
