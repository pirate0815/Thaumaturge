package dev.overgrown.thaumaturge.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.overgrown.thaumaturge.compat.modmenu.config.AspectConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class ModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new Screen(Text.translatable("config.thaumaturge.title")) {
            @Override
            protected void init() {
                super.init();
                int y = this.height / 2 - 20;
                int centerX = this.width / 2;

                // Always show aspects toggle
                addDrawableChild(CyclingButtonWidget.onOffBuilder(AspectConfig.ALWAYS_SHOW_ASPECTS)
                        .build(centerX - 100, y, 200, 20,
                                Text.translatable("config.thaumaturge.always_show_aspects"),
                                (button, value) -> {
                                    AspectConfig.ALWAYS_SHOW_ASPECTS = value;
                                    AspectConfig.save();
                                }));

                y += 24;

                // Save button
                addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> {
                    AspectConfig.save();
                    MinecraftClient.getInstance().setScreen(parent);
                }).dimensions(centerX - 50, y, 100, 20).build());
            }

            @Override
            public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                renderBackground(context, mouseX, mouseY, delta);
                context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xFFFFFF);
                super.render(context, mouseX, mouseY, delta);
            }
        };
    }

    @Override
    public void attachModpackBadges(Consumer<String> consumer) {
        // Example: Mark mod as part of a modpack if needed
        // consumer.accept(Thaumaturge.MOD_ID);
    }
}