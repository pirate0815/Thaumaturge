package dev.overgrown.thaumaturge;

import dev.overgrown.aspectslib.client.AspectsTooltipConfig;
import dev.overgrown.thaumaturge.client.keybind.KeybindManager;
import dev.overgrown.thaumaturge.client.overlay.AethericGogglesOverlay;
import dev.overgrown.thaumaturge.item.AspectLensItem;
import dev.overgrown.thaumaturge.spell.networking.SpellCastPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;

public class ThaumaturgeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Add condition to show aspects when player has the lens
        AspectsTooltipConfig.addVisibilityCondition((stack, player) ->
                AspectLensItem.hasLens(player));

        // Register Spell Keybinds
        KeybindManager.registerKeybinds();

        // Register Aetheric Goggles overlay
        AspectsTooltipConfig.addVisibilityCondition((stack, player) -> AspectLensItem.hasLens(player));

        HudRenderCallback.EVENT.register(new AethericGogglesOverlay());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (KeybindManager.PRIMARY_SPELL.wasPressed()) {
                sendSpellCastPacket(Hand.MAIN_HAND, 0);
            }
            if (KeybindManager.SECONDARY_SPELL.wasPressed()) {
                sendSpellCastPacket(Hand.MAIN_HAND, 1);
            }
            if (KeybindManager.TERNARY_SPELL.wasPressed()) {
                sendSpellCastPacket(Hand.MAIN_HAND, 2);
            }
        });
    }

    private void sendSpellCastPacket(Hand hand, int spellKey) {
        PacketByteBuf buf = PacketByteBufs.create();
        new SpellCastPacket(hand, spellKey).write(buf);
        ClientPlayNetworking.send(SpellCastPacket.ID, buf);
    }
}