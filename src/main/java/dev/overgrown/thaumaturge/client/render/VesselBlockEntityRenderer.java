package dev.overgrown.thaumaturge.client.render;

import dev.overgrown.thaumaturge.block.vessel.VesselBlock;
import dev.overgrown.thaumaturge.block.vessel.VesselBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.world.World;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class VesselBlockEntityRenderer implements BlockEntityRenderer<VesselBlockEntity> {

    private static final float START_XZ = 3f / 16f;
    private static final  float END_XZ = 1f - START_XZ;
    private static final float FLOOR_Y = 2f / 16;
    private static final float Y_STEP = 4f / 16f;


    private static final Sprite waterSprite = MinecraftClient.getInstance().getBlockRenderManager().getModels().getModelParticleSprite(Blocks.WATER.getDefaultState());



    public VesselBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }


    @Override
    public void render(VesselBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity == null ) {return;}
        int water_height = entity.getCachedState().get(VesselBlock.WATER_LEVEL);
        if (water_height == 0) {return;}
        World world = entity.getWorld();
        if (world == null) {return;}
        int sludgeAmount = entity.getSludgeAmount();

        float endY = FLOOR_Y + water_height * Y_STEP;

        int waterColor = BiomeColors.getWaterColor(world, entity.getPos());
        float share = Math.min(sludgeAmount / ((float) VesselBlockEntity.MAX_SLUDGE_AMOUNT), 1f);
        float reverse_share = 1f - share;
        float waterColor_r = (float) ((waterColor << 8) >>> 24);
        float waterColor_g = (float) ((waterColor << 16) >>> 24);
        float waterColor_b = (float) ((waterColor << 24) >>> 24);
        float sludge_r = 87;
        float sludge_g = 58;
        float sludge_b = 70;
        int r = (int) (reverse_share * waterColor_r + share * sludge_r);
        int g = (int) (reverse_share * waterColor_g + share * sludge_g);
        int b = (int) (reverse_share * waterColor_b + share * sludge_b);


        matrices.push();
        matrices.translate(0,0,0);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(ModRenderLayers.getSolid());
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        
        float minU = waterSprite.getMinU();
        float maxU = waterSprite.getMaxU();
        float minV = waterSprite.getMinV();
        float maxV = waterSprite.getMaxV();

        // Top Surface
        vertexConsumer.vertex(matrix, START_XZ, endY, END_XZ).color(r,g,b,255).texture(minU, maxV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0).next();
        vertexConsumer.vertex(matrix, END_XZ, endY, END_XZ).color(r,g,b,255).texture(maxU, maxV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0).next();
        vertexConsumer.vertex(matrix, END_XZ, endY, START_XZ).color(r,g,b,255).texture(maxU, minV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0).next();
        vertexConsumer.vertex(matrix, START_XZ, endY, START_XZ).color(r,g,b,255).texture(minU, minV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0).next();

        matrices.pop();
    }


}
