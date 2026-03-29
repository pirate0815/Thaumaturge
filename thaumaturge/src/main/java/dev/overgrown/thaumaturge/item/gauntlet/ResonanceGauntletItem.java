package dev.overgrown.thaumaturge.item.gauntlet;

import dev.overgrown.thaumaturge.block.api.AspectContainer;
import dev.overgrown.thaumaturge.block.faucet.FaucetBlockEntity;
import dev.overgrown.thaumaturge.item.focus.FocusItem;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class ResonanceGauntletItem extends Item {
    private final int slots;

    public static final String NBT_TAG_FAUCET = "faucet_cords";

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

            // Handle Faucet target manipulation
            NbtCompound compound = stack.getOrCreateNbt();

            // Player has already clicked on a faucet
            if (compound.contains(NBT_TAG_FAUCET)) {
                BlockPos faucetPos = BlockPos.fromLong(compound.getLong(NBT_TAG_FAUCET));
                if (!world.isClient) {compound.remove(NBT_TAG_FAUCET);}
                if (world.isChunkLoaded(ChunkSectionPos.getSectionCoord(faucetPos.getX()), ChunkSectionPos.getSectionCoord(faucetPos.getZ()))) {
                    BlockEntity entity = world.getBlockEntity(faucetPos);
                    if (entity instanceof FaucetBlockEntity faucetBlockEntity) {
                        BlockHitResult result = raycast(world, user, RaycastContext.FluidHandling.ANY);
                        if (result.getType() == HitResult.Type.BLOCK) {
                            BlockPos targetPos = result.getBlockPos();

                            // Handle Clicking on a Block with a TileEntity implementing AspectContainer
                            if (world.getBlockEntity(targetPos) instanceof AspectContainer) {
                                if (targetPos.isWithinDistance(faucetPos, 16)) {
                                    if (!world.isClient) {
                                        faucetBlockEntity.setTarget(targetPos);
                                    } else {
                                        user.sendMessage(Text.translatable("block.thaumaturge.faucet.success"), true);
                                    }
                                    return TypedActionResult.success(stack);
                                }
                            }
                            // Handle Player clicking on the faucet to unlink its target
                            else if (targetPos.equals(faucetPos)) {
                                if (!world.isClient) {
                                    faucetBlockEntity.setTarget(null);
                                } else {
                                    user.sendMessage(Text.translatable("block.thaumaturge.faucet.unlink"), true);
                                }
                                return TypedActionResult.success(stack);

                            }
                        }
                    }

                }
                if (world.isClient) {user.sendMessage(Text.translatable("block.thaumaturge.faucet.failure"), true);}
                return TypedActionResult.fail(stack);


            }
            // Handle Selecting a new Focus
            else {
                BlockHitResult result = raycast(world, user, RaycastContext.FluidHandling.ANY);
                if (result.getType() == HitResult.Type.BLOCK) {
                    if(world.getBlockEntity(result.getBlockPos()) instanceof FaucetBlockEntity faucetBlockEntity) {
                        if (!world.isClient) {
                            compound.putLong(NBT_TAG_FAUCET, result.getBlockPos().asLong());
                        } else {
                            user.sendMessage(Text.translatable("block.thaumaturge.faucet.selection"), true);
                        }
                        return TypedActionResult.success(stack);
                    }
                }
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