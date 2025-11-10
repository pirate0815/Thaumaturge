package dev.overgrown.thaumaturge.client.render;

import dev.overgrown.thaumaturge.block.jar.entity.JarBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class JarBlockEntityRenderer implements BlockEntityRenderer<JarBlockEntity> {

    private static final float START_Y = (1f / 16f);
    private static final float START_XZ = (2f / 16f);
    private static final  float END_XZ = 1f - START_XZ;
    private static final float CONTAINER_HEIGHT = 1 - (4f/16f);

    private static final int ALPHA = 250;
    private static final int RED = 14;
    private static final int GREEN = 102;
    private static final int BLUE = 204;

    private static final Sprite waterSprite = MinecraftClient.getInstance()
            .getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)
            .apply(new Identifier("thaumaturge", "block/jar-fluid"));



    public JarBlockEntityRenderer(BlockEntityRendererFactory.Context context) {}


    @Override
    public void render(JarBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity == null ) {return;}
        int level = entity.getLevel();
        if (level <= 0) return;

        matrices.push();
        float endY = START_Y + (((float) level) / (float) entity.getMaxLevel()) * CONTAINER_HEIGHT;
        matrices.translate(0,0,0);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(ModRenderLayers.getTranslucent());
        Matrix4f matrix = matrices.peek().getPositionMatrix();



        float minU = waterSprite.getMinU();
        float maxU = waterSprite.getMaxU();
        float minV = waterSprite.getMinV();
        float maxV = waterSprite.getMaxV();

        // Top Surface
        vertexConsumer.vertex(matrix, START_XZ, endY, END_XZ).color(RED, GREEN, BLUE, ALPHA).texture(minU, maxV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0).next();
        vertexConsumer.vertex(matrix, END_XZ, endY, END_XZ).color(RED, GREEN, BLUE, ALPHA).texture(maxU, maxV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0).next();
        vertexConsumer.vertex(matrix, END_XZ, endY, START_XZ).color(RED, GREEN, BLUE, ALPHA).texture(maxU, minV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0).next();
        vertexConsumer.vertex(matrix, START_XZ, endY, START_XZ).color(RED, GREEN, BLUE, ALPHA).texture(minU, minV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0).next();

        // Bottom Surface
        vertexConsumer.vertex(matrix, START_XZ, START_Y, START_XZ).color(RED, GREEN, BLUE, ALPHA).texture(minU, minV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, -1, 0).next();
        vertexConsumer.vertex(matrix, END_XZ, START_Y, START_XZ).color(RED, GREEN, BLUE, ALPHA).texture(maxU, minV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, -1, 0).next();
        vertexConsumer.vertex(matrix, END_XZ, START_Y, END_XZ).color(RED, GREEN, BLUE, ALPHA).texture(maxU, maxV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, -1, 0).next();
        vertexConsumer.vertex(matrix, START_XZ, START_Y, END_XZ).color(RED, GREEN, BLUE, ALPHA).texture(minU, maxV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, -1, 0).next();

        // North Surface
        vertexConsumer.vertex(matrix, START_XZ, START_Y, END_XZ).color(RED, GREEN, BLUE, ALPHA).texture(minU, minV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, 1).next();
        vertexConsumer.vertex(matrix, END_XZ, START_Y, END_XZ).color(RED, GREEN, BLUE, ALPHA).texture(maxU, minV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, 1).next();
        vertexConsumer.vertex(matrix, END_XZ, endY, END_XZ).color(RED, GREEN, BLUE, ALPHA).texture(maxU, minV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, 1).next();
        vertexConsumer.vertex(matrix, START_XZ, endY, END_XZ).color(RED, GREEN, BLUE, ALPHA).texture(minU, minV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, 1).next();

        // South Surface
        vertexConsumer.vertex(matrix, START_XZ, endY, START_XZ).color(RED, GREEN, BLUE, ALPHA).texture(minU, minV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, -1).next();
        vertexConsumer.vertex(matrix, END_XZ, endY, START_XZ).color(RED, GREEN, BLUE, ALPHA).texture(maxU, minV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, -1).next();
        vertexConsumer.vertex(matrix, END_XZ, START_Y, START_XZ).color(RED, GREEN, BLUE, ALPHA).texture(maxU, minV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, -1).next();
        vertexConsumer.vertex(matrix, START_XZ, START_Y, START_XZ).color(RED, GREEN, BLUE, ALPHA).texture(minU, minV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, -1).next();

        // West Surface
        vertexConsumer.vertex(matrix, START_XZ, endY, END_XZ).color(RED, GREEN, BLUE, ALPHA).texture(minU, maxV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, -1).next();
        vertexConsumer.vertex(matrix, START_XZ, endY, START_XZ).color(RED, GREEN, BLUE, ALPHA).texture(minU, minV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, -1).next();
        vertexConsumer.vertex(matrix, START_XZ, START_Y, START_XZ).color(RED, GREEN, BLUE, ALPHA).texture(maxU, minV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, -1).next();
        vertexConsumer.vertex(matrix, START_XZ, START_Y, END_XZ).color(RED, GREEN, BLUE, ALPHA).texture(maxU, maxV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, -1).next();

        // East Surface
        vertexConsumer.vertex(matrix, END_XZ, START_Y, END_XZ).color(RED, GREEN, BLUE, ALPHA).texture(minU, maxV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, -1).next();
        vertexConsumer.vertex(matrix, END_XZ, START_Y, START_XZ).color(RED, GREEN, BLUE, ALPHA).texture(minU, minV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, -1).next();
        vertexConsumer.vertex(matrix, END_XZ, endY, START_XZ).color(RED, GREEN, BLUE, ALPHA).texture(maxU, minV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, -1).next();
        vertexConsumer.vertex(matrix, END_XZ, endY, END_XZ).color(RED, GREEN, BLUE, ALPHA).texture(maxU, minV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 0, -1).next();

        matrices.pop();
    }


}
