package dev.overgrown.thaumaturge.client.tooltip;

import dev.overgrown.thaumaturge.data.Aspect;
import dev.overgrown.thaumaturge.item.ModItems;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.RenderLayer;
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

        // Check head slot for Goggles or Monocle
        ItemStack headStack = client.player.getEquippedStack(EquipmentSlot.HEAD);
        return headStack.getItem() == ModItems.AETHERIC_GOGGLES || headStack.getItem() == ModItems.RESONANCE_MONOCLE;
    }

    @Override
    public int getHeight(TextRenderer textRenderer) {
        return 18;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        boolean showTranslation = Screen.hasShiftDown() && hasRequiredItems();
        int width = 0;
        for (var entry : aspects.object2IntEntrySet()) {
            int valueWidth = showTranslation ?
                    textRenderer.getWidth(entry.getKey().value().getTranslatedName()) :
                    textRenderer.getWidth(String.valueOf(entry.getIntValue()));
            width += 16 + 2 + valueWidth;
        }
        return width + (aspects.size() - 1) * 4;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, int width, int height, DrawContext context) {
        boolean showTranslation = Screen.hasShiftDown() && hasRequiredItems();
        int currentX = x;

        for (var entry : aspects.object2IntEntrySet()) {
            if (MinecraftClient.getInstance().world != null) {
                int value = entry.getIntValue();
                Identifier texture = entry.getKey().value().getTextureLocation();

                context.drawTexture(RenderLayer::getGuiTextured, texture, currentX, y, 0, 0, 16, 16, 16, 16);

                if (showTranslation) {
                    Text aspectName = entry.getKey().value().getTranslatedName().formatted(Formatting.WHITE);
                    context.drawText(textRenderer, aspectName, currentX + 16 + 2, y + 4, 0xFFFFFF, true);
                } else {
                    String valueStr = String.valueOf(value);
                    context.drawText(textRenderer, valueStr, currentX + 16 + 2, y + 4, 0xFFFFFF, true);
                }

                int entryTextWidth = showTranslation ?
                        textRenderer.getWidth(entry.getKey().value().getTranslatedName()) :
                        textRenderer.getWidth(String.valueOf(value));
                currentX += 16 + 2 + entryTextWidth + 4;
            }
        }
    }
}