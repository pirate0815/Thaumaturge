package dev.overgrown.thaumaturge.spell.input;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Tracks blocks whose {@link Block#onUse} would consume the right-click, so that
 * the gauntlet does <em>not</em> register the interaction as a spell-combo input. Must register blocks during mod initialization like this:
 * <pre>{@code
 * GauntletInteractionBlocklist.register(ModBlocks.VESSEL);
 * }</pre>
 *
 * <p>The gauntlet checks this list before registering any RIGHT input.
 * LEFT inputs (attacks) always propagate; the block/entity interaction already
 * happened before the input is counted, so there is no conflict.
 */
public final class GauntletInteractionBlocklist {

    private static final Set<Block> BLOCKLIST = new HashSet<>();

    private GauntletInteractionBlocklist() {}

    /** Registers a block that should suppress gauntlet RIGHT-click inputs. */
    public static void register(Block block) {
        BLOCKLIST.add(block);
    }

    /** Returns {@code true} if right-clicking the block at {@code pos} should NOT
     *  register as a gauntlet input. */
    public static boolean isSuppressed(World world, BlockPos pos) {
        if (pos == null) return false;
        Block block = world.getBlockState(pos).getBlock();
        return BLOCKLIST.contains(block);
    }

    /** Unmodifiable view, mostly for debugging. */
    public static Set<Block> all() {
        return Collections.unmodifiableSet(BLOCKLIST);
    }
}