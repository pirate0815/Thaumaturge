package dev.overgrown.thaumaturge.item.modifier;

public class PowerModifierItem extends ResonanceModifierItem {
    public PowerModifierItem(Settings settings) {
        super(settings);
    }

    @Override
    public String getModifierType() {
        return "power";
    }
}