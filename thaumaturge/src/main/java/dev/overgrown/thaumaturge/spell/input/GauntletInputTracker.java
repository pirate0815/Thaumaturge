package dev.overgrown.thaumaturge.spell.input;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side input tracking for the Wynncraft-style combo system.
 *
 * <h3>How it works</h3>
 * <ol>
 *   <li>Every LEFT or RIGHT click while holding a gauntlet is fed into the tracker
 *       via {@link #onLeftClick} / {@link #onRightClick}.</li>
 *   <li>Inputs older than {@value #TIMEOUT_TICKS} ticks (≈ 1.5 seconds) are discarded.</li>
 *   <li>After each new input the last three inputs are tested against all
 *       {@link ComboPattern}s.  If a match is found, the combo is returned and
 *       the input deque is cleared so the same combo cannot double-fire.</li>
 *   <li>After a combo fires, a brief cooldown prevents stale trailing inputs
 *       from starting a new sequence.</li>
 *   <li>The cleanup task runs every 20 ticks (1 second) to evict stale entries.</li>
 * </ol>
 */
public final class GauntletInputTracker {

    /** Ticks an input stays valid before it is too old to be part of a combo. */
    public static final int TIMEOUT_TICKS = 30; // 1.5 s

    /** Ticks after a combo fires during which all new inputs are ignored. */
    private static final int POST_COMBO_COOLDOWN = 5; // ~0.25 s

    private record TimedInput(GauntletInput input, long tick) {}

    // Per-player circular buffer of recent inputs.
    private static final Map<UUID, ArrayDeque<TimedInput>> TRACKER = new ConcurrentHashMap<>();

    // Per-player cooldown: the game tick at which the cooldown expires.
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();

    private GauntletInputTracker() {}

    // Initialisation

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(GauntletInputTracker::tick);
    }

    /** Periodic cleanup, evicts deques for players who are no longer online. */
    private static void tick(MinecraftServer server) {
        if (server.getTicks() % 20 != 0) return;
        Set<UUID> online = new HashSet<>();
        server.getPlayerManager().getPlayerList().forEach(p -> online.add(p.getUuid()));
        TRACKER.keySet().removeIf(uuid -> !online.contains(uuid));
        COOLDOWNS.keySet().removeIf(uuid -> !online.contains(uuid));
    }

    // Input registration
    /**
     * Call when the player swings (attacks block, entity, or air) while holding a gauntlet.
     * Returns the matched {@link ComboPattern} if one completed, or {@code empty}.
     */
    public static Optional<ComboPattern> onLeftClick(ServerPlayerEntity player, int maxSlots) {
        return record(player, GauntletInput.LEFT, maxSlots);
    }

    /**
     * Call from {@code ResonanceGauntletItem.use()} when the player right-clicks.
     * Returns the matched {@link ComboPattern} if one completed, or {@code empty}.
     */
    public static Optional<ComboPattern> onRightClick(ServerPlayerEntity player, int maxSlots) {
        return record(player, GauntletInput.RIGHT, maxSlots);
    }

    /** Clears the input deque for {@code player}. */
    public static void reset(ServerPlayerEntity player) {
        TRACKER.remove(player.getUuid());
    }

    // Internal
    private static Optional<ComboPattern> record(ServerPlayerEntity player,
                                                 GauntletInput input,
                                                 int maxSlots) {
        UUID uuid = player.getUuid();
        long now  = player.getWorld().getTime();

        // Ignore inputs during post-combo cooldown
        Long cooldownEnd = COOLDOWNS.get(uuid);
        if (cooldownEnd != null && now < cooldownEnd) {
            return Optional.empty();
        }
        COOLDOWNS.remove(uuid);

        ArrayDeque<TimedInput> deque = TRACKER.computeIfAbsent(uuid, k -> new ArrayDeque<>());

        // Evict stale inputs
        deque.removeIf(ti -> (now - ti.tick()) > TIMEOUT_TICKS);

        // Add new input (keep at most 3)
        deque.addLast(new TimedInput(input, now));
        if (deque.size() > 3) deque.pollFirst();

        // Convert to plain-input deque for matching
        ArrayDeque<GauntletInput> inputs = new ArrayDeque<>(3);
        deque.forEach(ti -> inputs.addLast(ti.input()));

        Thaumaturge.LOGGER.info("[Gauntlet] {} input: {} → buffer now {} (maxSlots={})",
                player.getName().getString(), input, inputs, maxSlots);

        // Check combos
        for (ComboPattern pattern : ComboPattern.ALL) {
            if (pattern.matches(inputs, maxSlots)) {
                deque.clear();
                TRACKER.remove(uuid);
                // Set cooldown to absorb trailing inputs from the same physical click
                COOLDOWNS.put(uuid, now + POST_COMBO_COOLDOWN);
                Thaumaturge.LOGGER.info(
                        "[Gauntlet] {} triggered combo {}", player.getName().getString(), pattern);
                return Optional.of(pattern);
            }
        }

        return Optional.empty();
    }
}
