package dev.overgrown.thaumaturge.spell.impl.potentia.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class SpellBoltRenderState extends EntityRenderState {

    public int entityId;
    public long seed;
    public int tier;
    public float yaw;
    public float pitch;
    public Vec3d originPos;
    public float opacity;

}
