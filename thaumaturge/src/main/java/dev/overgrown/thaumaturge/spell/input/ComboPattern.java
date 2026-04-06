package dev.overgrown.thaumaturge.spell.input;

import java.util.ArrayDeque;
import java.util.List;

import static dev.overgrown.thaumaturge.spell.input.GauntletInput.*;

/**
 * A three-input sequence that maps to a gauntlet focus slot.
 *
 * <h3>Default combos</h3>
 * <table border="1">
 *   <tr><th>Pattern</th><th>Slot</th><th>Unlocked by</th></tr>
 *   <tr><td>R-L-L</td><td>0</td><td>Basic gauntlet (1-slot)</td></tr>
 *   <tr><td>L-R-L</td><td>1</td><td>Advanced gauntlet (2-slot)</td></tr>
 *   <tr><td>R-L-R</td><td>2</td><td>Engineering gauntlet (3-slot)</td></tr>
 * </table>
 */
public final class ComboPattern {

    // Canonical patterns
    /** Slot 0 — R-L-L */
    public static final ComboPattern SLOT_1 = new ComboPattern(0, "Slot 1",
            RIGHT, LEFT, LEFT);

    /** Slot 1 — L-R-L */
    public static final ComboPattern SLOT_2 = new ComboPattern(1, "Slot 2",
            LEFT, RIGHT, LEFT);

    /** Slot 2 — R-L-R */
    public static final ComboPattern SLOT_3 = new ComboPattern(2, "Slot 3",
            RIGHT, LEFT, RIGHT);

    /** All patterns, checked in order of priority. */
    public static final List<ComboPattern> ALL = List.of(SLOT_1, SLOT_2, SLOT_3);

    // Fields
    private final int slotIndex; // 0-based focus slot
    private final String name;
    private final GauntletInput[] sequence; // always length 3

    private ComboPattern(int slotIndex, String name, GauntletInput... sequence) {
        this.slotIndex = slotIndex;
        this.name = name;
        this.sequence = sequence;
    }

    // Public API
    /** The 0-based focus slot this pattern activates. */
    public int getSlotIndex() { return slotIndex; }

    public String getName() { return name; }

    /**
     * Returns {@code true} if the last 3 inputs in {@code recent} (tail = newest)
     * match this pattern, and the pattern's required slot is within {@code maxSlots}.
     *
     * @param recent    deque of recent inputs; only the last 3 are examined
     * @param maxSlots  number of active slots in the gauntlet (≥ slotIndex+1 required)
     */
    public boolean matches(ArrayDeque<GauntletInput> recent, int maxSlots) {
        if (slotIndex >= maxSlots) return false;
        if (recent.size() < 3) return false;

        // Collect last 3 as array (tail = most recent)
        Object[] arr = recent.toArray();
        int n = arr.length;
        return arr[n - 3] == sequence[0]
                && arr[n - 2] == sequence[1]
                && arr[n - 1] == sequence[2];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name).append(" [");
        for (int i = 0; i < sequence.length; i++) {
            sb.append(sequence[i]);
            if (i < sequence.length - 1) sb.append('-');
        }
        return sb.append(']').toString();
    }
}