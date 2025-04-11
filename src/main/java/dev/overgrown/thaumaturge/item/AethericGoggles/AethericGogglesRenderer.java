package dev.overgrown.thaumaturge.item.AethericGoggles;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.client.tooltip.AspectTooltipComponent;
import dev.overgrown.thaumaturge.client.tooltip.AspectTooltipData;
import dev.overgrown.thaumaturge.component.AspectComponent;
import dev.overgrown.thaumaturge.data.Aspect;
import dev.overgrown.thaumaturge.item.ModItems;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class AethericGogglesRenderer {
    private static AspectComponent currentAspects;
    private static Vector2f tooltipPosition;

    public static void init() {
        WorldRenderEvents.END.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();

            // Check for required client components
            if (client.player == null || client.world == null) {
                currentAspects = null;
                tooltipPosition = null;
                return;
            }

            // Retrieve and check matrixStack from context
            MatrixStack matrixStack = context.matrixStack();
            if (matrixStack == null) {
                currentAspects = null;
                tooltipPosition = null;
                return;
            }

            // Check if Aetheric Goggles are equipped
            ItemStack headStack = client.player.getEquippedStack(EquipmentSlot.HEAD);
            if (headStack.getItem() != ModItems.AETHERIC_GOGGLES) {
                currentAspects = null;
                tooltipPosition = null;
                return;
            }

            // Check for valid hit result
            HitResult hit = client.crosshairTarget;
            if (hit == null) {
                currentAspects = null;
                tooltipPosition = null;
                return;
            }

            Matrix4f projectionMatrix = context.projectionMatrix();

            // Handle entity hit
            if (hit.getType() == HitResult.Type.ENTITY) {
                Entity entity = ((EntityHitResult) hit).getEntity();
                if (entity instanceof ItemEntity itemEntity) {
                    ItemStack stack = itemEntity.getStack();
                    currentAspects = stack.getOrDefault(AspectComponent.TYPE, AspectComponent.DEFAULT);
                    tooltipPosition = calculateScreenPosition(); // Updated call
                } else {
                    currentAspects = null;
                    tooltipPosition = null;
                }
            }
            // Handle block hit
            else if (hit.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = ((BlockHitResult) hit).getBlockPos();
                BlockEntity blockEntity = client.world.getBlockEntity(pos);

                if (blockEntity instanceof Inventory inventory) {
                    Object2IntOpenHashMap<RegistryEntry<Aspect>> combinedAspects = new Object2IntOpenHashMap<>();
                    boolean hasItems = false;

                    for (int i = 0; i < inventory.size(); i++) {
                        ItemStack itemStack = inventory.getStack(i);
                        if (!itemStack.isEmpty()) {
                            hasItems = true;
                            AspectComponent component = itemStack.getOrDefault(AspectComponent.TYPE, AspectComponent.DEFAULT);
                            component.getMap().forEach((aspect, count) ->
                                    combinedAspects.mergeInt(aspect, count, Integer::sum)
                            );
                        }
                    }

                    if (hasItems) {
                        currentAspects = new AspectComponent(combinedAspects);
                    } else {
                        // Use block's aspects
                        ItemStack blockStack = client.world.getBlockState(pos).getBlock().asItem().getDefaultStack();
                        currentAspects = blockStack.getOrDefault(AspectComponent.TYPE, AspectComponent.DEFAULT);
                    }
                } else {
                    // Not an inventory, use block's aspects
                    ItemStack blockStack = client.world.getBlockState(pos).getBlock().asItem().getDefaultStack();
                    currentAspects = blockStack.getOrDefault(AspectComponent.TYPE, AspectComponent.DEFAULT);
                }

                tooltipPosition = calculateScreenPosition();
            } else {
                currentAspects = null;
                tooltipPosition = null;
            }
        });

        HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer.attachLayerAfter(
                Identifier.of("minecraft", "misc_overlays"),
                Identifier.of(Thaumaturge.MOD_ID, "aetheric_goggles_hud"),
                (context, tickCounter) -> {
                    if (currentAspects == null || currentAspects.isEmpty() || tooltipPosition == null) return;

                    AspectTooltipData data = new AspectTooltipData(currentAspects.getMap());
                    AspectTooltipComponent tooltip = new AspectTooltipComponent(data);

                    TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
                    int width = tooltip.getWidth(textRenderer);
                    int height = tooltip.getHeight(textRenderer);

                    // Calculate position with floating-point division
                    int x = (int) (tooltipPosition.x - (width / 2.0f));
                    int y = (int) (tooltipPosition.y - height - 4);

                    tooltip.drawItems(textRenderer, x, y, width, height, context);
                }
        ));
    }

    // Method to center on cross-hair
    private static Vector2f calculateScreenPosition() {
        MinecraftClient client = MinecraftClient.getInstance();
        int scaledWidth = client.getWindow().getScaledWidth();
        int scaledHeight = client.getWindow().getScaledHeight();
        return new Vector2f(scaledWidth / 2.0f, scaledHeight / 2.0f);
    }
}