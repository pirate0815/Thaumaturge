package dev.overgrown.thaumaturge.block.focal_manipulator.screen;

import dev.overgrown.aspectslib.aspects.api.AspectsAPI;
import dev.overgrown.aspectslib.registry.ModItems;
import dev.overgrown.thaumaturge.item.focus.FocusItem;
import dev.overgrown.thaumaturge.registry.ModScreens;
import dev.overgrown.thaumaturge.spell.focal.FocalComponentRegistry;
import dev.overgrown.thaumaturge.spell.focal.SpellNode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class FocalManipulatorScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    // Position of the inventory texture within the GUI (relative to GUI top-left)
    public static final int INVENTORY_TEX_X = -65;
    public static final int INVENTORY_TEX_Y = -5;

    private static final int SLOT_SIZE = 18;

    // Client constructor
    public FocalManipulatorScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(1));
    }

    // Server constructor
    public FocalManipulatorScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ModScreens.FOCAL_MANIPULATOR, syncId);
        checkSize(inventory, 1);
        this.inventory = inventory;
        inventory.onOpen(playerInventory.player);

        // Focus slot (bottom left)
        this.addSlot(new Slot(inventory, 0, 31, 190) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() instanceof FocusItem;
            }
        });

        int xStart = -56;

        // Player main inventory (3 rows x 3 cols vertical layout matching texture)
        for (int row = 0; row < 9; ++row) {
            for (int col = 0; col < 3; ++col) {
                this.addSlot(new Slot(playerInventory,
                        col + row * 3 + 9,
                        xStart + col * SLOT_SIZE,
                        62 + row * SLOT_SIZE));
            }
        }
        // Player hotbar (3x3 block above main inventory)
        for (int col = 0; col < 3; ++col) {
            for (int row = 0; row < 3; ++row) {
                this.addSlot(new Slot(playerInventory, col * 3 + row,
                        xStart + col * SLOT_SIZE,
                        5 + row * SLOT_SIZE));
            }
        }
    }

    /** Craft handling (called from C2S packet on server) */
    public void handleCraftRequest(ServerPlayerEntity player, SpellNode tree, String spellName) {
        ItemStack focusStack = inventory.getStack(0);
        if (focusStack.isEmpty() || !(focusStack.getItem() instanceof FocusItem focus)) {
            player.sendMessage(Text.translatable("focal.thaumaturge.no_focus"), false);
            return;
        }

        if (tree == null || tree.getChildren().isEmpty()) {
            player.sendMessage(Text.translatable("focal.thaumaturge.empty_spell"), false);
            return;
        }

        // Validate complexity against focus tier limit
        String tier = focus.getTier();
        int complexity = tree.computeComplexity();
        int limit = FocalComponentRegistry.getComplexityLimit(tier);
        if (complexity > limit) {
            player.sendMessage(Text.translatable("focal.thaumaturge.too_complex",
                    complexity, limit), false);
            return;
        }

        // Check that the player has the required Aspect Shards
        List<Identifier> requiredEffects = tree.collectEffects();
        for (Identifier effectId : requiredEffects) {
            String aspectName = effectId.getPath();
            Item shardItem = ModItems.getAspectShard(aspectName);
            if (shardItem == null) continue; // No shard exists for this aspect
            if (!playerHasItem(player, shardItem)) {
                player.sendMessage(Text.translatable("focal.thaumaturge.missing_shard", aspectName), false);
                return;
            }
        }

        // Consume the required Aspect Shards
        for (Identifier effectId : requiredEffects) {
            String aspectName = effectId.getPath();
            Item shardItem = ModItems.getAspectShard(aspectName);
            if (shardItem == null) continue;
            removeOneItem(player, shardItem);
        }

        // Write spell data to the focus item's NBT
        NbtCompound focusNbt = focusStack.getOrCreateNbt();
        focusNbt.put("SpellTree", tree.toNbt());
        focusNbt.putString("SpellName", spellName.isEmpty() ? "Unnamed Spell" : spellName);
        focusNbt.putInt("SpellComplexity", complexity);

        // Populate AspectData so the tooltip system and getAspect() work
        for (Identifier effectId : requiredEffects) {
            AspectsAPI.addAspect(focusStack, effectId, 1);
        }

        // Update the stack's custom name to the spell name
        if (!spellName.isEmpty()) {
            focusStack.setCustomName(Text.literal(spellName));
        }

        inventory.markDirty();
        player.sendMessage(Text.translatable("focal.thaumaturge.crafted", spellName), false);
    }

    private static boolean playerHasItem(ServerPlayerEntity player, Item item) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == item && !stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static void removeOneItem(ServerPlayerEntity player, Item item) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == item && !stack.isEmpty()) {
                stack.decrement(1);
                if (stack.isEmpty()) {
                    player.getInventory().setStack(i, ItemStack.EMPTY);
                }
                return;
            }
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack originalStack = ItemStack.EMPTY;
        Slot sourceSlot = this.slots.get(slot);
        if (sourceSlot.hasStack()) {
            ItemStack stackInSlot = sourceSlot.getStack();
            originalStack = stackInSlot.copy();

            if (slot == 0) {
                if (!this.insertItem(stackInSlot, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (stackInSlot.getItem() instanceof FocusItem) {
                    if (!this.insertItem(stackInSlot, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (slot < 28) {
                        if (!this.insertItem(stackInSlot, 28, 37, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (slot < 37) {
                        if (!this.insertItem(stackInSlot, 1, 28, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }

            if (stackInSlot.isEmpty()) {
                sourceSlot.setStack(ItemStack.EMPTY);
            } else {
                sourceSlot.markDirty();
            }
            if (stackInSlot.getCount() == originalStack.getCount()) {
                return ItemStack.EMPTY;
            }
            sourceSlot.onTakeItem(player, stackInSlot);
        }
        return originalStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    public Inventory getInventory() {
        return inventory;
    }
}