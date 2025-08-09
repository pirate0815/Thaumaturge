package dev.overgrown.thaumaturge.spell.modifier;

import dev.overgrown.thaumaturge.spell.utils.SpellContext;

public class PowerModifierEffect implements ModifierEffect {
    @Override
    public void modify(SpellContext context) {
        context.setAmplifier(context.getAmplifier() * 2); // Double the effect
    }
}