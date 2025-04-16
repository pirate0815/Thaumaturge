package dev.overgrown.thaumaturge;

import dev.overgrown.thaumaturge.client.keybind.KeybindManager;
import dev.overgrown.thaumaturge.client.tooltip.AspectTooltipComponent;
import dev.overgrown.thaumaturge.client.tooltip.AspectTooltipData;
import dev.overgrown.thaumaturge.item.aetheric_goggles.AethericGogglesRenderer;
import dev.overgrown.thaumaturge.networking.ThaumaturgeModPacketsS2C;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;

public class ThaumaturgeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        // Register keybinds
        KeybindManager.registerKeybinds();

        ThaumaturgeModPacketsS2C.register();

        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof AspectTooltipData aspectData) {
                return new AspectTooltipComponent(aspectData);
            }
            return null;
        });

        AethericGogglesRenderer.init();
    }
}