package dev.overgrown.thaumaturge.spell.tier;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SelfSpellDelivery {
    private final List<Consumer<ServerPlayerEntity>> effects = new ArrayList<>();

    public void addEffect(Consumer<ServerPlayerEntity> effect) {
        effects.add(effect);
    }

    public void execute(ServerPlayerEntity caster) {
        effects.forEach(effect -> effect.accept(caster));
    }
}
