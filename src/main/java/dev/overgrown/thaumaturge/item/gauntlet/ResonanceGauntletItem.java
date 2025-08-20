package dev.overgrown.thaumaturge.item.gauntlet;

import dev.overgrown.thaumaturge.item.focus.FocusItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ResonanceGauntletItem extends Item {
    private final int slots;

    public ResonanceGauntletItem(Settings settings, int slots) {
        super(settings);
        this.slots = slots;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (user.isSneaking()) {
            NbtList foci = getFoci(stack);
            if (!foci.isEmpty()) {
                // Drop all foci
                for (NbtElement element : foci) {
                    ItemStack focus = ItemStack.fromNbt((NbtCompound) element);
                    user.giveItemStack(focus);
                }

                // Clear foci from gauntlet
                setFoci(stack, new NbtList());
                return TypedActionResult.success(stack);
            }
        }
        return TypedActionResult.pass(stack);
    }

    public NbtList getFoci(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        return nbt.getList("Foci", NbtCompound.COMPOUND_TYPE);
    }

    public void setFoci(ItemStack stack, NbtList foci) {
        stack.getOrCreateNbt().put("Foci", foci);
    }

    public int getSlots() {
        return slots;
    }

    public boolean addFocus(ItemStack gauntletStack, ItemStack focusStack) {
        if (!(focusStack.getItem() instanceof FocusItem)) return false;

        NbtList foci = getFoci(gauntletStack);
        if (foci.size() >= slots) return false; // Check slot limit

        // Create a copy of the focus with count 1
        ItemStack focusCopy = focusStack.copy();
        focusCopy.setCount(1);

        // Serialize and add to NBT list
        NbtCompound focusNbt = new NbtCompound();
        focusCopy.writeNbt(focusNbt);
        foci.add(focusNbt);

        // Update gauntlet NBT
        setFoci(gauntletStack, foci);
        return true;
    }
}