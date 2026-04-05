package dev.overgrown.thaumaturge.item.gauntlet;

import dev.overgrown.thaumaturge.block.api.AspectContainer;
import dev.overgrown.thaumaturge.block.faucet.FaucetBlockEntity;
import dev.overgrown.thaumaturge.item.focus.FocusItem;
import dev.overgrown.thaumaturge.spell.input.ComboPattern;
import dev.overgrown.thaumaturge.spell.input.GauntletInteractionBlocklist;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Base class for all gauntlet items in the Thaumaturge mod.
 *
 * <h3>Right-click (RIGHT input) behavior</h3>
 * <ul>
 *   <li>If the player <strong>is not sneaking</strong>: register a RIGHT combo input.
 *       If that completes a {@link ComboPattern} the corresponding focus slot fires.</li>
 *   <li>If the player <strong>is sneaking</strong>: enter Faucet-linking mode
 *       (unchanged from the original system).</li>
 * </ul>
 *
 * <p>The RIGHT input is suppressed when the player is looking at a block registered
 * in {@link GauntletInteractionBlocklist} (e.g. Vessel, Jar, Faucet).
 *
 * <h3>Slot count</h3>
 * <p>Subclasses supply {@link #getFocusSlots()} (1, 2, or 3).  The combo system
 * will only recognize patterns for slots that exist.
 */
public abstract class ResonanceGauntletItem extends Item {

    private static final String KEY_FOCI   = "Foci";
    private static final String KEY_FAUCET = "faucet_cords";

    protected ResonanceGauntletItem(Settings settings) {
        super(settings);
    }

    /** Number of focus slots this gauntlet tier exposes (1, 2, or 3). */
    public abstract int getFocusSlots();

    // Item.use -> RIGHT input
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient) return TypedActionResult.pass(stack);

        ServerPlayerEntity player = (ServerPlayerEntity) user;

        // Sneak-right: faucet management / focus drop
        if (player.isSneaking()) {
            return handleSneak(stack, player, world);
        }

        // Check block interaction suppression
        HitResult hit = user.raycast(4.5, 1.0f, false);
        if (hit instanceof BlockHitResult blockHit
                && GauntletInteractionBlocklist.isSuppressed(world, blockHit.getBlockPos())) {
            // The block will handle this interaction; don't count as spell input.
            return TypedActionResult.pass(stack);
        }

        // Combo inputs are handled client-side by GauntletComboMixin.
        // The client detects the 3-input pattern and sends a C2S packet.
        return TypedActionResult.success(stack);
    }

    // Focus NBT management
    public NbtList getFoci(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        if (!nbt.contains(KEY_FOCI, NbtElement.LIST_TYPE)) {
            nbt.put(KEY_FOCI, new NbtList());
        }
        return nbt.getList(KEY_FOCI, NbtCompound.COMPOUND_TYPE);
    }

    public void setFoci(ItemStack stack, NbtList foci) {
        stack.getOrCreateNbt().put(KEY_FOCI, foci);
    }

    /**
     * Inserts a focus into the next available slot.
     *
     * @return {@code true} if there was a free slot and the focus was added
     */
    public boolean addFocus(ItemStack gauntletStack, ItemStack focusStack) {
        if (!(focusStack.getItem() instanceof FocusItem)) return false;

        NbtList foci = getFoci(gauntletStack);
        if (foci.size() >= getFocusSlots()) return false;

        ItemStack copy = focusStack.copy();
        copy.setCount(1);
        NbtCompound focusNbt = new NbtCompound();
        copy.writeNbt(focusNbt);
        foci.add(focusNbt);
        setFoci(gauntletStack, foci);
        return true;
    }

    /**
     * Removes and returns the focus in the given slot, or empty if none.
     */
    public Optional<ItemStack> removeFocus(ItemStack gauntletStack, int slot) {
        NbtList foci = getFoci(gauntletStack);
        if (slot >= foci.size()) return Optional.empty();
        NbtCompound removed = foci.getCompound(slot);
        foci.remove(slot);
        setFoci(gauntletStack, foci);
        return Optional.of(ItemStack.fromNbt(removed));
    }

    /** Tooltip */
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world,
                              List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        tooltip.add(Text.translatable("gauntlet.thaumaturge.slots",
                getFocusSlots()).formatted(Formatting.GRAY));

        NbtList foci = getFoci(stack);
        for (int i = 0; i < getFocusSlots(); i++) {
            ComboPattern pattern = ComboPattern.ALL.get(i);
            if (i < foci.size()) {
                ItemStack focus = ItemStack.fromNbt(foci.getCompound(i));
                String name = focus.hasCustomName()
                        ? focus.getName().getString()
                        : focus.getItem().getName(focus).getString();
                tooltip.add(Text.literal("  " + pattern.getName() + ": ")
                        .formatted(Formatting.DARK_AQUA)
                        .append(Text.literal(name).formatted(Formatting.WHITE)));
            } else {
                tooltip.add(Text.literal("  " + pattern.getName() + ": ")
                        .formatted(Formatting.DARK_AQUA)
                        .append(Text.translatable("gauntlet.thaumaturge.empty_slot")
                                .formatted(Formatting.DARK_GRAY)));
            }
        }
    }

    // Private helpers:
    /**
     * Sneak + right-click: drop all foci back to player, or enter faucet-link mode.
     */
    private TypedActionResult<ItemStack> handleSneak(ItemStack stack,
                                                     ServerPlayerEntity player,
                                                     World world) {
        NbtList foci = getFoci(stack);
        if (!foci.isEmpty()) {
            // Return all foci to the player
            for (int i = 0; i < foci.size(); i++) {
                ItemStack focus = ItemStack.fromNbt(foci.getCompound(i));
                if (!player.giveItemStack(focus)) {
                    player.dropItem(focus, false);
                }
            }
            setFoci(stack, new NbtList());
            return TypedActionResult.success(stack);
        }

        // Faucet-link management
        NbtCompound nbt = stack.getOrCreateNbt();
        if (nbt.contains(KEY_FAUCET)) {
            return handleFaucetTarget(stack, player, world, nbt);
        } else {
            return handleFaucetSelection(stack, player, world, nbt);
        }
    }

    private TypedActionResult<ItemStack> handleFaucetSelection(ItemStack stack,
                                                               ServerPlayerEntity player,
                                                               World world,
                                                               NbtCompound nbt) {
        BlockHitResult result = raycast(world, player, RaycastContext.FluidHandling.ANY);
        if (result.getType() == HitResult.Type.BLOCK
                && world.getBlockEntity(result.getBlockPos()) instanceof FaucetBlockEntity) {
            nbt.putLong(KEY_FAUCET, result.getBlockPos().asLong());
            if (world.isClient) {
                player.sendMessage(Text.translatable("block.thaumaturge.faucet.selection"), true);
            }
            return TypedActionResult.success(stack);
        }
        return TypedActionResult.pass(stack);
    }

    private TypedActionResult<ItemStack> handleFaucetTarget(ItemStack stack,
                                                            ServerPlayerEntity player,
                                                            World world,
                                                            NbtCompound nbt) {
        BlockPos faucetPos = BlockPos.fromLong(nbt.getLong(KEY_FAUCET));
        if (!world.isClient) nbt.remove(KEY_FAUCET);

        boolean loaded = world.isChunkLoaded(
                ChunkSectionPos.getSectionCoord(faucetPos.getX()),
                ChunkSectionPos.getSectionCoord(faucetPos.getZ()));

        if (loaded && world.getBlockEntity(faucetPos) instanceof FaucetBlockEntity faucet) {
            BlockHitResult result = raycast(world, player, RaycastContext.FluidHandling.ANY);
            if (result.getType() == HitResult.Type.BLOCK) {
                BlockPos targetPos = result.getBlockPos();
                BlockEntity te = world.getBlockEntity(targetPos);
                if (te instanceof AspectContainer && targetPos.isWithinDistance(faucetPos, 16)) {
                    if (!world.isClient) faucet.setTarget(targetPos);
                    else player.sendMessage(Text.translatable("block.thaumaturge.faucet.success"), true);
                    return TypedActionResult.success(stack);
                } else if (targetPos.equals(faucetPos)) {
                    if (!world.isClient) faucet.setTarget(null);
                    else player.sendMessage(Text.translatable("block.thaumaturge.faucet.unlink"), true);
                    return TypedActionResult.success(stack);
                }
            }
        }

        if (world.isClient) {
            player.sendMessage(Text.translatable("block.thaumaturge.faucet.failure"), true);
        }
        return TypedActionResult.fail(stack);
    }
}