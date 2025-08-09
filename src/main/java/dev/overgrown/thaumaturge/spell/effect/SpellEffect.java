package dev.overgrown.thaumaturge.spell.effect;

import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import dev.overgrown.thaumaturge.spell.modifier.ModifierRegistry;
import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.pattern.AspectRegistry;
import dev.overgrown.thaumaturge.spell.utils.SpellContext;
import net.minecraft.util.Identifier;

public class SpellEffect {
    private final Identifier aspectId;
    private final Identifier modifierId;
    private final int amplifier;
    private final AspectEffect aspectEffect;
    private final ModifierEffect modifierEffect;

    public SpellEffect(Identifier aspectId, Identifier modifierId, int amplifier) {
        this.aspectId = aspectId;
        this.modifierId = modifierId;
        this.amplifier = amplifier;
        this.aspectEffect = AspectRegistry.get(aspectId);
        this.modifierEffect = ModifierRegistry.get(modifierId);
    }

    public void apply(SpellContext context) {
        context.setAmplifier(amplifier);

        // Apply aspect effect using the context
        if (aspectEffect != null) {
            aspectEffect.apply(context);
        }

        // Apply modifier effect
        if (modifierEffect != null) {
            modifierEffect.modify(context);
        }
    }

    public Identifier aspectId() {
        return aspectId;
    }

    public Identifier modifierId() {
        return modifierId;
    }

    public int amplifier() {
        return amplifier;
    }

    public boolean hasAspect(Identifier aspect) {
        return aspectId.equals(aspect);
    }

    public boolean hasModifier(Identifier modifier) {
        return modifierId.equals(modifier);
    }
}