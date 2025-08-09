package dev.overgrown.thaumaturge.spell.effect;

import dev.overgrown.thaumaturge.spell.utils.SpellContext;
import net.minecraft.util.Identifier;

/**
 * Backport-safe holder for a spell triplet (aspect id, modifier id, amplifier).
 *
 * In the 1.20.1 backport, actual execution is centralized in SpellHandler +
 * delivery classes using AspectsLib/registries. This class no longer resolves
 * or invokes Aspect/Modifier effects directly.
 */
public class SpellEffect {
    private final Identifier aspectId;
    private final Identifier modifierId;
    private final int amplifier;

    public SpellEffect(Identifier aspectId, Identifier modifierId, int amplifier) {
        this.aspectId = aspectId;
        this.modifierId = modifierId;
        this.amplifier = amplifier;
    }

    /** Sets the amplifier on the provided context; execution happens elsewhere. */
    public void apply(SpellContext context) {
        if (context != null) {
            context.setAmplifier(amplifier);
        }
        // No direct aspect/modifier calls here in the backport.
    }

    public Identifier aspectId() { return aspectId; }
    public Identifier modifierId() { return modifierId; }
    public int amplifier() { return amplifier; }

    public boolean hasAspect(Identifier aspect) {
        return aspect != null && aspect.equals(this.aspectId);
    }

    public boolean hasModifier(Identifier modifier) {
        return modifier != null && modifier.equals(this.modifierId);
    }
}
