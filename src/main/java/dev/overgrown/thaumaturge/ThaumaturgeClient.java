package dev.overgrown.thaumaturge;

import dev.overgrown.thaumaturge.client.keybind.KeybindManager;
import dev.overgrown.thaumaturge.client.tooltip.AspectTooltipComponent;
import dev.overgrown.thaumaturge.client.tooltip.AspectTooltipData;
import dev.overgrown.thaumaturge.component.GauntletComponent;
import dev.overgrown.thaumaturge.component.ModComponents;
import dev.overgrown.thaumaturge.entity.ModEntities;
import dev.overgrown.thaumaturge.item.aetheric_goggles.AethericGogglesRenderer;
import dev.overgrown.thaumaturge.networking.SpellCastPacket;
import dev.overgrown.thaumaturge.networking.ThaumaturgeModPacketsS2C;
import dev.overgrown.thaumaturge.spell.impl.potentia.render.SpellBoltRenderer;
import dev.overgrown.thaumaturge.spell.impl.metallum.render.MetalShardRenderer;
import net.minecraft.client.render.entity.EmptyEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public class ThaumaturgeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.SPELL_BOLT, SpellBoltRenderer::new);
        EntityRendererRegistry.register(ModEntities.ARCANE_MINE, EmptyEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.METAL_SHARD, MetalShardRenderer::new);

        // Register all keybinds used for spell casting
        KeybindManager.registerKeybinds();

        // Register client-side packet handlers to receive data from server
        ThaumaturgeModPacketsS2C.register();

        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof AspectTooltipData aspectData) {
                return new AspectTooltipComponent(aspectData);
            }
            return null;
        });

        AethericGogglesRenderer.init();

        // Register end-of-tick event to check for spell keybind presses
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) return;

            if (KeybindManager.PRIMARY_SPELL.wasPressed()) {
                ClientPlayNetworking.send(new SpellCastPacket(SpellCastPacket.SpellTier.LESSER));
            }
            if (KeybindManager.SECONDARY_SPELL.wasPressed()) {
                ClientPlayNetworking.send(new SpellCastPacket(SpellCastPacket.SpellTier.ADVANCED));
            }
            if (KeybindManager.TERNARY_SPELL.wasPressed()) {
                ClientPlayNetworking.send(new SpellCastPacket(SpellCastPacket.SpellTier.GREATER));
            }
        });
    }

    private boolean hasFoci(PlayerEntity player, Identifier fociId) {
        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.contains(ModComponents.MAX_FOCI)) {
                GauntletComponent component = stack.getOrDefault(ModComponents.GAUNTLET_STATE, GauntletComponent.DEFAULT);
                for (GauntletComponent.FociEntry entry : component.entries()) {
                    if (entry.aspectId().equals(fociId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}