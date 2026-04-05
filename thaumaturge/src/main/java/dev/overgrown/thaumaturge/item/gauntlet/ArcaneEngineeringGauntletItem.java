package dev.overgrown.thaumaturge.item.gauntlet;

/** Arcane Engineering Gauntlet: 3 focus slots, combos R-L-L, L-R-L, R-L-R. */
public class ArcaneEngineeringGauntletItem extends ResonanceGauntletItem {

    public ArcaneEngineeringGauntletItem(Settings settings) {
        super(settings);
    }

    @Override
    public int getFocusSlots() {
        return 3;
    }
}