/**
 * SpellEntry.java
 * <p>
 * Represents a spell in the registry with its identifier and implementation
 */
package dev.overgrown.thaumaturge.spell.registry;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public record SpellEntry(Identifier id, SpellExecutor executor) {
    /**
     * Functional interface for spell implementations
     * Defines the behavior when a spell is cast
     */
    public interface SpellExecutor {
        /**
         * Executes the spell effect
         *
         * @param caster The player casting the spell
         */
        void execute(ServerPlayerEntity caster);
    }
}