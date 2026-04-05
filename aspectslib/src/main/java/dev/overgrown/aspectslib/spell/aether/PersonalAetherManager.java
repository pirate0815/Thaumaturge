package dev.overgrown.aspectslib.spell.aether;

import dev.overgrown.aspectslib.aether.AetherManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;

/**
 * Manages passive regeneration of every living entity's Personal Aether pool.
 *
 * <h3>Regeneration rates (per in-game tick, then converted to per-hour Codex values)</h3>
 * <table border="1">
 *   <tr><th>Source</th><th>Codex rate</th><th>Ticks between +1 unit</th></tr>
 *   <tr><td>Passive rest (awake)</td><td>+5 u/hr</td><td>~720</td></tr>
 *   <tr><td>Sleep</td><td>+20 u/hr</td><td>~180</td></tr>
 *   <tr><td>Ambient (rich env)</td><td>+2–10 u/hr</td><td>varies</td></tr>
 *   <tr><td>Ley Node (passive)</td><td>+15 u/hr</td><td>~240</td></tr>
 * </table>
 *
 * <p>The tick event is registered in {@link dev.overgrown.aspectslib.AspectsLib#onInitialize}.
 */
public final class PersonalAetherManager {

    // Regeneration tick intervals
    /** Ticks between processing personal aether recovery (20 ticks = 1 second). */
    private static final int REGEN_INTERVAL_TICKS = 20;

    /** Base regen per second for an awake, non-sleeping entity: 5 u/hr ≈ 0.00139 u/s */
    private static final double BASE_REGEN_PER_SECOND = 5.0 / 3600.0;

    /**
     * Ambient bonus per unit of chunk Aether density fraction.
     * At max density (1.0) this adds up to +10 u/hr ambient bonus.
     */
    private static final double AMBIENT_BONUS_SCALE = 10.0 / 3600.0;

    private PersonalAetherManager() {}

    /**
     * Registers the server tick listener.  Called once from
     * {@code AspectsLib.onInitialize()}.
     */
    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(PersonalAetherManager::onServerTick);
    }

    // Tick handler
    private static void onServerTick(MinecraftServer server) {
        long tick = server.getOverworld().getTime();
        if (tick % REGEN_INTERVAL_TICKS != 0) return;

        double secondsElapsed = REGEN_INTERVAL_TICKS / 20.0; // 1.0 second

        for (ServerWorld world : server.getWorlds()) {
            for (Entity rawEntity : world.iterateEntities()) {
                if (!(rawEntity instanceof LivingEntity entity)) continue;
                if (!(entity instanceof PersonalAetherPool pool)) continue;

                double current = pool.aspectslib$getPersonalAether();
                double max     = pool.aspectslib$getMaxPersonalAether();
                if (current >= max) continue;

                double regen = computeRegen(entity, world, secondsElapsed);
                pool.aspectslib$restorePersonalAether(regen);
            }
        }
    }

    // Regen computation
    private static double computeRegen(LivingEntity entity, ServerWorld world, double seconds) {
        // Base passive regen
        double regen = BASE_REGEN_PER_SECOND * seconds;

        // Sleep bonus (players sleeping regenerate 4× faster)
        if (entity instanceof net.minecraft.entity.player.PlayerEntity player && player.isSleeping()) {
            regen *= 4.0;
        }

        // Ambient bonus (proportional to chunk Aether density)
        ChunkPos chunkPos = new ChunkPos(entity.getBlockPos());
        if (!AetherManager.isDeadZone(world, chunkPos)) {
            var chunkData = AetherManager.getAetherData(world, chunkPos);
            double totalCurrent = 0, totalMax = 0;
            for (var id : chunkData.getAspectIds()) {
                totalCurrent += chunkData.getCurrentAether(id);
                totalMax     += chunkData.getMaxAether(id);
            }
            if (totalMax > 0) {
                double density = totalCurrent / totalMax;
                regen += AMBIENT_BONUS_SCALE * density * seconds;
            }
        } else {
            // Dead zones suppress even passive regen by 50 %
            regen *= 0.5;
        }

        return regen;
    }

    // Public API
    /**
     * Instantly restores the entity's pool by the given amount.
     * Used by consumables, rest mechanics, etc.
     */
    public static void restore(LivingEntity entity, double amount) {
        if (entity instanceof PersonalAetherPool pool) {
            pool.aspectslib$restorePersonalAether(amount);
        }
    }

    /**
     * Directly sets a practitioner's pool maximum, e.g. when they advance
     * from Apprentice to Journeyman.
     */
    public static void setMaxPool(LivingEntity entity, double newMax) {
        if (entity instanceof PersonalAetherPool pool) {
            pool.aspectslib$setMaxPersonalAether(newMax);
        }
    }

    /** Returns the entity's current pool, or 0 if it has no pool. */
    public static double getPool(LivingEntity entity) {
        if (entity instanceof PersonalAetherPool pool) return pool.aspectslib$getPersonalAether();
        return 0.0;
    }

    /** Returns the entity's pool fraction [0,1], or 0 if no pool. */
    public static double getPoolFraction(LivingEntity entity) {
        if (entity instanceof PersonalAetherPool pool) return pool.aspectslib$getPoolFraction();
        return 0.0;
    }
}