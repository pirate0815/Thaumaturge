package dev.overgrown.thaumaturge.block.focal_manipulator.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.overgrown.thaumaturge.spell.focal.FocalComponentRegistry;
import dev.overgrown.thaumaturge.spell.focal.Socket;
import dev.overgrown.thaumaturge.spell.focal.SpellComponentDefinition;
import dev.overgrown.thaumaturge.spell.focal.SpellComponentType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Scrollable list widget showing available spell components as 32×32 icons only.
 * Clicking an icon adds the component to the spell tree.
 */
public class FocalListWidget extends AlwaysSelectedEntryListWidget<FocalListWidget.FocalEntry> {

    private final FocalManipulatorScreen screen;

    public FocalListWidget(FocalManipulatorScreen screen, MinecraftClient client,
                           int width, int height, int top, int bottom, int itemHeight) {
        super(client, width, height, top, bottom, itemHeight);
        this.screen = screen;
        setLeftPos(screen.guiLeft() + 20);
        setRenderBackground(false);
        setRenderHorizontalShadows(false);
    }

    /**
     * Rebuilds the list to show only components of the given types whose
     * required sockets are satisfied by the parent's provided sockets.
     */
    public void rebuild(@Nullable Set<SpellComponentType> allowedTypes, @Nullable Set<Socket> parentProvides) {
        clearEntries();
        if (allowedTypes == null || allowedTypes.isEmpty()) return;

        Set<Socket> available = parentProvides != null ? parentProvides : Set.of();

        for (SpellComponentType type : SpellComponentType.values()) {
            if (!allowedTypes.contains(type)) continue;
            List<SpellComponentDefinition> defs = FocalComponentRegistry.byType(type);
            for (SpellComponentDefinition def : defs) {
                if (def.id().equals(FocalComponentRegistry.ROOT_ID)) continue;
                if (!available.containsAll(def.requires())) continue;
                addEntry(new FocalEntry(def));
            }
        }
    }

    @Override
    protected int getScrollbarPositionX() {
        return 10000;
    }

    @Override
    public int getRowWidth() {
        return 32;
    }

    @Override
    public void setSelected(@Nullable FocalEntry entry) {
        super.setSelected(entry);
    }

    @Nullable
    public SpellComponentDefinition getSelectedDefinition() {
        FocalEntry entry = getSelectedOrNull();
        return entry != null ? entry.definition : null;
    }

    /** Entry (icon only) */
    public class FocalEntry extends Entry<FocalEntry> {

        final SpellComponentDefinition definition;

        public FocalEntry(SpellComponentDefinition definition) {
            this.definition = definition;
        }

        @Override
        public Text getNarration() {
            return Text.literal(definition.name());
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            FocalListWidget.this.setSelected(this);
            if (button == 0) {
                screen.onComponentClicked(definition);
            }
            return true;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth,
                           int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            Identifier icon = definition.icon();
            if (icon != null) {
                RenderSystem.enableBlend();
                context.drawTexture(icon, x, y, 0, 0, entryWidth, entryHeight, 32, 32);
                RenderSystem.disableBlend();
            }
        }
    }
}
