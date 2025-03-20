package dev.overgrown.thaumaturge.block.vessel;

import dev.overgrown.thaumaturge.block.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

public class VesselBlockEntity extends BlockEntity implements Inventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(8, ItemStack.EMPTY);

    public VesselBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VESSEL, pos, state);
    }

    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        Inventories.readNbt(nbt, inventory, registries);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        Inventories.writeNbt(nbt, inventory, registries);
        super.writeNbt(nbt, registries);
    }

    public boolean addStack(ItemStack stack) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack slotStack = inventory.get(i);
            if (slotStack.isEmpty()) {
                inventory.set(i, stack.copyWithCount(1));
                markDirty();
                return true;
            } else if (ItemStack.areItemsAndComponentsEqual(slotStack, stack) && slotStack.getCount() < slotStack.getMaxCount()) {
                slotStack.increment(1);
                markDirty();
                return true;
            }
        }
        return false;
    }

    // Implement Inventory methods
    @Override
    public int size() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        return inventory.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack stack = Inventories.splitStack(inventory, slot, amount);
        markDirty();
        return stack;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack stack = Inventories.removeStack(inventory, slot);
        markDirty();
        return stack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        stack.capCount(getMaxCount(stack));
        markDirty();
    }

    @Override
    public void clear() {
        inventory.clear();
        markDirty();
    }

    @Override
    public boolean canPlayerUse(net.minecraft.entity.player.PlayerEntity player) {
        return true;
    }
}