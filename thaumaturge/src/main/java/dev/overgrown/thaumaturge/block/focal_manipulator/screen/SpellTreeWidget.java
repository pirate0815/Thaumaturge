package dev.overgrown.thaumaturge.block.focal_manipulator.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.overgrown.thaumaturge.spell.focal.Socket;
import dev.overgrown.thaumaturge.spell.focal.SpellComponentDefinition;
import dev.overgrown.thaumaturge.spell.focal.SpellNode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders the spell tree as a top-down branching layout. Root is centered at the top; children branch downward.
 * Left-click selects a node, right-click removes it and all descendants.
 */
public class SpellTreeWidget implements Drawable, Element, Selectable {

    private static final int ICON_SIZE = 16;
    private static final int H_GAP = 4;
    private static final int V_GAP = 20; // extra room for socket indicators
    private static final int LEVEL_HEIGHT = ICON_SIZE + V_GAP;

    private final FocalManipulatorScreen screen;
    private final int x, y, width, height;
    private SpellNode root;
    private SpellNode selected;
    private int scrollY = 0;

    private final List<LayoutNode> layoutNodes = new ArrayList<>();
    private int treeContentHeight = 0;

    public SpellTreeWidget(FocalManipulatorScreen screen, int x, int y, int width, int height) {
        this.screen = screen;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setRoot(SpellNode root) {
        this.root = root;
        this.selected = null;
        rebuildLayout();
    }

    public SpellNode getRoot() {
        return root;
    }

    public SpellNode getSelected() {
        return selected;
    }

    public void setSelected(SpellNode node) {
        this.selected = node;
    }

    public void rebuildFlat() {
        rebuildLayout();
    }

    private void rebuildLayout() {
        layoutNodes.clear();
        treeContentHeight = 0;
        if (root == null) return;

        int subtreeW = computeSubtreeWidth(root);
        int startX = width / 2 - subtreeW / 2;
        assignPositions(root, startX, 4, subtreeW);

        for (LayoutNode ln : layoutNodes) {
            treeContentHeight = Math.max(treeContentHeight, ln.y + ICON_SIZE + 4);
        }
    }

    private int computeSubtreeWidth(SpellNode node) {
        List<SpellNode> children = node.getChildren();
        if (children.isEmpty()) return ICON_SIZE;

        int total = 0;
        for (int i = 0; i < children.size(); i++) {
            if (i > 0) total += H_GAP;
            total += computeSubtreeWidth(children.get(i));
        }
        return Math.max(ICON_SIZE, total);
    }

    private void assignPositions(SpellNode node, int leftX, int topY, int availWidth) {
        int iconX = leftX + availWidth / 2 - ICON_SIZE / 2;
        layoutNodes.add(new LayoutNode(node, iconX, topY));

        List<SpellNode> children = node.getChildren();
        if (children.isEmpty()) return;

        int childY = topY + LEVEL_HEIGHT;
        int[] childWidths = new int[children.size()];
        int totalChildWidth = 0;
        for (int i = 0; i < children.size(); i++) {
            childWidths[i] = computeSubtreeWidth(children.get(i));
            totalChildWidth += childWidths[i];
        }
        totalChildWidth += H_GAP * (children.size() - 1);

        int childStartX = leftX + availWidth / 2 - totalChildWidth / 2;
        int cx = childStartX;
        for (int i = 0; i < children.size(); i++) {
            assignPositions(children.get(i), cx, childY, childWidths[i]);
            cx += childWidths[i] + H_GAP;
        }
    }

    /** Rendering */
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Background
        context.fill(x, y, x + width, y + height, 0x40000000);
        context.enableScissor(x, y, x + width, y + height);

        // Clamp scroll
        int maxScroll = Math.max(0, treeContentHeight - height);
        scrollY = Math.min(scrollY, maxScroll);
        scrollY = Math.max(0, scrollY);

        // Draw connection lines (behind icons)
        for (LayoutNode ln : layoutNodes) {
            int parentCX = x + ln.x + ICON_SIZE / 2;
            int parentBY = y + ln.y + ICON_SIZE - scrollY;

            List<SpellNode> children = ln.node.getChildren();
            if (children.isEmpty()) continue;

            // Vertical line down from parent to mid-point
            int midY = parentBY + V_GAP / 2;
            context.fill(parentCX, parentBY, parentCX + 1, midY, 0x80FFFFFF);

            // Horizontal line spanning all children
            int leftmost = Integer.MAX_VALUE;
            int rightmost = Integer.MIN_VALUE;
            for (SpellNode child : children) {
                LayoutNode cl = findLayout(child);
                if (cl != null) {
                    int childCX = x + cl.x + ICON_SIZE / 2;
                    leftmost = Math.min(leftmost, childCX);
                    rightmost = Math.max(rightmost, childCX);
                }
            }
            if (leftmost < Integer.MAX_VALUE) {
                context.fill(leftmost, midY, rightmost + 1, midY + 1, 0x80FFFFFF);
            }

            // Vertical lines down from horizontal bar to each child
            for (SpellNode child : children) {
                LayoutNode cl = findLayout(child);
                if (cl != null) {
                    int childCX = x + cl.x + ICON_SIZE / 2;
                    int childTY = y + cl.y - scrollY;
                    context.fill(childCX, midY, childCX + 1, childTY, 0x80FFFFFF);
                }
            }
        }

        // Draw icons
        LayoutNode hoveredNode = null;
        for (LayoutNode ln : layoutNodes) {
            int drawX = x + ln.x;
            int drawY = y + ln.y - scrollY;

            if (drawY + ICON_SIZE < y || drawY > y + height) continue;

            SpellComponentDefinition def = ln.node.getDefinition();
            if (def == null) continue;

            // Selection highlight
            if (ln.node == selected) {
                context.fill(drawX - 2, drawY - 2, drawX + ICON_SIZE + 2, drawY + ICON_SIZE + 2, 0x60FFFFFF);
            }

            // Hover detection
            boolean hovered = mouseX >= drawX && mouseX < drawX + ICON_SIZE
                    && mouseY >= drawY && mouseY < drawY + ICON_SIZE;
            if (hovered) {
                hoveredNode = ln;
                if (ln.node != selected) {
                    context.fill(drawX - 1, drawY - 1, drawX + ICON_SIZE + 1, drawY + ICON_SIZE + 1, 0x40FFFF00);
                }
            }

            // Draw the aspect icon (including root)
            Identifier icon = def.icon();
            if (icon != null) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                context.drawTexture(icon, drawX, drawY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            }

            // Type border indicator (subtle 1px colored border on bottom)
            int typeColor = switch (def.type()) {
                case MEDIUM -> 0xFF6699FF;
                case EFFECT -> 0xFFFF6666;
                case MODIFIER -> 0xFF66FF66;
            };
            context.fill(drawX, drawY + ICON_SIZE, drawX + ICON_SIZE, drawY + ICON_SIZE + 1, typeColor);

            // Socket indicators (small icons below the node)
            int indicatorY = drawY + ICON_SIZE + 2;
            int indicatorX = drawX;
            if (def.provides().contains(Socket.TARGET)) {
                // Target indicator: small crosshair (4x4)
                int cx = indicatorX + 2;
                int cy = indicatorY + 2;
                context.fill(cx, cy - 2, cx + 1, cy + 3, 0xFFFF6666); // vertical
                context.fill(cx - 2, cy, cx + 3, cy + 1, 0xFFFF6666); // horizontal
                indicatorX += 8;
            }
            if (def.provides().contains(Socket.TRAJECTORY)) {
                // Trajectory indicator: small arrow (4x4)
                context.fill(indicatorX, indicatorY + 2, indicatorX + 5, indicatorY + 3, 0xFF66CCFF); // shaft
                context.fill(indicatorX + 3, indicatorY + 1, indicatorX + 4, indicatorY + 4, 0xFF66CCFF); // head
                context.fill(indicatorX + 4, indicatorY + 2, indicatorX + 5, indicatorY + 3, 0xFF66CCFF); // tip
            }
        }

        context.disableScissor();

        // Tooltip for hovered node (drawn outside scissor so it's not clipped)
        if (hoveredNode != null) {
            SpellComponentDefinition def = hoveredNode.node.getDefinition();
            if (def != null) {
                List<Text> tooltip = new ArrayList<>();
                tooltip.add(Text.literal(def.name()));
                String typeStr = switch (def.type()) {
                    case MEDIUM -> "§9Medium";
                    case EFFECT -> "§cEffect";
                    case MODIFIER -> "§aModifier";
                };
                tooltip.add(Text.literal(typeStr));
                tooltip.add(Text.literal("§7Cost: " + def.baseComplexity()));
                if (!def.provides().isEmpty()) {
                    StringBuilder sb = new StringBuilder("§7Provides: ");
                    for (Socket s : def.provides()) sb.append(s.name().toLowerCase()).append(" ");
                    tooltip.add(Text.literal(sb.toString().trim()));
                }
                if (!def.requires().isEmpty()) {
                    StringBuilder sb = new StringBuilder("§7Requires: ");
                    for (Socket s : def.requires()) sb.append(s.name().toLowerCase()).append(" ");
                    tooltip.add(Text.literal(sb.toString().trim()));
                }
                context.drawTooltip(MinecraftClient.getInstance().textRenderer, tooltip, mouseX, mouseY);
            }
        }
    }

