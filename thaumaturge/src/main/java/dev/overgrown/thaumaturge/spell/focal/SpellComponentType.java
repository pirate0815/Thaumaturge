package dev.overgrown.thaumaturge.spell.focal;

public enum SpellComponentType {
    MEDIUM,
    EFFECT,
    MODIFIER;

    public static SpellComponentType fromString(String s) {
        return switch (s.toLowerCase()) {
            case "medium" -> MEDIUM;
            case "effect" -> EFFECT;
            case "modifier" -> MODIFIER;
            default -> throw new IllegalArgumentException("Unknown component type: " + s);
        };
    }
}