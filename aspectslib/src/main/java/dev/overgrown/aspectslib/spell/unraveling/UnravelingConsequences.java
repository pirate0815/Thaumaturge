package dev.overgrown.aspectslib.spell.unraveling;

import dev.overgrown.aspectslib.AspectsLib;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Applies game-world consequences when an entity's Unraveling stage advances.
 *
 * <p>Default consequences are registered here; consuming mods can add their own
 * via {@link #addListener}.  Listeners receive the entity and the current stage
 * every {@link UnravelingTracker} tick interval while the entity is above
 * {@link UnravelingStage#NONE}.
 *
 * <h3>Default effects</h3>
 * <table border="1">
 *   <tr><th>Stage</th><th>Default effect (every 5 seconds while active)</th></tr>
 *   <tr><td>ASPECT_BLEED</td><td>Mining Fatigue I</td></tr>
 *   <tr><td>RESONANCE_FRACTURE</td><td>Nausea + Weakness I</td></tr>
 *   <tr><td>ASPECT_SCREAM</td><td>Blindness + Weakness II + Nausea</td></tr>
 *   <tr><td>UNRAVELING</td><td>Wither I + Slowness III + Weakness III</td></tr>
 *   <tr><td>OPENING</td><td>Wither II + persistent effects; spawn warning</td></tr>
 * </table>
 */
public final class UnravelingConsequences {

    private static final List<BiConsumer<LivingEntity, UnravelingStage>> LISTENERS = new ArrayList<>();

    static {
        // Register default effects
        addListener(UnravelingConsequences::defaultEffects);
    }

    private UnravelingConsequences() {}

    /**
     * Fires all registered consequence listeners for the given entity/stage.
     * Called by {@link UnravelingTracker} once per tick interval.
     */
    public static void apply(LivingEntity entity, UnravelingStage stage, float stress) {
        for (BiConsumer<LivingEntity, UnravelingStage> listener : LISTENERS) {
            try {
                listener.accept(entity, stage);
            } catch (Exception e) {
                AspectsLib.LOGGER.error("UnravelingConsequences listener threw: {}", e.getMessage());
            }
        }
    }

    /**
     * Registers a custom consequence listener.
     * The consumer is invoked once per 20-tick interval for every entity whose
     * stage is above NONE.
     */
    public static void addListener(BiConsumer<LivingEntity, UnravelingStage> listener) {
        LISTENERS.add(listener);
    }

    /** Returns an unmodifiable view of all registered listeners. */
    public static List<BiConsumer<LivingEntity, UnravelingStage>> getListeners() {
        return Collections.unmodifiableList(LISTENERS);
    }

    private static void defaultEffects(LivingEntity entity, UnravelingStage stage) {
        // Effects are applied as short-duration status effects so they expire naturally if the stage retreats.
        final int SHORT = 100; // 5 seconds
        switch (stage) {
            case ASPECT_BLEED -> {
                entity.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.MINING_FATIGUE, SHORT, 0, true, false, true));
            }
            case RESONANCE_FRACTURE -> {
                entity.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.NAUSEA, SHORT, 0, true, false, true));
                entity.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.WEAKNESS, SHORT, 0, true, false, true));
            }
            case ASPECT_SCREAM -> {
                entity.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.BLINDNESS, SHORT, 0, true, false, true));
                entity.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.WEAKNESS, SHORT, 1, true, false, true));
                entity.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.NAUSEA, SHORT, 0, true, false, true));
            }
            case UNRAVELING -> {
                entity.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.WITHER, SHORT, 0, true, true, true));
                entity.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SLOWNESS, SHORT, 2, true, false, true));
                entity.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.WEAKNESS, SHORT, 2, true, false, true));
            }
            case OPENING -> {
                entity.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.WITHER, SHORT, 1, true, true, true));
                entity.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SLOWNESS, SHORT, 4, true, false, true));
                entity.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.WEAKNESS, SHORT, 4, true, false, true));
                // Structural consequence: mark dead zone at location
                if (!entity.getWorld().isClient()) {
                    var serverWorld = (net.minecraft.server.world.ServerWorld) entity.getWorld();
                    var chunkPos = entity.getChunkPos();
                    dev.overgrown.aspectslib.aether.AetherManager
                            .markAsDeadZone(serverWorld, chunkPos,
                                    new dev.overgrown.aspectslib.aether.DeadZoneData(
                                            false, serverWorld.getTime()));
                    AspectsLib.LOGGER.warn(
                            "OPENING-stage Unraveling at {} caused a Dead Zone at chunk {}",
                            entity.getName().getString(), chunkPos);
                }
            }
            default -> { /* NONE — no effect */ }
        }
    }
}