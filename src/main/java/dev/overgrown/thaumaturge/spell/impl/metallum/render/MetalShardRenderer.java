package dev.overgrown.thaumaturge.spell.impl.metallum.render;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.spell.impl.metallum.entity.MetalShardEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.ProjectileEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class MetalShardRenderer extends EntityRenderer<MetalShardEntity, ProjectileEntityRenderState> {
    private static final Identifier TEXTURE = Thaumaturge.identifier("textures/entity/metal_shard.png");

    public MetalShardRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public ProjectileEntityRenderState createRenderState() {
        return new ProjectileEntityRenderState();
    }

    @Override
    public void render(ProjectileEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        // Add rendering logic here
    }

    public Identifier getTexture(ProjectileEntityRenderState state) {
        return TEXTURE;
    }
}