package dev.overgrown.thaumaturge.item.focus;

/** Lesser Focus: Limited complexity capacity (10). */
public class LesserFocusItem extends BaseFocusItem {
    public LesserFocusItem(Settings settings) {
        super(settings);
    }

    @Override
    public String getTier() {
        return "lesser";
    }
}