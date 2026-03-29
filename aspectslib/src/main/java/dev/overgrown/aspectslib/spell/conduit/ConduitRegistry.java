package dev.overgrown.aspectslib.spell.conduit;

import dev.overgrown.aspectslib.AspectsLib;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Tracks which items are permitted as conduits for spell-casting.
 *
 * <p>Items that implement {@link IConduit} are automatically considered valid
 * at cast time without needing an explicit registration entry, this registry
 * is an <em>additional</em> allowlist for vanilla or third-party items that
 * you want to allow as conduits without modifying their class.
 *
 * <h3>Typical use</h3>
 * <pre>{@code
 * // Allow any blaze rod to be used as a conduit
 * ConduitRegistry.register(new Identifier("minecraft", "blaze_rod"));
 * }</pre>
 *
 * <h3>Checking validity</h3>
 * <pre>{@code
 * if (ConduitRegistry.isValidConduit(player.getMainHandStack())) {
 *     // can cast
 * }
 * }</pre>
 */
public final class ConduitRegistry {

    private static final Set<Identifier> REGISTERED_IDS = new HashSet<>();

    private ConduitRegistry() {}

    // Registration
    /**
     * Registers an item identifier as a valid conduit. The item does not need
     * to implement {@link IConduit}; it will simply be allowed through the
     * "is this a conduit?" gate.
     */
    public static void register(Identifier itemId) {
        if (itemId != null) {
            REGISTERED_IDS.add(itemId);
            AspectsLib.LOGGER.debug("Registered conduit item: {}", itemId);
        }
    }

    /** Removes an item identifier from the conduit allow-list. */
    public static void unregister(Identifier itemId) {
        REGISTERED_IDS.remove(itemId);
    }

    // Queries

    /**
     * Returns {@code true} if the given stack can serve as a casting conduit.
     *
     * <p>A stack is valid when any of the following holds:
     * <ol>
     *   <li>The item implements {@link IConduit} directly.</li>
     *   <li>The item's registry identifier has been explicitly registered via
     *       {@link #register(Identifier)}.</li>
     * </ol>
     */
    public static boolean isValidConduit(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        if (item instanceof IConduit) return true;
        Identifier id = Registries.ITEM.getId(item);
        return REGISTERED_IDS.contains(id);
    }

    /** Overload that checks a bare item rather than an ItemStack. */
    public static boolean isValidConduit(Item item) {
        if (item == null) return false;
        if (item instanceof IConduit) return true;
        return REGISTERED_IDS.contains(Registries.ITEM.getId(item));
    }

    /** Checks by identifier alone (no item lookup). */
    public static boolean isRegisteredById(Identifier id) {
        return REGISTERED_IDS.contains(id);
    }

    /** Returns an unmodifiable view of explicitly registered item ids. */
    public static Set<Identifier> getRegisteredIds() {
        return Collections.unmodifiableSet(REGISTERED_IDS);
    }

    /** Clears all explicit registrations (useful for reloads/tests). */
    public static void clear() {
        REGISTERED_IDS.clear();
    }
}