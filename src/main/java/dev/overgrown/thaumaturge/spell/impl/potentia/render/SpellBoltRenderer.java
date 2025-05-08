package dev.overgrown.thaumaturge.spell.impl.potentia.render;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.spell.impl.potentia.entity.SpellBoltEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class SpellBoltRenderer extends EntityRenderer<SpellBoltEntity, SpellBoltRenderState> {
    public SpellBoltRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public SpellBoltRenderState createRenderState() {
        return new SpellBoltRenderState();
    }

    @Override
    public void updateRenderState(SpellBoltEntity entity, SpellBoltRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.seed = entity.getSeed();
        state.tier = entity.getTier();
    }

    @Override
    public void render(SpellBoltRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        renderBolt(matrices, vertexConsumers, state.tier, state.seed);
    }

    private void renderBolt(MatrixStack matrices, VertexConsumerProvider consumers, int tier, long seed) {
        VertexConsumer consumer = consumers.getBuffer(RenderLayer.getLightning());
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float r = 0.2f, g = 0.2f, b = 1.0f;
        if (tier > 1) b = 0.8f;

        Random random = Random.create(seed);
        for (int i = 0; i < 3; i++) {
            generateBranch(matrix, consumer, random, r, g, b);
        }
    }

    private void generateBranch(Matrix4f matrix, VertexConsumer consumer, Random random, float r, float g, float b) {
        for (int j = 0; j < 5; j++) {
            float offset = j * 0.5f;
            consumer.vertex(matrix, offset, 0, 0).color(r, g, b, 0.5f);
            consumer.vertex(matrix, offset + 0.5f, 1, 0).color(r, g, b, 0.5f);
        }
    }

    public Identifier getTexture(SpellBoltRenderState state) {
        return Thaumaturge.identifier("textures/entity/spell_bolt.png");
    }
}