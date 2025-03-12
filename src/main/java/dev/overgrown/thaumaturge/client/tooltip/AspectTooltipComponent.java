package dev.overgrown.thaumaturge.client.tooltip;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.util.Identifier;
import java.util.Map;

public class AspectTooltipComponent implements TooltipComponent {
    private final Map<Identifier, Integer> aspects;

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
        for (var entry : aspects.entrySet()) {
            width += 16 + 2 + textRenderer.getWidth(entry.getValue().toString());
        }
        return width;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, int width, int height, DrawContext context) {
        int currentX = x;
        for (var entry : aspects.entrySet()) {
            Identifier aspectId = entry.getKey();
            int value = entry.getValue();

            // Draw aspect icon
            Identifier texture = new Identifier(aspectId.getNamespace(), "textures/aspects_icons/" + aspectId.getPath() + ".png");
            context.drawTexture(texture, currentX, y, 0, 0, 0, 16, 16, 16, 16);

            // Draw value text
            String valueStr = String.valueOf(value);
            context.drawText(textRenderer, valueStr, currentX + 18, y + 4, 0xFFFFFF, true);
            currentX += 16 + textRenderer.getWidth(valueStr) + 4;
        }
    }
}