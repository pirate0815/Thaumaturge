package dev.overgrown.thaumaturge.spell.pattern;

import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * Contract for Aspect effects (backport-safe).
 * Only the original three entry points: self, entity, and aoe.
 */
public interface AspectEffect {

    /** Cast on the caster themselves. */
    default void castOnSelf(ServerPlayerEntity caster, List<ModifierEffect> modifiers) {
        // no-op
    }

    /** Cast on a targeted entity. */
    default void castOnEntity(ServerPlayerEntity caster, Entity target, List<ModifierEffect> modifiers) {
        // no-op
    }

    /** Cast as an area-of-effect centered at a block position with a given radius. */
    default void castAoe(ServerPlayerEntity caster, BlockPos center, float radius, List<ModifierEffect> modifiers) {
        // no-op
    }
}
