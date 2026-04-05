package dev.overgrown.thaumaturge.spell.input;

/**
 * The two raw inputs that can be chained into a spell combo.
 *
 * <p>Combos are three-input sequences, inspired by Wynncraft's cast system:
 * <pre>
 *   Slot 1 : R-L-L
 *   Slot 2 : L-R-L
 *   Slot 3 : R-L-R
 * </pre>
 *
 * <p>LEFT is sourced from any attack action (block, entity, or air swing).
 * RIGHT is sourced from {@link dev.overgrown.thaumaturge.item.gauntlet.ResonanceGauntletItem#use}.
 */
public enum GauntletInput {
    LEFT,
    RIGHT;

    /** Short label used for debug display. */
    @Override
    public String toString() {
        return this == LEFT ? "L" : "R";
    }
}