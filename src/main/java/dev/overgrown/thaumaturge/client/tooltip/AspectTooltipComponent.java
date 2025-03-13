package dev.overgrown.thaumaturge.client.tooltip;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.loot.LootTables;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import java.util.Map;

public class AspectTooltipComponent implements TooltipComponent {
    private final Map<Identifier, Integer> aspects;
    private int minimumWidth;
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
            width += 16 + 4 + textRenderer.getWidth(entry.getValue().toString());
            MutableText aspectNameDisplay = getAspectName(entry.getKey());
            width += textRenderer.getWidth(aspectNameDisplay);
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
            Identifier texture = Identifier.of(aspectId.getNamespace(), "textures/aspects_icons/" + aspectId.getPath() + ".png");
            context.drawTexture(RenderLayer::getGuiTextured, texture, currentX, y, 0, 0, 16, 16, 16, 16);

            // Draw value text
            String valueStr = String.valueOf(value);
            currentX += 18;
            context.drawText(textRenderer, valueStr, currentX, y + 4, 0xFFFFFF, true);
            currentX += textRenderer.getWidth(valueStr) + 2;

            MutableText aspectNameDisplay = getAspectName(aspectId);
            context.drawText(textRenderer, aspectNameDisplay, currentX, y + 4, 0xFFFFFF, true);
            currentX += textRenderer.getWidth(aspectNameDisplay) + 2;
            if(currentX > minimumWidth) minimumWidth = currentX;
        }
    }

    private MutableText getAspectName(Identifier identifier) {
        return Text.literal("(")
                .append(Text.translatable("aspect." + identifier.getNamespace() + "." + identifier.getPath() + ".name"))
                .append(Text.literal(")"))
                .formatted(Formatting.GRAY);
    }
}