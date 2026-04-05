package dev.overgrown.thaumaturge.item.gauntlet;

/** Advanced Manipulation Gauntlet: 2 focus slots, combos R-L-L and L-R-L. */
public class AdvancedManipulationGauntletItem extends ResonanceGauntletItem {

    public AdvancedManipulationGauntletItem(Settings settings) {
        super(settings);
    }

    @Override
    public int getFocusSlots() {
        return 2;
    }
}