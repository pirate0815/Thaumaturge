package dev.overgrown.thaumaturge.spell.component;

import dev.overgrown.aspectslib.spell.cost.SpellDuration;
import dev.overgrown.aspectslib.spell.cost.SpellRange;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Map;

/**
 * A modular spell-effect block attached to an Aspect.
 *
 * <p>Each Aspect that can be cast through a Focus must have a corresponding
 * {@code GauntletSpellEffect} registered via {@link GauntletSpellEffectRegistry}.
 *
 * <h3>Lifecycle of one cast</h3>
 * <ol>
 *   <li>The combo fires and the gauntlet reads the aspect ID from the active focus.</li>
 *   <li>The effect is looked up in the registry.</li>
 *   <li>Targets are acquired by the gauntlet's delivery helper.</li>
 *   <li>{@link #apply(GauntletCastContext)} is called.</li>
 * </ol>
 *
 * <h3>Cost model</h3>
 * Effects declare their Aspect intensities (1–10) for each Aspect they consume.
 * These are fed to {@link dev.overgrown.aspectslib.spell.cost.AetherCostCalculator}
 * which applies all the formula multipliers automatically.
 */
public interface GauntletSpellEffect {

    /** Identifier matching the Aspect this effect belongs to. */
    Identifier getAspectId();

    /**
     * Aspect intensities consumed by this effect.
     * Key = Aspect ID, Value = intensity 1–10.
     * The primary Aspect should always be present with intensity 3–7.
     */
    Map<Identifier, Integer> getAspectIntensities();

    /** Default range; may be overridden per context. */
    default SpellRange getDefaultRange() {
        return SpellRange.FAR;
    }

    /** Default duration; most combat effects are INSTANT. */
    default SpellDuration getDefaultDuration() {
        return SpellDuration.INSTANT;
    }

    /**
     * Base stability for this effect (0.0–1.0). Effects that are inherently
     * unstable (e.g., Vitium, Alienis) can override this to return a lower
     * value, increasing misfire chance before modifiers are applied.
     *
     * @return stability baseline; default is 1.0 (perfectly stable)
     */
    default double getStabilityBase() {
        return 1.0;
    }

    /**
     * Applies the effect given the resolved cast context.
     *
     * @return {@code true} if something meaningful happened
     */
    boolean apply(GauntletCastContext ctx);

    /**
     * All information available to the effect at execution time.
     *
     * @param caster           the casting player
     * @param world            the server world
     * @param castOrigin       eye position of the caster
     * @param entityTargets    resolved targets
     * @param blockTargets     block positions targeted (for build/break effects)
     * @param focusTier        "lesser" / "advanced" / "greater"
     * @param modifierIds      list of AspectsLib modifier IDs active for this cast
     * @param potencyMult      overall potency multiplier (1.0 = default)
     * @param powered          whether the cast was a "powered" variant
     */
    record GauntletCastContext(
            ServerPlayerEntity caster,
            ServerWorld world,
            Vec3d castOrigin,
            List<Entity> entityTargets,
            List<BlockPos> blockTargets,
            String focusTier,
            List<Identifier> modifierIds,
            double potencyMult,
            boolean powered
    ) {
        /** The first living entity target, or null. */
        public LivingEntity primaryLivingTarget() {
            for (Entity e : entityTargets) {
                if (e instanceof LivingEntity le) return le;
            }
            return null;
        }

        public boolean hasEntityTargets() {
            return !entityTargets.isEmpty();
        }

        public boolean hasBlockTargets() {
            return !blockTargets.isEmpty();
        }
    }
}