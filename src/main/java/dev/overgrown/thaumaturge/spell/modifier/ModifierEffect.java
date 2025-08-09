package dev.overgrown.thaumaturge.spell.modifier;

import dev.overgrown.thaumaturge.spell.utils.SpellContext;

public interface ModifierEffect {
    void modify(SpellContext context);
}