package dev.overgrown.thaumaturge.spell.tier;

import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import dev.overgrown.thaumaturge.spell.utils.EnvironmentalResonance;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Delivery for targeted spells: either an entity OR a block face.
 */
public final class TargetedSpellDelivery implements SpellDelivery {

    private final ServerPlayerEntity caster;
    private final ServerWorld world;

    private final Entity targetEntity;
    private final BlockPos blockPos;
    private final Direction face;

    private List<ModifierEffect> modifiers = List.of();

    private EnvironmentalResonance.ResonanceEffect resonanceEffect;

    public void setResonance(List<EnvironmentalResonance.ResonanceEffect> resonances) {
        if (resonances != null && !resonances.isEmpty()) {
            this.resonanceEffect = resonances.get(0); // Use first resonance effect
        }
    }

    public EnvironmentalResonance.ResonanceEffect getResonanceEffect() {
        return resonanceEffect;
    }

    public boolean hasOpposingResonance(Identifier opposingAspect) {
        return resonanceEffect != null &&
                resonanceEffect.type == EnvironmentalResonance.ResonanceType.OPPOSING &&
                resonanceEffect.envAspect.equals(opposingAspect);
    }

    public double getAmplificationFactor() {
        return resonanceEffect != null &&
                resonanceEffect.type == EnvironmentalResonance.ResonanceType.AMPLIFYING ?
                resonanceEffect.factor : 1.0;
    }

    /** Entity-target constructor. */
    public TargetedSpellDelivery(ServerPlayerEntity caster, Entity targetEntity) {
        this.caster = Objects.requireNonNull(caster, "caster");
        this.world = (ServerWorld) caster.getWorld();
        this.targetEntity = Objects.requireNonNull(targetEntity, "targetEntity");
        this.blockPos = null;
        this.face = null;
    }

    /** Block-target constructor. */
    public TargetedSpellDelivery(ServerPlayerEntity caster, BlockPos blockPos, Direction face) {
        this.caster = Objects.requireNonNull(caster, "caster");
        this.world = (ServerWorld) caster.getWorld();
        this.blockPos = Objects.requireNonNull(blockPos, "blockPos");
        this.face = Objects.requireNonNull(face, "face");
        this.targetEntity = null;
    }

    // Back-compat with old callsites that passed unused context args.
    @SuppressWarnings("unused")
    public TargetedSpellDelivery(ServerPlayerEntity caster, Object _unused1, Object _unused2, Entity target) {
        this(caster, target);
    }

    public ServerPlayerEntity getCaster() { return caster; }
    public ServerWorld getWorld() { return world; }

    public boolean isEntityTarget() { return targetEntity != null; }
    public boolean isBlockTarget()  { return blockPos != null && face != null; }

    public Entity getTargetEntity() { return targetEntity; }
    public BlockPos getBlockPos()   { return blockPos; }
    public Direction getFace()      { return face; }

    public List<ModifierEffect> getModifiers() { return modifiers; }
    public void setModifiers(List<ModifierEffect> mods) {
        if (mods == null || mods.isEmpty()) {
            this.modifiers = List.of();
        } else {
            this.modifiers = List.copyOf(new ArrayList<>(mods));
        }
    }
}
