package dev.overgrown.thaumaturge.spell.utils;

import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import dev.overgrown.thaumaturge.spell.modifier.PowerModifierEffect;
import net.minecraft.util.Identifier;

import java.util.*;

public class EnvironmentalResonance {
    private final Map<Identifier, List<ResonanceEffect>> aspectEffects = new HashMap<>();

    public void addOpposingEffect(Identifier spellAspect, Identifier envAspect, double factor) {
        aspectEffects.computeIfAbsent(spellAspect, k -> new ArrayList<>())
                .add(new ResonanceEffect(envAspect, ResonanceType.OPPOSING, factor));
    }

    public void addAmplifyingEffect(Identifier spellAspect, Identifier envAspect, double factor) {
        aspectEffects.computeIfAbsent(spellAspect, k -> new ArrayList<>())
                .add(new ResonanceEffect(envAspect, ResonanceType.AMPLIFYING, factor));
    }

    public List<ResonanceEffect> getResonanceForAspect(Identifier aspect) {
        return aspectEffects.getOrDefault(aspect, Collections.emptyList());
    }

    public List<ModifierEffect> applyToModifiers(Identifier aspect, List<ModifierEffect> originalModifiers) {
        List<ResonanceEffect> resonances = getResonanceForAspect(aspect);
        if (resonances.isEmpty()) {
            return originalModifiers;
        }

        List<ModifierEffect> modified = new ArrayList<>(originalModifiers);

        for (ResonanceEffect resonance : resonances) {
            if (resonance.type == ResonanceType.AMPLIFYING) {
                // Add power modifier for amplification
                modified.add(new PowerModifierEffect((float) resonance.factor));
            } else if (resonance.type == ResonanceType.OPPOSING) {
                // For opposing aspects, we might reduce effectiveness or transform the effect
                // This will be handled in the individual aspect effects
            }
        }

        return modified;
    }

    public boolean hasOpposingEffect(Identifier aspect, Identifier opposingAspect) {
        return getResonanceForAspect(aspect).stream()
                .anyMatch(e -> e.envAspect.equals(opposingAspect) && e.type == ResonanceType.OPPOSING);
    }

    public double getAmplificationFactor(Identifier aspect) {
        return getResonanceForAspect(aspect).stream()
                .filter(e -> e.type == ResonanceType.AMPLIFYING)
                .mapToDouble(e -> e.factor)
                .average()
                .orElse(1.0);
    }

    public static class ResonanceEffect {
        public final Identifier envAspect;
        public final ResonanceType type;
        public final double factor;

        public ResonanceEffect(Identifier envAspect, ResonanceType type, double factor) {
            this.envAspect = envAspect;
            this.type = type;
            this.factor = factor;
        }
    }

    public enum ResonanceType {
        OPPOSING,
        AMPLIFYING
    }
}