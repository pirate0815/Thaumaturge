package dev.overgrown.thaumaturge.spell.impl.potentia.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.EntityRenderState;

@Environment(EnvType.CLIENT)
public class SpellBoltRenderState extends EntityRenderState {
    public long seed;
    public int tier;
    public float yaw;
    public float pitch;
}
