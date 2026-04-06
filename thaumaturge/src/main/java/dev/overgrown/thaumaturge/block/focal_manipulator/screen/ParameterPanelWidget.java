package dev.overgrown.thaumaturge.block.focal_manipulator.screen;

import dev.overgrown.thaumaturge.spell.focal.ParameterDef;
import dev.overgrown.thaumaturge.spell.focal.SpellComponentDefinition;
import dev.overgrown.thaumaturge.spell.focal.SpellNode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Panel widget displayed in the bottom-right of the Focal Manipulator screen.
 * Shows tunable parameters for the currently selected spell node, with +/- buttons
 * to adjust each value. Changes are applied directly to the {@link SpellNode}.
 */
public class ParameterPanelWidget implements Drawable, Element, Selectable {

    private static final int ROW_HEIGHT = 14;
    private static final int BUTTON_SIZE = 10;
    private static final float STEP = 0.5f;

    private final FocalManipulatorScreen screen;
    private final int x, y, width, height;
    private SpellNode selectedNode;
    private final List<ParamRow> rows = new ArrayList<>();

    public ParameterPanelWidget(FocalManipulatorScreen screen, int x, int y, int width, int height) {
        this.screen = screen;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setSelectedNode(SpellNode node) {
        this.selectedNode = node;
        rows.clear();
        if (node == null) return;
        SpellComponentDefinition def = node.getDefinition();
        if (def == null || def.parameters().isEmpty()) return;

        int rowY = 0;
        for (ParameterDef p : def.parameters()) {
            rows.add(new ParamRow(p, rowY));
            rowY += ROW_HEIGHT;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (rows.isEmpty()) return;

        TextRenderer tr = MinecraftClient.getInstance().textRenderer;

        // Panel background
        context.fill(x, y, x + width, y + Math.min(height, rows.size() * ROW_HEIGHT + 14), 0x80000000);

        // Title
        SpellComponentDefinition def = selectedNode != null ? selectedNode.getDefinition() : null;
        String title = def != null ? def.name() + " Settings" : "Settings";
        context.drawText(tr, title, x + 3, y + 2, 0xFFFFFF, false);

        int contentY = y + 14;

        for (ParamRow row : rows) {
            int rowTop = contentY + row.yOffset;
            if (rowTop + ROW_HEIGHT > y + height) break;

            float value = selectedNode.getParameter(row.def.key());

            // Parameter name
            context.drawText(tr, row.def.displayName() + ":", x + 3, rowTop + 2, 0xCCCCCC, false);

            // Value
            String valueStr = String.format("%.1f", value);
            int valueX = x + width - 50;
            context.drawText(tr, valueStr, valueX, rowTop + 2, 0x55FF55, false);

            // [-] button
            int minusBtnX = x + width - 24;
            boolean minusHovered = mouseX >= minusBtnX && mouseX < minusBtnX + BUTTON_SIZE
                    && mouseY >= rowTop && mouseY < rowTop + BUTTON_SIZE;
            context.fill(minusBtnX, rowTop, minusBtnX + BUTTON_SIZE, rowTop + BUTTON_SIZE,
                    minusHovered ? 0xFFFF6666 : 0xFF993333);
            context.drawText(tr, "-", minusBtnX + 2, rowTop + 1, 0xFFFFFF, false);

            // [+] button
            int plusBtnX = x + width - 12;
            boolean plusHovered = mouseX >= plusBtnX && mouseX < plusBtnX + BUTTON_SIZE
                    && mouseY >= rowTop && mouseY < rowTop + BUTTON_SIZE;
            context.fill(plusBtnX, rowTop, plusBtnX + BUTTON_SIZE, rowTop + BUTTON_SIZE,
                    plusHovered ? 0xFF66FF66 : 0xFF339933);
            context.drawText(tr, "+", plusBtnX + 2, rowTop + 1, 0xFFFFFF, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (selectedNode == null || rows.isEmpty() || button != 0) return false;

        int contentY = y + 14;
        for (ParamRow row : rows) {
            int rowTop = contentY + row.yOffset;

            // [-] button
            int minusBtnX = x + width - 24;
            if (mouseX >= minusBtnX && mouseX < minusBtnX + BUTTON_SIZE
                    && mouseY >= rowTop && mouseY < rowTop + BUTTON_SIZE) {
                float current = selectedNode.getParameter(row.def.key());
                selectedNode.setParameter(row.def.key(), current - STEP);
                screen.onTreeChanged();
                return true;
            }

            // [+] button
            int plusBtnX = x + width - 12;
            if (mouseX >= plusBtnX && mouseX < plusBtnX + BUTTON_SIZE
                    && mouseY >= rowTop && mouseY < rowTop + BUTTON_SIZE) {
                float current = selectedNode.getParameter(row.def.key());
                selectedNode.setParameter(row.def.key(), current + STEP);
                screen.onTreeChanged();
                return true;
            }
        }
        return false;
    }

    @Override
    public void setFocused(boolean focused) {}

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {}

    private record ParamRow(ParameterDef def, int yOffset) {}
}
