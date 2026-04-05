package dev.overgrown.aspectslib.spell.perception;

/**
 * Interface injected onto {@link net.minecraft.entity.LivingEntity} via
 * {@link dev.overgrown.aspectslib.mixin.spell.LivingEntitySightMixin}.
 *
 * <p>Consuming mods (e.g., the Thaumaturge mod) query this interface to
 * determine whether and at what fidelity to render the Aether overlay.
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * if (player instanceof AetherSightCapability sight) {
 *     if (sight.aspectslib$getSightData().isActive()) {
 *         renderAetherOverlay(sight.aspectslib$getSightData().getStage());
 *     }
 * }
 * }</pre>
 *
 * <p>For Staff-wielding Archmages, the consuming mod should call
 * {@code sight.aspectslib$getSightData().addPracticeTicks(1)} once per tick
 * while the Staff is equipped.
 */
public interface AetherSightCapability {

    /** Returns the mutable {@link AetherSightData} for this entity. */
    AetherSightData aspectslib$getSightData();

    // Convenience shortcuts
    /** Activates Aether Sight. Returns false if stage is too low. */
    default boolean aspectslib$activateSight() {
        return aspectslib$getSightData().activate();
    }

    /** Deactivates Aether Sight (no-op at Stage 5). */
    default void aspectslib$deactivateSight() {
        aspectslib$getSightData().deactivate();
    }

    /** Returns true if Aether Sight is currently rendering. */
    default boolean aspectslib$isSightActive() {
        return aspectslib$getSightData().isActive();
    }

    /** Returns the current development stage (0–5). */
    default int aspectslib$getSightStage() {
        return aspectslib$getSightData().getStage();
    }
}