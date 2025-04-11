package dev.overgrown.thaumaturge.item.ResonanceMonocle;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.client.tooltip.AspectTooltipComponent;
import dev.overgrown.thaumaturge.client.tooltip.AspectTooltipData;
import dev.overgrown.thaumaturge.component.AspectComponent;
import dev.overgrown.thaumaturge.data.Aspect;
import dev.overgrown.thaumaturge.item.ModItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.joml.Vector2f;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

@Environment(EnvType.CLIENT)
public class ResonanceMonocleRenderer {
    private static Vector2f tooltipPosition;
    private static AspectComponent tooltipComponent;

    public static void init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) {
                tooltipComponent = null;
                tooltipPosition = null;
                return;
            }

            ItemStack headStack = client.player.getEquippedStack(EquipmentSlot.HEAD);
            if (headStack.getItem() != ModItems.RESONANCE_MONOCLE) {
                tooltipComponent = null;
                tooltipPosition = null;
                return;
            }

            HitResult hit = client.crosshairTarget;
            if (hit == null || hit.getType() != HitResult.Type.ENTITY) {
                tooltipComponent = null;
                tooltipPosition = null;
                return;
            }

            Entity entity = ((EntityHitResult) hit).getEntity();
            if (entity instanceof LivingEntity livingEntity) {
                Object2IntOpenHashMap<RegistryEntry<Aspect>> combinedAspects = new Object2IntOpenHashMap<>();
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    ItemStack stack = livingEntity.getEquippedStack(slot);
                    AspectComponent component = stack.getOrDefault(AspectComponent.TYPE, AspectComponent.DEFAULT);
                    component.getMap().forEach((aspect, amount) -> combinedAspects.mergeInt(aspect, amount, Integer::sum));
                }
                if (combinedAspects.isEmpty()) {
                    tooltipComponent = null;
                    tooltipPosition = null;
                } else {
                    tooltipComponent = new AspectComponent(combinedAspects);
                    tooltipPosition = calculateScreenPosition(); // Modified line
                }
            } else {
                tooltipComponent = null;
                tooltipPosition = null;
            }
        });

        HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer.attachLayerAfter(Identifier.of("minecraft", "misc_overlays"),
                Identifier.of(Thaumaturge.MOD_ID, "resonance_monocle_hud"),
                (drawContext, tickCounter) -> {
                    if (tooltipPosition == null || tooltipComponent == null) return;

                    AspectTooltipData data = new AspectTooltipData(tooltipComponent.getMap());
                    AspectTooltipComponent tooltip = new AspectTooltipComponent(data);

                    TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
                    int width = tooltip.getWidth(textRenderer);
                    int height = tooltip.getHeight(textRenderer);

                    int x = (int) (tooltipPosition.x - (width / 2f));
                    int y = (int) (tooltipPosition.y - height - 4);

                    tooltip.drawItems(textRenderer, x, y, width, height, drawContext);
                }));
    }

    // Method to center on cross-hair
    private static Vector2f calculateScreenPosition() {
        MinecraftClient client = MinecraftClient.getInstance();
        int scaledWidth = client.getWindow().getScaledWidth();
        int scaledHeight = client.getWindow().getScaledHeight();
        return new Vector2f(scaledWidth / 2.0f, scaledHeight / 2.0f);
    }
}