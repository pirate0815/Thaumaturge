package dev.overgrown.thaumaturge.block.focal_manipulator.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.overgrown.thaumaturge.item.focus.FocusItem;
import dev.overgrown.thaumaturge.networking.FocalManipulatorPackets;
import dev.overgrown.thaumaturge.spell.focal.FocalComponentRegistry;
import dev.overgrown.thaumaturge.spell.focal.SpellComponentDefinition;
import dev.overgrown.thaumaturge.spell.focal.SpellNode;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class FocalManipulatorScreen extends HandledScreen<FocalManipulatorScreenHandler> {

    private static final Identifier OUTER_TEXTURE =
            new Identifier("thaumaturge", "textures/gui/focal_manipulator/focal_manipulator_outer_layer.png");
    private static final Identifier INNER_TEXTURE =
            new Identifier("thaumaturge", "textures/gui/focal_manipulator/focal_manipulator_inner_layer.png");
    private static final Identifier INVENTORY_TEXTURE =
            new Identifier("thaumaturge", "textures/gui/focal_manipulator/focal_manipulator_player_inventory.png");

    private TextFieldWidget spellNameField;
    private ButtonWidget craftButton;
    private FocalListWidget focalListWidget;
    private SpellTreeWidget spellTree;
    private ParameterPanelWidget parameterPanel;

    private SpellNode rootNode;

    public FocalManipulatorScreen(FocalManipulatorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 256;
        this.backgroundHeight = 256;
    }

    @Override
    protected void init() {
        super.init();

        // Spell name field (top)
        this.spellNameField = new TextFieldWidget(this.textRenderer, this.x + 80, this.y + 6, 100, 12, Text.empty());
        this.spellNameField.setMaxLength(50);
        this.spellNameField.setEditable(true);
        this.spellNameField.setPlaceholder(Text.literal("Spell Name..."));
        this.addSelectableChild(this.spellNameField);

        // Craft button (top right)
        this.craftButton = ButtonWidget.builder(Text.literal("Craft"), button -> onCraftPressed())
                .dimensions(this.x + 227, this.y + 6, 40, 20)
                .build();
        this.addDrawableChild(this.craftButton);

        // Spell tree (center area)
        this.spellTree = new SpellTreeWidget(this,
                this.x + 84, this.y + 24, 120, 150);

        rootNode = new SpellNode(FocalComponentRegistry.ROOT_ID);
        spellTree.setRoot(rootNode);
        this.addSelectableChild(this.spellTree);

        // Component list (left scrollable panel, icon-only, original style)
        int yOff = 30;
        this.focalListWidget = new FocalListWidget(this, client, 32, backgroundHeight,
                y + yOff, y + backgroundHeight - 68, 36);
        this.addSelectableChild(this.focalListWidget);

        // Parameter panel (bottom right)
        this.parameterPanel = new ParameterPanelWidget(this,
                this.x + 210, this.y + 176, 120, 80);
        this.addSelectableChild(this.parameterPanel);

        // Default: show what root can accept
        onTreeSelectionChanged(rootNode);

        // Load existing spell from focus if present
        loadSpellFromFocus();
    }

    private void loadSpellFromFocus() {
        ItemStack focusStack = handler.getInventory().getStack(0);
        if (!focusStack.isEmpty() && focusStack.getItem() instanceof FocusItem) {
            var nbt = focusStack.getNbt();
            if (nbt != null && nbt.contains("SpellTree")) {
                SpellNode loaded = SpellNode.fromNbt(nbt.getCompound("SpellTree"));
                if (loaded != null) {
                    rootNode = loaded;
                    spellTree.setRoot(rootNode);
                }
                if (nbt.contains("SpellName")) {
                    spellNameField.setText(nbt.getString("SpellName"));
                }
            }
        }
    }

    // ACTIONS:
    /**
     * Called when a component icon is clicked in the list.
     * Adds it to the currently selected tree node (or root).
     */
    public void onComponentClicked(SpellComponentDefinition def) {
        SpellNode selectedNode = spellTree.getSelected();
        if (selectedNode == null) selectedNode = rootNode;

        if (!selectedNode.canAcceptChildDef(def)) return;

        SpellNode newChild = new SpellNode(def.id());
        selectedNode.addChild(newChild);

        // Check complexity limit
        ItemStack focusStack = handler.getInventory().getStack(0);
        if (focusStack.getItem() instanceof FocusItem focus) {
            int limit = FocalComponentRegistry.getComplexityLimit(focus.getTier());
            if (rootNode.computeComplexity() > limit) {
                selectedNode.removeChild(newChild);
                return;
            }
        }

        spellTree.rebuildFlat();
        spellTree.setSelected(newChild);
        onTreeSelectionChanged(newChild);
        onTreeChanged();
    }

    private void onCraftPressed() {
        if (rootNode == null || rootNode.getChildren().isEmpty()) return;

        ItemStack focusStack = handler.getInventory().getStack(0);
        if (focusStack.isEmpty() || !(focusStack.getItem() instanceof FocusItem)) return;

        String name = spellNameField.getText().trim();
        if (name.isEmpty()) name = "Unnamed Spell";

        ClientPlayNetworking.send(FocalManipulatorPackets.CRAFT_SPELL,
                FocalManipulatorPackets.writeCraftPacket(rootNode, name));
    }

    /** Callbacks from widgets */
    public void onTreeSelectionChanged(SpellNode selected) {
        if (selected != null) {
            SpellComponentDefinition def = selected.getDefinition();
            if (def != null) {
                focalListWidget.rebuild(def.allowedChildren(), def.provides());
            }
            parameterPanel.setSelectedNode(selected);
        }
    }

    public void onTreeChanged() {
        // Complexity is recalculated in render
    }

    /** Rendering */
    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        context.drawTexture(OUTER_TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        context.drawTexture(INVENTORY_TEXTURE,
                this.x + FocalManipulatorScreenHandler.INVENTORY_TEX_X,
                this.y + FocalManipulatorScreenHandler.INVENTORY_TEX_Y,
                0, 0, 256, 256);
        context.drawTexture(INNER_TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // Don't draw default title/labels
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);

        // Component list
        this.focalListWidget.render(context, mouseX, mouseY, delta);

        // Spell tree
        this.spellTree.render(context, mouseX, mouseY, delta);

        // Spell name field
        this.spellNameField.render(context, mouseX, mouseY, delta);

        // Parameter panel
        this.parameterPanel.render(context, mouseX, mouseY, delta);

        // Stats panel (right side)
        int statsX = this.x + 229;
        int statsY = this.y + 30;

        int complexity = rootNode != null ? rootNode.computeComplexity() : 0;
        int limit = 0;
        String tierName = "—";

        ItemStack focusStack = handler.getInventory().getStack(0);
        if (focusStack.getItem() instanceof FocusItem focus) {
            String tier = focus.getTier();
            limit = FocalComponentRegistry.getComplexityLimit(tier);
            tierName = tier.substring(0, 1).toUpperCase() + tier.substring(1);
        }

        // Complexity color
        int complexColor;
        if (limit == 0) {
            complexColor = 0xAAAAAA;
        } else if (complexity > limit) {
            complexColor = 0xFF5555;
        } else if (complexity > limit * 0.75) {
            complexColor = 0xFFAA00;
        } else {
            complexColor = 0x55FF55;
        }

        int lineY = statsY;

        context.drawText(this.textRenderer, "Focus: " + tierName,
                statsX, lineY, 0xFFFFFF, false);
        lineY += 12;

        context.drawText(this.textRenderer, "Complexity: " + complexity + "/" + (limit > 0 ? limit : "—"),
                statsX, lineY, complexColor, false);
        lineY += 12;

        if (rootNode != null && !rootNode.getChildren().isEmpty()) {
            // Aether cost to craft
            int craftCost = Math.max(10, complexity * 10);
            context.drawText(this.textRenderer, "Craft: " + craftCost + " Aether",
                    statsX, lineY, 0x9966FF, false);
            lineY += 12;

            // Aether cost per cast (estimate)
            int castCost = computeCastAetherCost();
            context.drawText(this.textRenderer, "Cast: ~" + castCost + " Aether",
                    statsX, lineY, 0x9966FF, false);
            lineY += 12;

            // Cooldown estimate
            int cooldownTicks = 10 + complexity * 2;
            float cooldownSecs = cooldownTicks / 20.0f;
            context.drawText(this.textRenderer, String.format("Cooldown: %.1fs", cooldownSecs),
                    statsX, lineY, 0xFF9966, false);
            lineY += 12;

            // Experience level required
            int xpLevel = computeRequiredXpLevel(complexity);
            context.drawText(this.textRenderer, "XP Level: " + xpLevel,
                    statsX, lineY, 0x55FFFF, false);
            lineY += 12;

            // Aspect Shards required (unique effect aspects)
            List<Identifier> effects = rootNode.collectEffects();
            context.drawText(this.textRenderer, "Shards: " + effects.size(),
                    statsX, lineY, 0xFFAA00, false);
            lineY += 12;

            for (Identifier effectId : effects) {
                SpellComponentDefinition eDef = FocalComponentRegistry.get(effectId);
                String name = eDef != null ? eDef.name() : effectId.getPath();
                context.drawText(this.textRenderer, " " + name,
                        statsX, lineY, 0xAAAAAA, false);
                lineY += 10;
            }
        }

        // Update craft button state
        boolean canCraft = focusStack.getItem() instanceof FocusItem
                && rootNode != null
                && !rootNode.getChildren().isEmpty()
                && complexity <= limit;
        craftButton.active = canCraft;

        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    /** Cost helpers */
    private int computeRequiredXpLevel(int complexity) {
        if (complexity <= 5) return 1;
        if (complexity <= 10) return 5;
        if (complexity <= 20) return 10;
        if (complexity <= 35) return 20;
        return 30;
    }

    private int computeCastAetherCost() {
        if (rootNode == null) return 0;
        List<Identifier> effects = rootNode.collectEffects();
        int total = 0;
        for (Identifier id : effects) {
            SpellComponentDefinition def = FocalComponentRegistry.get(id);
            if (def != null) total += def.baseComplexity() * 5;
        }
        return Math.max(1, total);
    }

    /** Input */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Let the text field handle focus/unfocus on click
        if (this.spellNameField.mouseClicked(mouseX, mouseY, button)) return true;
        // Clicking elsewhere unfocuses the text field
        this.spellNameField.setFocused(false);

        if (this.parameterPanel.mouseClicked(mouseX, mouseY, button)) return true;
        if (this.spellTree.mouseClicked(mouseX, mouseY, button)) return true;
        if (this.focalListWidget.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (this.spellTree.mouseScrolled(mouseX, mouseY, amount)) return true;
        if (this.focalListWidget.mouseScrolled(mouseX, mouseY, amount)) return true;
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.spellNameField.keyPressed(keyCode, scanCode, modifiers)) return true;
        // When the text field is focused, consume all key presses except Escape
        // to prevent inventory keybinds (like "I") from closing the GUI
        if (this.spellNameField.isFocused() && keyCode != 256 /* GLFW_KEY_ESCAPE */) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.spellNameField.charTyped(chr, modifiers)) return true;
        return super.charTyped(chr, modifiers);
    }

    public int guiLeft() {
        return x;
    }
}
