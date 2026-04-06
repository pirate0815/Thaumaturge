package dev.overgrown.thaumaturge.item.focus;

/** Advanced Focus: Moderate complexity capacity (25). */
public class AdvancedFocusItem extends BaseFocusItem {
    public AdvancedFocusItem(Settings settings) {
        super(settings);
    }

    @Override
    public String getTier() {
        return "advanced";
    }
}