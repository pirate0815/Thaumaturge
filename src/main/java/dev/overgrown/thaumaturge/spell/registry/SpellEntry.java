package dev.overgrown.thaumaturge.spell.registry;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public record SpellEntry(Identifier id, SpellExecutor executor) {
    public interface SpellExecutor {
        void execute(ServerPlayerEntity caster);
    }
}