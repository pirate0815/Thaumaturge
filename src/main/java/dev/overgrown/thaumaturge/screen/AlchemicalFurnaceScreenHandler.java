package dev.overgrown.thaumaturge.screen;

import dev.overgrown.aspectslib.api.AspectsAPI;
import dev.overgrown.thaumaturge.block.alchemical_furnace.AlchemicalFurnaceBlockEntity;
import dev.overgrown.thaumaturge.registry.ModScreens;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class AlchemicalFurnaceScreenHandler extends ScreenHandler {

    private static class FuelSlot extends Slot {
        public FuelSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }
        @Override
        public boolean canInsert(ItemStack stack) {
            Integer fuel = FuelRegistry.INSTANCE.get(stack.getItem());
            return fuel != null && super.canInsert(stack);
        }
    }

    private static class OutputSlot extends Slot {

        public OutputSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }
    }

    private static class InputSLot extends Slot {

        public InputSLot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            boolean hasAspects = !AspectsAPI.getItemAspectData(stack.getItem()).isEmpty();
            return hasAspects && super.canInsert(stack);
        }
    }

    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;


    // Server Constructor
    public AlchemicalFurnaceScreenHandler (int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate delegate) {
        super(ModScreens.ALCHEMICAL_FURNACE_SCREEN_HANDLER, syncId);
        checkSize(inventory, 3);
        this.inventory = inventory;
        this.propertyDelegate = delegate;

        inventory.onOpen(playerInventory.player);
        this.addProperties(propertyDelegate);

        // XY Cords are those for the container gui image

        // Add Furnace Slots
        this.addSlot(new InputSLot(this.inventory, AlchemicalFurnaceBlockEntity.INPUT_SLOT, 62, 17));
        this.addSlot(new FuelSlot(this.inventory, AlchemicalFurnaceBlockEntity.FUEL_SLOT, 62, 53));
        this.addSlot(new OutputSlot(this.inventory, AlchemicalFurnaceBlockEntity.OUTPUT_SLOT, 98, 53));

        // Inventory
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // Hotbar
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    // Client Constructor
    public AlchemicalFurnaceScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(3), new ArrayPropertyDelegate(5));
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    public int getFuelBurnTime() {
        return propertyDelegate.get(0);
    }

    public int getItemBurnTime() {
        return propertyDelegate.get(1);
    }

    public int getItemMaxBurnTime() {
        return propertyDelegate.get(2);
    }

    public int getTotalAspectLevel() {
        return propertyDelegate.get(3);
    }

    public int getFuelMaxBurnTime() {
        return propertyDelegate.get(4);
    }
}
