package dev.overgrown.thaumaturge.client;

import dev.overgrown.aspectslib.api.IAspectAffinityEntity;
import dev.overgrown.aspectslib.data.Aspect;
import dev.overgrown.aspectslib.data.AspectData;
import dev.overgrown.aspectslib.data.ModRegistries;
import dev.overgrown.thaumaturge.item.AethericGogglesItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

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

        AspectData aspectData = getAspectDataForTarget(client, hit);
        if (aspectData == null || aspectData.isEmpty()) return;

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int x = screenWidth / 2;
        int y = screenHeight / 2 + CROSSHAIR_OFFSET_Y;

        renderAspectData(drawContext, aspectData, x, y);
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
        int startX = centerX - totalWidth / 2;
        int currentX = startX;

        for (var entry : data.getMap().object2IntEntrySet()) {
            Identifier aspectId = entry.getKey();
            Aspect aspect = ModRegistries.ASPECTS.get(aspectId);
            if (aspect == null) continue;

            // Draw aspect icon
            Identifier texture = aspect.textureLocation();
            context.drawTexture(texture, currentX, centerY, 0, 0,
                    ASPECT_ICON_SIZE, ASPECT_ICON_SIZE,
                    ASPECT_ICON_SIZE, ASPECT_ICON_SIZE);

            // Draw aspect name or value
            if (showNames) {
                Text aspectName = aspect.getTranslatedName().formatted(Formatting.WHITE);
                context.drawText(textRenderer, aspectName,
                        currentX + ASPECT_TEXT_OFFSET,
                        centerY + TEXT_Y_OFFSET,
                        0xFFFFFF, false);
                currentX += ASPECT_ICON_SIZE + textRenderer.getWidth(aspectName) + ASPECT_SPACING;
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
}