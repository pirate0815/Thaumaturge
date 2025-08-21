package dev.overgrown.thaumaturge;

import dev.overgrown.aspectslib.client.AspectsTooltipConfig;
import dev.overgrown.thaumaturge.client.keybind.KeybindManager;
import dev.overgrown.thaumaturge.client.overlay.AethericGogglesOverlay;
import dev.overgrown.thaumaturge.entity.ModEntities;
import dev.overgrown.thaumaturge.item.AspectLensItem;
import dev.overgrown.thaumaturge.registry.ModBlocks;
import dev.overgrown.thaumaturge.spell.impl.potentia.render.SpellBoltRenderer;
import dev.overgrown.thaumaturge.spell.networking.SpellCastPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.render.RenderLayer;

public class ThaumaturgeClient implements ClientModInitializer {
    private static final float DEFAULT_AOE_RADIUS = 3.0f;

    @Override
    public void onInitializeClient() {
        // Entity Renderers
        EntityRendererRegistry.register(ModEntities.SPELL_BOLT, SpellBoltRenderer::new);

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
                SpellCastPacket.send(SpellCastPacket.KeyType.PRIMARY);
            }
            while (KeybindManager.SECONDARY_SPELL.wasPressed()) {
                SpellCastPacket.send(SpellCastPacket.KeyType.SECONDARY);
            }
            while (KeybindManager.TERNARY_SPELL.wasPressed()) {
                SpellCastPacket.send(SpellCastPacket.KeyType.TERNARY);
            }
        });
    }
}
