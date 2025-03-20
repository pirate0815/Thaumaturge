package dev.overgrown.thaumaturge.client.tooltip;

import dev.overgrown.thaumaturge.data.Aspect;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.screen.Screen;

public class AspectTooltipComponent implements TooltipComponent {
    private final Object2IntMap<RegistryEntry<Aspect>> aspects;
    public AspectTooltipComponent(AspectTooltipData data) {
        this.aspects = data.aspects();
    }

    @Override
    public int getHeight(TextRenderer textRenderer) {
        return 18; // Icon height + padding
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        int width = 0;
        // Decide which measurement to use based on shift state
        boolean showTranslation = Screen.hasShiftDown();
        for (var entry : aspects.object2IntEntrySet()) { // Changed to object2IntEntrySet()
            int valueWidth;
            if (showTranslation) {
                Text aspectName = entry.getKey().value().getTranslatedName();
                valueWidth = textRenderer.getWidth(aspectName);
            } else {
                valueWidth = textRenderer.getWidth(String.valueOf(entry.getIntValue())); // Changed to getIntValue()
            }
            width += 16 + 2 + valueWidth; // 16 (icon) + 2 (padding) + text width
        }
        // Add padding between entries (4px per entry)
        return width + (aspects.size() - 1) * 4;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, int width, int height, DrawContext context) {
        boolean showTranslation = Screen.hasShiftDown();
        int currentX = x;

        for (var entry : aspects.object2IntEntrySet()) { // Changed to object2IntEntrySet()
            if (MinecraftClient.getInstance().world != null) {
                int value = entry.getIntValue(); // Changed to getIntValue()

                Identifier texture = entry.getKey().value().getTextureLocation();

                // Use the correct RenderLayer method for GUI textures
                context.drawTexture(RenderLayer::getGuiTextured, texture, currentX, y, 0, 0, 16, 16, 16, 16);

                if (showTranslation) {
                    Text aspectName = entry.getKey().value().getTranslatedName()
                            .formatted(Formatting.GRAY);
                    context.drawText(textRenderer, aspectName, currentX + 16 + 2, y + 4, 0xFFFFFF, true);
                } else {
                    String valueStr = String.valueOf(value);
                    context.drawText(textRenderer, valueStr, currentX + 16 + 2, y + 4, 0xFFFFFF, true);
                }

                int entryTextWidth = showTranslation ?
                        textRenderer.getWidth(entry.getKey().value().getTranslatedName()) :
                        textRenderer.getWidth(String.valueOf(value));
                int entryWidth = 16 + 2 + entryTextWidth;
                currentX += entryWidth + 4;
            }
        }
    }

}