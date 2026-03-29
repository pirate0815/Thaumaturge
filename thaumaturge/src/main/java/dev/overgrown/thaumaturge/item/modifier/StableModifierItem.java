package dev.overgrown.thaumaturge.item.modifier;

public class StableModifierItem extends ResonanceModifierItem {
    public StableModifierItem(Settings settings) {
        super(settings);
    }

    @Override
    public String getModifierType() {
        return "stable";
    }
}