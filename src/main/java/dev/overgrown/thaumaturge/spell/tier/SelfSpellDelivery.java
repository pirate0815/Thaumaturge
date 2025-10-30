package dev.overgrown.thaumaturge.spell.tier;

import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import dev.overgrown.thaumaturge.spell.utils.EnvironmentalResonance;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Delivery for self-cast spells.
 */
public final class SelfSpellDelivery implements SpellDelivery {

    private final ServerPlayerEntity caster;
    private List<ModifierEffect> modifiers = List.of();
    private EnvironmentalResonance.ResonanceEffect resonanceEffect;

    public SelfSpellDelivery(ServerPlayerEntity caster) {
        this.caster = Objects.requireNonNull(caster, "caster");
    }

    // Back-compat with old callsites that passed unused context args.
    @SuppressWarnings("unused")
    public SelfSpellDelivery(ServerPlayerEntity caster, Object _unused1, Object _unused2) {
        this(caster);
    }

    public ServerPlayerEntity getCaster() {
        return caster;
    }

    public List<ModifierEffect> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<ModifierEffect> mods) {
        if (mods == null || mods.isEmpty()) {
            this.modifiers = List.of();
        } else {
            this.modifiers = List.copyOf(new ArrayList<>(mods));
        }
    }

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
}
