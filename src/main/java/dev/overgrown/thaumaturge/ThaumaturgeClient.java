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
        KeybindManager.registerKeybinds();
        ThaumaturgeModPacketsS2C.register();

        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof AspectTooltipData aspectData) {
                return new AspectTooltipComponent(aspectData);
            }
            return null;
        });

        AethericGogglesRenderer.init();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) return;

            // Handle primary key for Aer/Motus combination or individual spells
            if (KeybindManager.PRIMARY_SPELL.wasPressed()) {
                boolean hasAer = hasFoci(player, Registries.ITEM.getId(ModItems.LESSER_AER_FOCI));
                boolean hasMotus = hasFoci(player, Registries.ITEM.getId(ModItems.LESSER_MOTUS_FOCI));

                if (hasAer || hasMotus) {
                    SpellCastPacket.Type type = hasAer ? SpellCastPacket.Type.LESSER_AER : SpellCastPacket.Type.LESSER_MOTUS;
                    ClientPlayNetworking.send(new SpellCastPacket(type));
                }
            }

            checkAndSendSpell(KeybindManager.SECONDARY_SPELL, ModItems.ADVANCED_AER_FOCI, SpellCastPacket.Type.ADVANCED_AER, player);
            checkAndSendSpell(KeybindManager.TERNARY_SPELL, ModItems.GREATER_AER_FOCI, SpellCastPacket.Type.GREATER_AER, player);
        });
    }

    private void checkAndSendSpell(KeyBinding keyBinding, Item fociItem, SpellCastPacket.Type type, PlayerEntity player) {
        if (keyBinding.wasPressed()) {
            if (hasFoci(player, Registries.ITEM.getId(fociItem))) {
                ClientPlayNetworking.send(new SpellCastPacket(type));
            }
        }
    }

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