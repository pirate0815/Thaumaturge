package dev.overgrown.thaumaturge.client.tooltip;

import dev.overgrown.thaumaturge.compat.modmenu.config.AspectConfig;
import dev.overgrown.thaumaturge.data.Aspect;
import dev.overgrown.thaumaturge.item.ModItems;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class AspectTooltipComponent implements TooltipComponent {
    private final Object2IntMap<RegistryEntry<Aspect>> aspects;

    public AspectTooltipComponent(AspectTooltipData data) {
        this.aspects = data.aspects();
    }

    private boolean hasRequiredItems() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return false;
        PlayerEntity player = client.player;

        // Check Aspect Lens in inventory
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack item = player.getInventory().getStack(i);
            if (item.getItem() == ModItems.ASPECT_LENS) {
                return true;
            }
        }

        // Check head slot for Goggles
        ItemStack headStack = client.player.getEquippedStack(EquipmentSlot.HEAD);
        return headStack.getItem() == ModItems.AETHERIC_GOGGLES;
    }

    private boolean shouldShowTranslation() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (AspectConfig.ALWAYS_SHOW_ASPECTS) return true;
        return hasRequiredItems() && client.currentScreen != null && Screen.hasShiftDown();
    }

    @Override
    public int getHeight(TextRenderer textRenderer) {
        return 18;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        boolean showTranslation = shouldShowTranslation();
        int width = 0;
        for (var entry : aspects.object2IntEntrySet()) {
            int valueWidth = showTranslation ?
                    textRenderer.getWidth(entry.getKey().value().getTranslatedName()) :
                    textRenderer.getWidth(String.valueOf(entry.getIntValue()));
            width += 16 + 2 + valueWidth + 4; // Added +4 for padding
        }
        return width;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, int width, int height, DrawContext context) {
        boolean showTranslation = shouldShowTranslation();
        int currentX = x;
        final int TEXT_COLOR = 0xFFFFFFFF; // White with full opacity

        for (var entry : aspects.object2IntEntrySet()) {
            int value = entry.getIntValue();
            Identifier texture = entry.getKey().value().textureLocation();

            // Draw aspect icon
            context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, currentX, y, 0, 0, 16, 16, 16, 16);

            int textY = y + 5; // Center text vertically

            if (showTranslation) {
                // Only show translation name (hide the amount)
                Text aspectName = entry.getKey().value().getTranslatedName().formatted(Formatting.WHITE);
                context.drawText(textRenderer, aspectName, currentX + 18, textY, TEXT_COLOR, false);

                // Only move by name width + padding
                currentX += 16 + textRenderer.getWidth(aspectName) + 6;
            } else {
                // Only show amount (number)
                String valueStr = String.valueOf(value);
                context.drawText(textRenderer, valueStr, currentX + 18, textY, TEXT_COLOR, false);

                // Only move by amount width + padding
                currentX += 16 + textRenderer.getWidth(valueStr) + 6;
            }
        }
    }
}