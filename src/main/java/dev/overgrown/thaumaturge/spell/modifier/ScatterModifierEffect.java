package dev.overgrown.thaumaturge.spell.modifier;

import dev.overgrown.thaumaturge.spell.utils.SpellContext;

public class ScatterModifierEffect implements ModifierEffect {
    @Override
    public void modify(SpellContext context) {
        int projectiles = 3 + context.getAmplifier();
        // TODO: Logic to create multiple projectiles
    }
}
