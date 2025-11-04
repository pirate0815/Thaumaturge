package dev.overgrown.thaumaturge.item.apophenia;

import dev.overgrown.thaumaturge.integration.modonomicon.ModonomiconIntegration;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ApopheniaItem extends Item {
    private static final String OPEN_KEY = "Open";
    private static final Identifier THAUMATURGE_BOOK_ID = new Identifier("thaumaturge", "apophenia");

    // Track if we're waiting for GUI to close
    private static boolean waitingForGuiClose = false;
    private static ItemStack lastOpenedStack = null;

    public ApopheniaItem() {
        super(new FabricItemSettings().maxCount(1));

        // Register client tick event to check for GUI close
        if (ModonomiconIntegration.isModonomiconLoaded()) {
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                onClientTick(client);
            });
        }
    }

    private void onClientTick(MinecraftClient client) {
        if (waitingForGuiClose && lastOpenedStack != null) {
            // Check if the Modonomicon GUI is no longer open
            if (client.currentScreen == null) {
                // GUI was closed, update the item state
                setBookOpenState(lastOpenedStack, false);
                waitingForGuiClose = false;
                lastOpenedStack = null;
            }
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (world.isClient()) {
            // Try to open Modonomicon book first
            if (ModonomiconIntegration.isModonomiconLoaded()) {
                if (ModonomiconIntegration.openBook(THAUMATURGE_BOOK_ID)) {
                    ModonomiconIntegration.setBookOpenState(stack, true);
                    user.getItemCooldownManager().set(this, 5);

                    // Start tracking for GUI close
                    waitingForGuiClose = true;
                    lastOpenedStack = stack;

                    return TypedActionResult.success(stack);
                }
            }

            // Fallback to original behavior if Modonomicon is not installed or failed to open
            toggleState(stack);
            user.getItemCooldownManager().set(this, 5);
        }

        return TypedActionResult.success(stack, world.isClient());
    }

    private void toggleState(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        boolean isOpen = nbt.getBoolean(OPEN_KEY);
        nbt.putBoolean(OPEN_KEY, !isOpen);
    }

    private void setBookOpenState(ItemStack stack, boolean open) {
        if (ModonomiconIntegration.isModonomiconLoaded()) {
            ModonomiconIntegration.setBookOpenState(stack, open);
        } else {
            NbtCompound nbt = stack.getOrCreateNbt();
            nbt.putBoolean(OPEN_KEY, open);
        }
    }

    public static boolean isOpen(ItemStack stack) {
        // Check Modonomicon open state first
        if (ModonomiconIntegration.isModonomiconLoaded() && ModonomiconIntegration.isBookOpen(stack)) {
            return true;
        }

        // Fallback to original NBT check
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        return nbt != null && nbt.getBoolean(OPEN_KEY);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.TOOT_HORN;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        if (ModonomiconIntegration.isModonomiconLoaded()) {
            tooltip.add(Text.translatable("item.thaumaturge.apophenia.tooltip.modonomicon"));
        } else {
            if (isOpen(stack)) {
                tooltip.add(Text.translatable("item.thaumaturge.apophenia.tooltip.open"));
            } else {
                tooltip.add(Text.translatable("item.thaumaturge.apophenia.tooltip.closed"));
            }
        }
    }

    // Method to handle when the book is closed (called from screen close event if needed)
    public static void onBookClosed(ItemStack stack) {
        if (ModonomiconIntegration.isModonomiconLoaded()) {
            ModonomiconIntegration.setBookOpenState(stack, false);
        } else {
            // Reset to closed state for original behavior
            NbtCompound nbt = stack.getOrCreateNbt();
            nbt.putBoolean(OPEN_KEY, false);
        }
    }
}