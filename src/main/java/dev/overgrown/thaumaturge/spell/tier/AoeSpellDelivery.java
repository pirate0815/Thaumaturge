package dev.overgrown.thaumaturge.spell.tier;

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
    private ServerPlayerEntity caster; // Add caster field

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

    public void execute(ServerPlayerEntity caster) {
        this.caster = caster; // Store the caster when executing
        this.casterWorld = (ServerWorld) caster.getWorld();
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