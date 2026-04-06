package dev.overgrown.thaumaturge.item.focus;

/** Greater Focus: High complexity capacity (50), nearly double the advanced focus. */
public class GreaterFocusItem extends BaseFocusItem {
    public GreaterFocusItem(Settings settings) {
        super(settings);
    }

    @Override
    public String getTier() {
        return "greater";
    }
}