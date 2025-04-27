/**
 * ThaumaturgeClient.java
 * <p>
 * Client-side initialization and management for the Thaumaturge mod.
 * This file handles client-specific features including:
 * - Keybindings for spell casting
 * - Tooltip components for aspects
 * - Network packet registration for client-server communication
 * - Client tick event handling for spell activation
 *
 * @see dev.overgrown.thaumaturge.Thaumaturge - Server-side counterpart
 * @see dev.overgrown.thaumaturge.client.keybind.KeybindManager - Keybinding registration
 */
package dev.overgrown.thaumaturge;

import dev.overgrown.thaumaturge.client.keybind.KeybindManager;
import dev.overgrown.thaumaturge.client.tooltip.AspectTooltipComponent;
import dev.overgrown.thaumaturge.client.tooltip.AspectTooltipData;
import dev.overgrown.thaumaturge.component.GauntletComponent;
import dev.overgrown.thaumaturge.component.ModComponents;
import dev.overgrown.thaumaturge.item.ModItems;
import dev.overgrown.thaumaturge.item.aetheric_goggles.AethericGogglesRenderer;
import dev.overgrown.thaumaturge.networking.SpellCastPacket;
import dev.overgrown.thaumaturge.networking.ThaumaturgeModPacketsS2C;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public class ThaumaturgeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
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

            // Handle primary key for Aer/Motus combination or individual spells
            // When both foci are equipped, it will use a combination spell
            if (KeybindManager.PRIMARY_SPELL.wasPressed()) {
                boolean hasAqua = hasFoci(player, Registries.ITEM.getId(ModItems.LESSER_AQUA_FOCI));
                boolean hasGelum = hasFoci(player, Registries.ITEM.getId(ModItems.LESSER_GELUM_FOCI));

                if (hasAqua && hasGelum) {
                    // The server will handle the combination automatically
                    ClientPlayNetworking.send(new SpellCastPacket(SpellCastPacket.Type.LESSER_AQUA));
                } else if (hasAqua) {
                    ClientPlayNetworking.send(new SpellCastPacket(SpellCastPacket.Type.LESSER_AQUA));
                } else {
                    // Existing Aer/Motus check
                    boolean hasAer = hasFoci(player, Registries.ITEM.getId(ModItems.LESSER_AER_FOCI));
                    boolean hasMotus = hasFoci(player, Registries.ITEM.getId(ModItems.LESSER_MOTUS_FOCI));

                    if (hasAer || hasMotus) {
                        SpellCastPacket.Type type = hasAer ? SpellCastPacket.Type.LESSER_AER : SpellCastPacket.Type.LESSER_MOTUS;
                        ClientPlayNetworking.send(new SpellCastPacket(type));
                    }
                }
            }

            // Check other keybinds for advanced and greater tier spells
            checkAndSendSpell(KeybindManager.SECONDARY_SPELL, ModItems.ADVANCED_AER_FOCI, SpellCastPacket.Type.ADVANCED_AER, player);
            checkAndSendSpell(KeybindManager.TERNARY_SPELL, ModItems.GREATER_AER_FOCI, SpellCastPacket.Type.GREATER_AER, player);
        });
    }

    /**
     * Helper method to check if a keybind was pressed and send the corresponding spell packet
     *
     * @param keyBinding The keybind to check
     * @param fociItem The foci item required for this spell
     * @param type The type of spell to cast
     * @param player The player casting the spell
     */
    private void checkAndSendSpell(KeyBinding keyBinding, Item fociItem, SpellCastPacket.Type type, PlayerEntity player) {
        if (keyBinding.wasPressed()) {
            if (hasFoci(player, Registries.ITEM.getId(fociItem))) {
                ClientPlayNetworking.send(new SpellCastPacket(type));
            }
        }
    }

    /**
     * Checks if a player has a specific foci item equipped in either hand's gauntlet
     *
     * @param player The player to check for equipped foci
     * @param fociId The identifier of the foci to check for
     * @return true if the foci is equipped in either hand's gauntlet
     */
    private boolean hasFoci(PlayerEntity player, Identifier fociId) {
        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.contains(ModComponents.MAX_FOCI)) {
                GauntletComponent component = stack.getOrDefault(ModComponents.GAUNTLET_STATE, GauntletComponent.DEFAULT);
                if (component.fociIds().contains(fociId)) {
                    return true;
                }
            }
        }
        return false;
    }
}