package dev.overgrown.thaumaturge;

import dev.overgrown.aspectslib.client.AspectsTooltipConfig;
import dev.overgrown.thaumaturge.client.overlay.AethericGogglesOverlay;
import dev.overgrown.thaumaturge.item.AspectLensItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class ThaumaturgeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Show AspectsLib tooltips only when the player has the lens equipped
        AspectsTooltipConfig.addVisibilityCondition((stack, player) -> AspectLensItem.hasLens(player));

        // Aetheric Goggles overlay
        HudRenderCallback.EVENT.register(new AethericGogglesOverlay());

    }
}
