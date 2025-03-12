package dev.overgrown.thaumaturge;

import dev.overgrown.thaumaturge.client.tooltip.AspectTooltipComponent;
import dev.overgrown.thaumaturge.client.tooltip.AspectTooltipData;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;

public class ThaumaturgeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof AspectTooltipData aspectData) {
                return new AspectTooltipComponent(aspectData);
            }
            return null;
        });
    }
}