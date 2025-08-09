package dev.overgrown.thaumaturge.spell.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpellContext {
    private final World world;
    private final PlayerEntity caster;
    private final Entity target;
    private final BlockPos pos;
    private int amplifier = 1;

    public SpellContext(World world, PlayerEntity caster, Entity target, BlockPos pos) {
        this.world = world;
        this.caster = caster;
        this.target = target;
        this.pos = pos;
    }

    // Getters
    public World getWorld() { return world; }
    public PlayerEntity getCaster() { return caster; }
    public Entity getTarget() { return target; }
    public BlockPos getPos() { return pos; }
    public int getAmplifier() { return amplifier; }

    // Set amplifier for effect scaling
    public void setAmplifier(int amplifier) {
        this.amplifier = amplifier;
    }
}