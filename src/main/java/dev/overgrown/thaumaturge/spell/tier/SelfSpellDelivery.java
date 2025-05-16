package dev.overgrown.thaumaturge.spell.tier;

import dev.overgrown.thaumaturge.networking.SpellCastPacket;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SelfSpellDelivery {
    private final SpellCastPacket.SpellTier tier;
    private float powerMultiplier = 1.0f;
    private final List<Consumer<ServerPlayerEntity>> effects = new ArrayList<>();

    public SelfSpellDelivery(SpellCastPacket.SpellTier tier) {
        this.tier = tier;
    }

    public SpellCastPacket.SpellTier getTier() {
        return tier;
    }

    public float getPowerMultiplier() {
        return powerMultiplier;
    }

    public void setPowerMultiplier(float powerMultiplier) {
        this.powerMultiplier = powerMultiplier;
    }

    public void addEffect(Consumer<ServerPlayerEntity> effect) {
        effects.add(effect);
    }

    public void execute(ServerPlayerEntity caster) {
        effects.forEach(effect -> effect.accept(caster));
    }
}