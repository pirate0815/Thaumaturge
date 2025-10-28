package dev.overgrown.thaumaturge.item.aetheric_goggles.overlay;

import dev.overgrown.aspectslib.aether.AetherAPI;
import dev.overgrown.aspectslib.aether.AetherManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class AethericGogglesOverlay {

    public void onHudRender(DrawContext drawContext, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Check if player is wearing aetheric goggles
        if (!isWearingAethericGoggles(client.player)) return;

        // Get current chunk Aether data
        ChunkPos chunkPos = new ChunkPos(client.player.getBlockPos());
        var aetherData = AetherManager.getAetherData(client.player.getWorld(), chunkPos);

        // Render Aether levels
        renderAetherLevels(drawContext, aetherData, client);

        // Render corruption status if applicable
        renderCorruptionStatus(drawContext, client);
    }

    private boolean isWearingAethericGoggles(net.minecraft.entity.player.PlayerEntity player) {
        // Check if player has aetheric goggles equipped
        return player.getInventory().armor.get(3).getItem() instanceof dev.overgrown.thaumaturge.item.aetheric_goggles.AethericGogglesItem;
    }

    private void renderAetherLevels(DrawContext drawContext, dev.overgrown.aspectslib.aether.AetherChunkData aetherData, MinecraftClient client) {
        int x = 10;
        int y = 10;

        for (Identifier aspectId : aetherData.getAspectIds()) {
            double percentage = aetherData.getAetherPercentage(aspectId);
            int current = aetherData.getCurrentAether(aspectId);
            int max = aetherData.getMaxAether(aspectId);

            // Render aspect bar
            renderAspectBar(drawContext, x, y, aspectId, percentage, current, max);
            y += 20;
        }
    }

    private void renderCorruptionStatus(DrawContext drawContext, MinecraftClient client) {
        BlockPos pos = client.player.getBlockPos();
        ChunkPos chunkPos = new ChunkPos(pos);

        if (AetherAPI.isDeadZone(client.player.getWorld(), chunkPos)) {
            // Render dead zone warning
            drawContext.drawText(client.textRenderer, "DEAD ZONE", 10, 100, 0xFF0000, true);
        }
    }

    private void renderAspectBar(DrawContext drawContext, int x, int y, Identifier aspectId, double percentage, int current, int max) {
        // Implementation for rendering aspect bars
        String text = String.format("%s: %d/%d", aspectId.getPath(), current, max);
        drawContext.drawText(MinecraftClient.getInstance().textRenderer, text, x, y, 0xFFFFFF, true);

        // Draw progress bar
        int barWidth = 100;
        int filledWidth = (int) (barWidth * percentage);
        drawContext.fill(x, y + 10, x + barWidth, y + 15, 0xFF555555); // Background
        drawContext.fill(x, y + 10, x + filledWidth, y + 15, 0xFF00FF00); // Fill
    }
}