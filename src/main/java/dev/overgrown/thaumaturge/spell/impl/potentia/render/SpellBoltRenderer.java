package dev.overgrown.thaumaturge.spell.impl.potentia.render;

import dev.overgrown.thaumaturge.client.render.LaserRenderer;
import dev.overgrown.thaumaturge.component.ModComponents;
import dev.overgrown.thaumaturge.spell.impl.potentia.entity.SpellBoltEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.joml.Vector2f;

@Environment(EnvType.CLIENT)
public class SpellBoltRenderer extends EntityRenderer<SpellBoltEntity, SpellBoltRenderState> {

    private static final LaserRenderer LASER_RENDERER = new LaserRenderer(
            LaserRenderer.LaserPart.DEFAULT, LaserRenderer.LaserPart.DEFAULT,
            1,
            new Vector2f(1 / 16F, 1 / 16F),
            false,
            0,
            0
    );

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
        state.entityId = entity.getId();
        state.seed = entity.getSeed();
        state.tier = entity.getTier();
        state.yaw = entity.getYaw();
        state.pitch = entity.getPitch();
        var caster = entity.getCaster();
        state.originPos = null;
        state.opacity = entity.getOpacity(tickDelta);

        if (caster != null) {
            state.originPos = caster.getLerpedPos(tickDelta).add(0, caster.getHeight() / 2.4F, 0);
            var headRot = -MathHelper.lerp(tickDelta, caster.lastBodyYaw, caster.bodyYaw);
            var isRightHand = caster.getMainHandStack().hasChangedComponent(ModComponents.GAUNTLET_STATE);
            var handVec = new Vec3d((isRightHand ? -1 : 1) * entity.getWidth() / 1.2F, 0, 0);
            state.originPos = state.originPos.add(handVec.rotateY((float) Math.toRadians(headRot)));
        }
    }

    @Override
    public void render(SpellBoltRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (state.originPos == null) {
            return;
        }

        matrices.push();
        var entityPos = new Vec3d(state.x, state.y, state.z);
        var targetPos = state.originPos.add(entityPos.subtract(state.originPos).normalize().multiply(50));
        this.translateToOrigin(matrices, entityPos, state.originPos);
        int segments = 20;
        float lengthMultiplier = (float) (entityPos.distanceTo(state.originPos) / state.originPos.distanceTo(targetPos));
        var spread = 10 / 16F;

        var segmentPartVec = targetPos.subtract(state.originPos).multiply(1F / segments);
        var startVec = state.originPos;
        var tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getDynamicDeltaTicks();
        var randomStart = Random.create(state.entityId);
        var opacity = state.opacity;

        for (int i = 0; i < segments; i++) {
            var startProgress = (1F / segments) * i;
            var endProgress = (1F / segments) * (i + 1);
            var currentProgress = lengthMultiplier <= startProgress ? 0F : (lengthMultiplier >= endProgress ? 1F : (lengthMultiplier - startProgress) / (endProgress - startProgress));

            if (currentProgress > 0F) {
                var end = i == segments - 1 ? targetPos : state.originPos.add(segmentPartVec.multiply(i + 1)).add(randomizeVector(randomStart, spread));
                var offset = startVec.subtract(state.originPos);

                matrices.push();
                matrices.translate(offset.x, offset.y, offset.z);
                LASER_RENDERER
                        .faceAndRender(matrices, vertexConsumers, startVec, end, 1, tickDelta, currentProgress, opacity, 1F);
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