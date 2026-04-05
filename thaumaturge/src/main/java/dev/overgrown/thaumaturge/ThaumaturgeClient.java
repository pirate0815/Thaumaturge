package dev.overgrown.thaumaturge;

import dev.overgrown.aspectslib.aspects.client.AspectsTooltipConfig;
import dev.overgrown.thaumaturge.block.pedestal.client.PedestalBlockEntityRenderer;
import dev.overgrown.thaumaturge.block.focal_manipulator.screen.FocalManipulatorScreen;
import dev.overgrown.thaumaturge.client.render.JarBlockEntityRenderer;
import dev.overgrown.thaumaturge.client.render.VesselBlockEntityRenderer;
import dev.overgrown.thaumaturge.client.screen.AlchemicalFurnaceScreen;
import dev.overgrown.thaumaturge.client.visualisation.FaucetTransferVisualisationHandler;
import dev.overgrown.thaumaturge.networking.FaucetTransferVisualisation;
import dev.overgrown.thaumaturge.client.render.AuraNodeVisibility;
import dev.overgrown.thaumaturge.item.aetheric_goggles.overlay.AethericGogglesOverlay;
import dev.overgrown.thaumaturge.item.apophenia.predicate.ApopheniaModelProvider;
import dev.overgrown.thaumaturge.registry.ModBlocks;
import dev.overgrown.thaumaturge.registry.ModEntities;
import dev.overgrown.thaumaturge.item.aspect_lens.AspectLensItem;
import dev.overgrown.thaumaturge.registry.ModItems;
import dev.overgrown.thaumaturge.registry.ModScreens;
import dev.overgrown.thaumaturge.spell.impl.potentia.render.SpellBoltRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.EmptyEntityRenderer;
import net.minecraft.item.DyeableItem;

public class ThaumaturgeClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        // Register color provider for the gauntlet
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            if (tintIndex > 0) {
                return -1; // No tint for other layers
            }
            return ((DyeableItem) stack.getItem()).getColor(stack);
        }, ModItems.BASIC_CASTING_GAUNTLET);

        // Registers the Apophenia model predicate
        ApopheniaModelProvider.register();

        // Entity Renderers
        EntityRendererRegistry.register(ModEntities.SPELL_BOLT, SpellBoltRenderer::new);
        EntityRendererRegistry.register(ModEntities.ARCANE_MINE, EmptyEntityRenderer::new);
        BlockEntityRendererFactories.register(ModBlocks.PEDESTAL_BE, PedestalBlockEntityRenderer::new);

        // Tooltips visible only with lens
        AspectsTooltipConfig.addVisibilityCondition((stack, player) -> AspectLensItem.hasLens(player));

        // Aetheric Goggles Overlay
        HudRenderCallback.EVENT.register(new AethericGogglesOverlay());

        // Register Aura Node visibility
        AuraNodeVisibility.initialize();

        ClientPlayNetworking.registerGlobalReceiver(FaucetTransferVisualisation.ASPECT_TRANSFER_PAKET, FaucetTransferVisualisationHandler::receive);

        BlockEntityRendererFactories.register(ModBlocks.VESSEL_BLOCK_ENTITY, VesselBlockEntityRenderer::new);

        // Jar Rendering
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.JAR, RenderLayer.getCutout());
        BlockEntityRendererFactories.register(ModBlocks.JAR_BLOCK_ENTITY, JarBlockEntityRenderer::new);

        // Alchemical Furnace Screen
        HandledScreens.register(ModScreens.ALCHEMICAL_FURNACE_SCREEN_HANDLER, AlchemicalFurnaceScreen::new);

        // Focal Manipulator Screen
        HandledScreens.register(ModScreens.FOCAL_MANIPULATOR, FocalManipulatorScreen::new);
    }
}