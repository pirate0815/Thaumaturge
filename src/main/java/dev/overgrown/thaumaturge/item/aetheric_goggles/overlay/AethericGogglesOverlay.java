package dev.overgrown.thaumaturge.item.aetheric_goggles.overlay;

import dev.overgrown.aspectslib.api.IAspectAffinityEntity;
import dev.overgrown.aspectslib.data.Aspect;
import dev.overgrown.aspectslib.data.AspectData;
import dev.overgrown.aspectslib.data.ModRegistries;
import dev.overgrown.thaumaturge.block.vessel.VesselBlock;
import dev.overgrown.thaumaturge.block.vessel.entity.VesselBlockEntity;
import dev.overgrown.thaumaturge.item.aetheric_goggles.AethericGogglesItem;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import java.util.Map;

public class AethericGogglesOverlay implements HudRenderCallback {
    private static final int ASPECT_ICON_SIZE = 16;
    private static final int ASPECT_TEXT_OFFSET = 18;
    private static final int ASPECT_SPACING = 4;
    private static final int CROSSHAIR_OFFSET_Y = -30;
    private static final int TEXT_Y_OFFSET = 5;

    @Override
    public void onHudRender(DrawContext drawContext, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Only render if wearing goggles
        if (!AethericGogglesItem.isWearingGoggles(client.player)) return;

        HitResult hit = client.crosshairTarget;
        if (hit == null || hit.getType() == HitResult.Type.MISS) return;

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int x = screenWidth / 2;
        int y = screenHeight / 2 + CROSSHAIR_OFFSET_Y;

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            BlockPos pos = blockHit.getBlockPos();
            BlockState state = client.world.getBlockState(pos);
            
            if (state.getBlock() instanceof VesselBlock) {
                if (client.world.getBlockEntity(pos) instanceof VesselBlockEntity vessel) {
                    Map<String, Integer> aspects = vessel.getAspects();
                    if (!aspects.isEmpty()) {
                        renderVesselAspects(drawContext, aspects, x, y);
                        return;
                    }
                }
            }
        }

        AspectData aspectData = getAspectDataForTarget(client, hit);
        if (aspectData != null && !aspectData.isEmpty()) {
            renderAspectData(drawContext, aspectData, x, y);
        }
    }

    private AspectData getAspectDataForTarget(MinecraftClient client, HitResult hit) {
        if (hit.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) hit;
            Entity entity = entityHit.getEntity();

            if (entity instanceof LivingEntity livingEntity) {
                // Handle living entities
                if (livingEntity instanceof IAspectAffinityEntity aspectAffinity) {
                    return aspectAffinity.aspectslib$getAspectData();
                }
            } else if (entity instanceof ItemEntity itemEntity) {
                // Handle item entities
                ItemStack stack = itemEntity.getStack();
                return ((dev.overgrown.aspectslib.api.IAspectDataProvider) (Object) stack).aspectslib$getAspectData();
            }
        }
        return null;
    }

    private void renderAspectData(DrawContext context, AspectData data, int centerX, int centerY) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        boolean showNames = MinecraftClient.getInstance().options.sneakKey.isPressed();

        // Calculate total width for centering
        int totalWidth = 0;
        for (var entry : data.getMap().object2IntEntrySet()) {
            Aspect aspect = ModRegistries.ASPECTS.get(entry.getKey());
            if (aspect == null) continue;

            int textWidth = showNames ?
                    textRenderer.getWidth(aspect.getTranslatedName()) :
                    textRenderer.getWidth(String.valueOf(entry.getIntValue()));

            totalWidth += ASPECT_ICON_SIZE + textWidth + ASPECT_SPACING;
        }

        // Start position for drawing
        int currentX = centerX - totalWidth / 2;

        for (var entry : data.getMap().object2IntEntrySet()) {
            Identifier aspectId = entry.getKey();
            Aspect aspect = ModRegistries.ASPECTS.get(aspectId);
            if (aspect == null) continue;

            Identifier texture = aspect.textureLocation();
            context.drawTexture(texture, currentX, centerY, 0, 0,
                    ASPECT_ICON_SIZE, ASPECT_ICON_SIZE,
                    ASPECT_ICON_SIZE, ASPECT_ICON_SIZE);

            if (showNames) {
                Text aspectText = aspect.getTranslatedName().formatted(Formatting.WHITE);
                context.drawText(textRenderer, aspectText,
                        currentX + ASPECT_TEXT_OFFSET,
                        centerY + TEXT_Y_OFFSET,
                        0xFFFFFF, false);
                currentX += ASPECT_ICON_SIZE + textRenderer.getWidth(aspectText) + ASPECT_SPACING;
            } else {
                String value = String.valueOf(entry.getIntValue());
                context.drawText(textRenderer, value,
                        currentX + ASPECT_TEXT_OFFSET,
                        centerY + TEXT_Y_OFFSET,
                        0xFFFFFF, false);
                currentX += ASPECT_ICON_SIZE + textRenderer.getWidth(value) + ASPECT_SPACING;
            }
        }
    }

    private void renderVesselAspects(DrawContext context, Map<String, Integer> aspects, int centerX, int centerY) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        boolean showNames = MinecraftClient.getInstance().options.sneakKey.isPressed();

        // Calculate total width for centering
        int totalWidth = 0;
        for (Map.Entry<String, Integer> entry : aspects.entrySet()) {
            // Convert aspect name to lowercase for identifier
            String aspectName = entry.getKey().toLowerCase();
            Aspect aspect = ModRegistries.ASPECTS.get(new Identifier("aspectslib", aspectName));
            if (aspect == null) continue;

            int textWidth = showNames ?
                    textRenderer.getWidth(aspect.getTranslatedName()) :
                    textRenderer.getWidth(String.valueOf(entry.getValue()));

            totalWidth += ASPECT_ICON_SIZE + textWidth + ASPECT_SPACING;
        }

        // Start position for drawing
        int currentX = centerX - totalWidth / 2;

        for (Map.Entry<String, Integer> entry : aspects.entrySet()) {
            // Convert aspect name to lowercase for identifier
            String aspectName = entry.getKey().toLowerCase();
            Identifier aspectId = new Identifier("aspectslib", aspectName);
            Aspect aspect = ModRegistries.ASPECTS.get(aspectId);
            if (aspect == null) continue;

            Identifier texture = aspect.textureLocation();
            context.drawTexture(texture, currentX, centerY, 0, 0,
                    ASPECT_ICON_SIZE, ASPECT_ICON_SIZE,
                    ASPECT_ICON_SIZE, ASPECT_ICON_SIZE);

            if (showNames) {
                Text aspectText = aspect.getTranslatedName().formatted(Formatting.WHITE);
                context.drawText(textRenderer, aspectText,
                        currentX + ASPECT_TEXT_OFFSET,
                        centerY + TEXT_Y_OFFSET,
                        0xFFFFFF, false);
                currentX += ASPECT_ICON_SIZE + textRenderer.getWidth(aspectText) + ASPECT_SPACING;
            } else {
                String value = String.valueOf(entry.getValue());
                context.drawText(textRenderer, value,
                        currentX + ASPECT_TEXT_OFFSET,
                        centerY + TEXT_Y_OFFSET,
                        0xFFFFFF, false);
                currentX += ASPECT_ICON_SIZE + textRenderer.getWidth(value) + ASPECT_SPACING;
            }
        }
    }
}