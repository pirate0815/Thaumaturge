package dev.overgrown.thaumaturge;

import dev.overgrown.aspectslib.client.AspectsTooltipConfig;
import dev.overgrown.thaumaturge.client.keybind.KeybindManager;
import dev.overgrown.thaumaturge.client.overlay.AethericGogglesOverlay;
import dev.overgrown.thaumaturge.item.AspectLensItem;
import dev.overgrown.thaumaturge.spell.networking.SpellCastPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class ThaumaturgeClient implements ClientModInitializer {
    private static final float DEFAULT_AOE_RADIUS = 3.0f;

    @Override
    public void onInitializeClient() {
        // Tooltips visible only with lens
        AspectsTooltipConfig.addVisibilityCondition((stack, player) -> AspectLensItem.hasLens(player));

        // Register spell keybinds (original flow)
        KeybindManager.registerKeybinds();

        // Overlay
        HudRenderCallback.EVENT.register(new AethericGogglesOverlay());

        // Handle presses: Primary=Lesser(self), Secondary=Advanced(targeted), Ternary=Greater(aoe)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (KeybindManager.PRIMARY_SPELL.wasPressed()) {
                SpellCastPacket.sendSelf();
            }
            while (KeybindManager.SECONDARY_SPELL.wasPressed()) {
                SpellCastPacket.sendTargetedFromCrosshair();
            }
            while (KeybindManager.TERNARY_SPELL.wasPressed()) {
                SpellCastPacket.sendAoeFromCrosshair(DEFAULT_AOE_RADIUS);
            }
            // The remaining keys (quaternary..denary) are registered for future use, unchanged.
        });
    }
}
