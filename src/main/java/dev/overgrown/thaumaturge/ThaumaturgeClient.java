package dev.overgrown.thaumaturge;

import dev.overgrown.aspectslib.client.AspectsTooltipConfig;
import dev.overgrown.thaumaturge.client.AethericGogglesOverlay;
import dev.overgrown.thaumaturge.item.AspectLensItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class ThaumaturgeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Add condition to show aspects when player has the lens
        AspectsTooltipConfig.addVisibilityCondition((stack, player) ->
                AspectLensItem.hasLens(player));

        // Register Aetheric Goggles overlay
        HudRenderCallback.EVENT.register(new AethericGogglesOverlay());
    }
}