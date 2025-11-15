package dev.overgrown.thaumaturge.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.overgrown.thaumaturge.block.alchemical_furnace.AlchemicalFurnaceBlockEntity;
import dev.overgrown.thaumaturge.screen.AlchemicalFurnaceScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class AlchemicalFurnaceScreen extends HandledScreen<AlchemicalFurnaceScreenHandler> {


    private static final Identifier TEXTURE = Identifier.tryParse("thaumaturge:textures/gui/alchemical_furnace.png");


    public AlchemicalFurnaceScreen(AlchemicalFurnaceScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

        int totalAspects = handler.getTotalAspectLevel();
        if (totalAspects > 0) {
            // Draw Aspect Level
            int tankX = 98;
            int tankY = 20;
            int tankHeight = 22;
            int tankWidth = 16;
            int fullTankX = 176;
            int fullTankY = 0;
            int tankFillHeight = Math.min((tankHeight * totalAspects) /AlchemicalFurnaceBlockEntity.MAX_ASPECT_COUNT, tankHeight);
            context.drawTexture(TEXTURE, x+tankX, y+ tankY +tankHeight-tankFillHeight, fullTankX, fullTankY, tankWidth, tankFillHeight);
        }
        int itemBurnTime = handler.getItemBurnTime();
        if (itemBurnTime > 0) {
            int itemMaxBurnTime = handler.getItemMaxBurnTime();
            if (itemMaxBurnTime <= 0) {itemMaxBurnTime = 1;}

            // Draw Progress Arrow
            int arrowX = 81;
            int arrowY = 24;
            int arrowHeight = 8;
            int arrowWidth = 14;
            int fullArrowX = 176;
            int fullArrowY = 22;
            int arrowFillWidth = Math.min((arrowWidth*itemBurnTime)/itemMaxBurnTime,arrowWidth);
            context.drawTexture(TEXTURE,x+arrowX,y+arrowY,fullArrowX, fullArrowY, arrowFillWidth, arrowHeight);
        }
        int fuelBurnTime = handler.getFuelBurnTime();
        if (fuelBurnTime > 0) {
            int fuelMaxBurnTime = handler.getFuelMaxBurnTime();
            int fireX = 63;
            int fireY = 37;
            int fireHeight = 14;
            int fireWidth = 14;
            int fullFireX = 176;
            int fullFireY = 30;
            int fireFillHeight = Math.min(((fuelBurnTime*fireHeight)/fuelMaxBurnTime),fireHeight);
            int fireNotFillHeight = fireHeight - fireFillHeight;
            context.drawTexture(TEXTURE,x+fireX,y+fireY+fireNotFillHeight,fullFireX,fullFireY+fireNotFillHeight,fireWidth,fireFillHeight);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }
}
