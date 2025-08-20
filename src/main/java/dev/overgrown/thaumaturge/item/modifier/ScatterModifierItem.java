package dev.overgrown.thaumaturge.item.modifier;

public class ScatterModifierItem extends ResonanceModifierItem {
    public ScatterModifierItem(Settings settings) {
        super(settings);
    }

    @Override
    public String getModifierType() {
        return "scatter";
    }
}