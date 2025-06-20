package dev.overgrown.thaumaturge.spell.tier;

import dev.overgrown.thaumaturge.networking.SpellCastPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AoeSpellDelivery {
    private float radius = 5.0f;
    private final List<Consumer<BlockPos>> effects = new ArrayList<>();
    private ServerWorld casterWorld;
    private ServerPlayerEntity caster;
    private final SpellCastPacket.SpellTier tier;
    private float powerMultiplier = 1.0f;
    private int scatterSize = 0;

    public int getScatterSize() {
        return scatterSize;
    }

    public void setScatterSize(int scatterSize) {
        this.scatterSize = scatterSize;
    }

    public void setCasterWorld(ServerWorld world) {
        this.casterWorld = world;
    }

    public ServerWorld getCasterWorld() {
        return casterWorld;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getRadius() {
        return radius;
    }

    public ServerPlayerEntity getCaster() {
        return caster;
    }

    public void addEffect(Consumer<BlockPos> effect) {
        effects.add(effect);
    }

    public AoeSpellDelivery(SpellCastPacket.SpellTier tier) {
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

    public void execute(ServerPlayerEntity caster) {
        this.caster = caster; // Store the caster when executing
        this.casterWorld = caster.getWorld();
        BlockPos center = caster.getBlockPos();
        int radiusInt = MathHelper.floor(this.radius);
        BlockPos min = center.add(-radiusInt, -radiusInt, -radiusInt);
        BlockPos max = center.add(radiusInt, radiusInt, radiusInt);
        BlockPos.iterate(min, max).forEach(pos -> {
            for (Consumer<BlockPos> effect : effects) {
                effect.accept(pos);
            }
        });
    }
}