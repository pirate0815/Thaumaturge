package dev.overgrown.thaumaturge.spell.modifier;

/**
 * Marker interface for spell modifiers in the 1.20.1 backport.
 * Aspect effects inspect the list of modifiers directly (e.g., instanceof PowerModifierEffect).
 * No hooks here; execution is centralized in AspectEffect implementations.
 */
public interface ModifierEffect {
    // marker only
}
