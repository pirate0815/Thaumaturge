package dev.overgrown.aspectslib.resonance;

import net.minecraft.util.Identifier;

public record Resonance(Identifier aspect1, Identifier aspect2, Type type, double factor) {
    public enum Type {
        AMPLIFYING,
        OPPOSING
    }

    public boolean matches(Identifier a1, Identifier a2) {
        return (aspect1.equals(a1) && aspect2.equals(a2)) ||
                (aspect1.equals(a2) && aspect2.equals(a1));
    }
}