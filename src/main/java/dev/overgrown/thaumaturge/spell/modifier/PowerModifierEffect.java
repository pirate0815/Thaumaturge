package dev.overgrown.thaumaturge.spell.modifier;

/**
 * Simple "increase potency" modifier.
 * Aspects can detect presence of this modifier and scale numbers by {@link #getMultiplier()}.
 *
 * Example usage inside an AspectEffect:
 *   float mult = delivery.getModifiers().stream()
 *       .filter(m -> m instanceof PowerModifierEffect)
 *       .map(m -> ((PowerModifierEffect) m).getMultiplier())
 *       .findFirst().orElse(1.0f);
 *   float damage = baseDamage * mult;
 */
public final class PowerModifierEffect implements ModifierEffect {
    private final float multiplier;

    /** Default 1.25x power. */
    public PowerModifierEffect() {
        this(1.25f);
    }

    /** Custom multiplier for future configurability. */
    public PowerModifierEffect(float multiplier) {
        // Keep sane bounds; aspects can clamp too.
        this.multiplier = Math.max(0.0f, Math.min(multiplier, 10.0f));
    }

    public float getMultiplier() {
        return multiplier;
    }
}
