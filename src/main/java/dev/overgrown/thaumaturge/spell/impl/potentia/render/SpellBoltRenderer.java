package dev.overgrown.thaumaturge.spell.impl.potentia.render;

import dev.overgrown.thaumaturge.client.render.LaserRenderer;
import dev.overgrown.thaumaturge.spell.impl.potentia.entity.SpellBoltEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.awt.*;

@Environment(EnvType.CLIENT)
public class SpellBoltRenderer extends EntityRenderer<SpellBoltEntity> {

    private static final LaserRenderer LASER_RENDERER = new LaserRenderer(
            Color.WHITE);

    public SpellBoltRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(SpellBoltEntity entity) {
        return null;
    }

    @Override
    public void render(SpellBoltEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        var caster = entity.getCaster();

        if (caster == null) {
            return;
        }

        var originPos = caster.getLerpedPos(tickDelta).add(0, caster.getHeight() / 2.4F, 0);

        matrices.push();
        var entityPos = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
        var targetPos = originPos.add(entityPos.subtract(originPos).normalize().multiply(50));
        this.translateToOrigin(matrices, entityPos, originPos);
        int segments = 20;
        float lengthMultiplier = (float) (entityPos.distanceTo(originPos) / originPos.distanceTo(targetPos));
        var spread = 10 / 16F;

        var segmentPartVec = targetPos.subtract(originPos).multiply(1F / segments);
        var startVec = originPos;
        var randomStart = Random.create(entity.getId());
        var opacity = entity.getOpacity(tickDelta);

        for (int i = 0; i < segments; i++) {
            var startProgress = (1F / segments) * i;
            var endProgress = (1F / segments) * (i + 1);
            var currentProgress = lengthMultiplier <= startProgress ? 0F : (lengthMultiplier >= endProgress ? 1F : (lengthMultiplier - startProgress) / (endProgress - startProgress));

            if (currentProgress > 0F) {
                var end = i == segments - 1 ? targetPos : originPos.add(segmentPartVec.multiply(i + 1)).add(randomizeVector(randomStart, spread));
                var offset = startVec.subtract(originPos);

                matrices.push();
                matrices.translate(offset.x, offset.y, offset.z);
                LASER_RENDERER.opacity(opacity);
                LASER_RENDERER.length((float) (startVec.distanceTo(end) * currentProgress));
                LASER_RENDERER
                        .faceAndRender(matrices, vertexConsumers, startVec, end, 1, tickDelta);
                matrices.pop();
                startVec = end;
            }
        }

        matrices.pop();
    }

    private void translateToOrigin(MatrixStack matrixStack, Vec3d entityPos, Vec3d origin) {
        var delta = origin.subtract(entityPos);
        matrixStack.translate(delta.x, delta.y, delta.z);
    }

    private static Vec3d randomizeVector(Random random, float spread) {
        return new Vec3d((random.nextFloat() - 0.5F) * 2F * spread, (random.nextFloat() - 0.5F) * 2F * spread, (random.nextFloat() - 0.5F) * 2F * spread);
    }
}