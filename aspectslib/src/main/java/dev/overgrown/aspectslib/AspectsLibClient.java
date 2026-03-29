package dev.overgrown.aspectslib;

import dev.overgrown.aspectslib.aspects.client.AspectsTooltipConfig;
import dev.overgrown.aspectslib.aspects.client.tooltip.AspectTooltipComponent;
import dev.overgrown.aspectslib.aspects.client.tooltip.AspectTooltipData;
import dev.overgrown.aspectslib.aspects.data.*;
import dev.overgrown.aspectslib.entity.aura_node.client.AuraNodeVisibilityConfig;
import dev.overgrown.aspectslib.aspects.networking.SyncAspectIdentifierPacket;
import dev.overgrown.aspectslib.registry.ModEntities;
import dev.overgrown.aspectslib.entity.aura_node.render.AuraNodeRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class AspectsLibClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Initialize default tooltip visibility to shown
        AspectsTooltipConfig.setAlwaysShow(false);

        // Initialize aura node visibility to hidden by default
        // No conditions are set by default - other mods must add them via API
        AuraNodeVisibilityConfig.setAlwaysShow(false);

        EntityRendererRegistry.register(ModEntities.AURA_NODE, AuraNodeRenderer::new);

        // Register custom tooltip component
        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof AspectTooltipData aspectTooltipData) {
                return new AspectTooltipComponent(aspectTooltipData);
            }
            return null;
        });

        // Handle aspect data sync from server
        ClientPlayNetworking.registerGlobalReceiver(SyncAspectIdentifierPacket.ID, (client, handler, buf, responseSender) -> {
            try {
                Map<String, Identifier> nameMap = SyncAspectIdentifierPacket.readNameMap(buf);
                Map<Identifier, Aspect> aspectMap;

                // Handle legacy packet format
                if (buf.readableBytes() > 0) {
                    aspectMap = SyncAspectIdentifierPacket.readAspectData(buf);
                } else {
                    aspectMap = new HashMap<>();
                    AspectsLib.LOGGER.warn("Received legacy packet format - only name mapping synced");
                }

                final Map<String, Identifier> finalNameMap = nameMap;
                final Map<Identifier, Aspect> finalAspectMap = aspectMap;

                // Apply received data on client thread
                client.execute(() -> {
                    AspectManager.NAME_TO_ID.clear();
                    AspectManager.NAME_TO_ID.putAll(finalNameMap);

                    if (!finalAspectMap.isEmpty()) {
                        ModRegistries.ASPECTS.clear();
                        ModRegistries.ASPECTS.putAll(finalAspectMap);
                    }

                    AspectsLib.LOGGER.info("Synced {} aspects from server (name mappings: {})",
                            finalAspectMap.size(), finalNameMap.size());
                });
            } catch (Exception e) {
                AspectsLib.LOGGER.error("Failed to read aspect sync packet: {}", e.getMessage());
            }
        });

        AspectsLib.LOGGER.info("AspectsLib Client initialized!");
    }
}