    private LayoutNode findLayout(SpellNode node) {
        for (LayoutNode ln : layoutNodes) {
            if (ln.node == node) return ln;
        }
        return null;
    }

    /** Input */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX < x || mouseX >= x + width || mouseY < y || mouseY >= y + height) {
            return false;
        }

        for (LayoutNode ln : layoutNodes) {
            int drawX = x + ln.x;
            int drawY = y + ln.y - scrollY;
            if (mouseX >= drawX && mouseX < drawX + ICON_SIZE
                    && mouseY >= drawY && mouseY < drawY + ICON_SIZE) {

                if (button == 0) {
                    selected = ln.node;
                    screen.onTreeSelectionChanged(ln.node);
                    return true;
                } else if (button == 1 && ln.node != root) {
                    SpellNode parent = root.findParent(ln.node);
                    if (parent != null) {
                        parent.removeChild(ln.node);
                        if (selected == ln.node) {
                            selected = parent;
                            screen.onTreeSelectionChanged(parent);
                        }
                        rebuildLayout();
                        screen.onTreeChanged();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height) {
            scrollY -= (int) (amount * 12);
            scrollY = Math.max(0, scrollY);
            return true;
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

    /** Layout data */
    private record LayoutNode(SpellNode node, int x, int y) {}
}
