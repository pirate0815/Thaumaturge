package dev.overgrown.aspectslib.spell.unraveling;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.spell.aether.PersonalAetherPool;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Tracks per-entity Unraveling stress and fires consequences when thresholds
 * are crossed.
 *
 * <h3>Stress accumulation model</h3>
 * <ul>
 *   <li>Every time a spell overdrawing the Personal Aether pool is attempted
 *       (pool fraction &lt; 0.10 at cast time), stress increases by
 *       {@code overdraw_fraction × OVERDRAW_STRESS_FACTOR}.</li>
 *   <li>Every tick the pool is in {@code CRITICAL} state, a tiny amount of
 *       passive stress accumulates ({@code PASSIVE_STRESS_PER_TICK}).</li>
 *   <li>Stress decays naturally over time at {@code DECAY_PER_TICK} when the
 *       pool is {@code NOMINAL}.</li>
 * </ul>
 *
 * <h3>Stage consequences</h3>
 * Concrete game effects for each stage are dispatched via
 * {@link UnravelingConsequences}, which consuming mods can subscribe to.
 * This class handles only the tracking and advancement logic.
 */
public final class UnravelingTracker {

    /** Stress added per unit of overdraw fraction (how far below 0 the pool went). */
    private static final float OVERDRAW_STRESS_FACTOR = 0.08f;

    /** Passive stress per tick when pool is CRITICAL (1–10%). */
    private static final float PASSIVE_STRESS_PER_TICK = 0.0001f;

    /** Stress decay per tick when pool is NOMINAL (≥25%). */
    private static final float DECAY_PER_TICK = 0.00005f;

    /** How often the tracker ticks per server-tick cycle. */
    private static final int   TICK_INTERVAL = 20; // 1 second

    // Per-entity stress storage (server-side only)
    private static final Map<UUID, Float> STRESS_MAP = new ConcurrentHashMap<>();

    private UnravelingTracker() {}

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(UnravelingTracker::onTick);
    }

    private static void onTick(MinecraftServer server) {
        long tick = server.getOverworld().getTime();
        if (tick % TICK_INTERVAL != 0) return;

        for (ServerWorld world : server.getWorlds()) {
            // world.iterateEntities() returns Iterable<Entity> in 1.20.1.
            for (Entity rawEntity : world.iterateEntities()) {
                if (!(rawEntity instanceof LivingEntity entity)) continue;
                if (!(entity instanceof PersonalAetherPool pool)) continue;

                PersonalAetherPool.PoolState state = pool.aspectslib$getPoolState();
                UUID uuid = entity.getUuid();
                float stress = STRESS_MAP.getOrDefault(uuid, 0f);

                switch (state) {
                    case NOMINAL   -> stress = Math.max(0f, stress - DECAY_PER_TICK * TICK_INTERVAL);
                    case CRITICAL  -> stress = Math.min(1f, stress + PASSIVE_STRESS_PER_TICK * TICK_INTERVAL);
                    case DEPLETED, EXHAUSTED -> { /* neutral; no change */ }
                }

                STRESS_MAP.put(uuid, stress);

                UnravelingStage current = UnravelingStage.forStress(stress);
                if (!current.isHealthy()) {
                    UnravelingConsequences.apply(entity, current, stress);

                    // Warn players of their stage
                    if (entity instanceof ServerPlayerEntity player && tick % 200 == 0) {
                        player.sendMessage(
                                Text.translatable("aspectslib.unraveling." + current.name().toLowerCase()),
                                true
                        );
                    }
                }
            }
        }
    }

    /**
     * Records an overdraw event.  Call this whenever a practitioner casts a
     * spell that draws their pool below 0 (forced draws via Entity Pact, etc.).
     *
     * @param entity the caster
     * @param overdrawFraction how far below 0 the pool went (positive value; e.g. 0.05 = drew 5% of max beyond empty)
     */
    public static void recordOverdraw(LivingEntity entity, float overdrawFraction) {
        UUID uuid = entity.getUuid();
        float current = STRESS_MAP.getOrDefault(uuid, 0f);
        float increase = Math.max(0f, overdrawFraction) * OVERDRAW_STRESS_FACTOR;
        STRESS_MAP.put(uuid, Math.min(1f, current + increase));
        AspectsLib.LOGGER.debug("Unraveling stress for {}: {:.3f} (+{:.3f})",
                entity.getName().getString(), STRESS_MAP.get(uuid), increase);
    }

    /**
     * Returns the current Unraveling stress for an entity (0.0 = none,
     * 1.0 = maximum).
     */
    public static float getStress(LivingEntity entity) {
        return STRESS_MAP.getOrDefault(entity.getUuid(), 0f);
    }

    /** Returns the current {@link UnravelingStage} for an entity. */
    public static UnravelingStage getStage(LivingEntity entity) {
        return UnravelingStage.forStress(getStress(entity));
    }

    /**
     * Clears an entity's stress entirely (e.g., after extended rest or a
     * purification ritual performed by another practitioner).
     */
    public static void clearStress(LivingEntity entity) {
        STRESS_MAP.remove(entity.getUuid());
    }

    public static void writeNbt(LivingEntity entity, NbtCompound nbt) {
        float stress = STRESS_MAP.getOrDefault(entity.getUuid(), 0f);
        if (stress > 0f) nbt.putFloat("AspectLibUnravelingStress", stress);
    }

    public static void readNbt(LivingEntity entity, NbtCompound nbt) {
        if (nbt.contains("AspectLibUnravelingStress")) {
            STRESS_MAP.put(entity.getUuid(), nbt.getFloat("AspectLibUnravelingStress"));
        }
    }
}