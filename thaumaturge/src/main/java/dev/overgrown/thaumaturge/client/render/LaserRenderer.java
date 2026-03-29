package dev.overgrown.thaumaturge.client.render;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.*;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.awt.*;

public class LaserRenderer {

    private Color glowColor;
    private Color coreColor = Color.WHITE;
    private float rainbow = 0F;
    private float glowOpacity = 1F;
    private float coreOpacity = 1F;
    private int bloom = 3;
    private Vector2f size = new Vector2f(1F / 16F, 1F / 16F);
    private float length = 1F;
    private boolean normalTransparency = false;
    private float rotation = 0F;
    private float rotationSpeed = 0F;
    private float opacityAndSizeModifier = 1F;

    public LaserRenderer(Color color) {
        this.glowColor = color;
    }

    public LaserRenderer(Color glowColor, Color coreColor) {
        this.glowColor = glowColor;
        this.coreColor = coreColor;
    }

    public LaserRenderer color(Color color) {
        this.glowColor = color;
        return this;
    }

    public LaserRenderer color(Color glowColor, Color coreColor) {
        this.glowColor = glowColor;
        this.coreColor = coreColor;
        return this;
    }

    public Color getCoreColor() {
        return this.coreColor;
    }

    public Color getGlowColor() {
        return this.glowColor;
    }

    public LaserRenderer enableRainbow(float speed) {
        this.rainbow = speed;
        return this;
    }

    public float getRainbowSpeed() {
        return this.rainbow;
    }

    public LaserRenderer opacity(float opacity) {
        this.glowOpacity = this.coreOpacity = opacity;
        return this;
    }

    public LaserRenderer opacity(float glowOpacity, float coreOpacity) {
        this.glowOpacity = glowOpacity;
        this.coreOpacity = coreOpacity;
        return this;
    }

    public float getCoreOpacity() {
        return this.coreOpacity;
    }

    public float getGlowOpacity() {
        return this.glowOpacity;
    }

    public LaserRenderer bloom(int bloom) {
        this.bloom = MathHelper.clamp(bloom, 0, 10);
        return this;
    }

    public int getBloom() {
        return this.bloom;
    }

    public LaserRenderer size(float size) {
        return this.size(size, size);
    }

    public LaserRenderer size(float width, float height) {
        this.size = new Vector2f(width, height);
        return this;
    }

    public LaserRenderer size(Vector2f size) {
        this.size = size;
        return this;
    }

    public Vector2f getSize() {
        return this.size;
    }

    public LaserRenderer length(float length) {
        this.length = length;
        return this;
    }

    public float getLength() {
        return this.length;
    }

    public LaserRenderer normalTransparency() {
        this.normalTransparency = true;
        return this;
    }

    public LaserRenderer normalTransparency(boolean normalTransparency) {
        this.normalTransparency = normalTransparency;
        return this;
    }

    public boolean hasNormalTransparency() {
        return this.normalTransparency;
    }

    public LaserRenderer rotate(float rotation) {
        this.rotation = rotation;
        return this;
    }

    public float getRotation() {
        return this.rotation;
    }

    public LaserRenderer rotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
        return this;
    }

    public float getRotationSpeed() {
        return this.rotationSpeed;
    }

    public LaserRenderer opacityAndSizeModifier(float modifier) {
        this.opacityAndSizeModifier = modifier;
        return this;
    }

    public void face(MatrixStack matrixStack, Vec3d origin, Vec3d target) {
        faceVec(matrixStack, origin, target);
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
    }

    public void faceAndRender(MatrixStack matrixStack, VertexConsumerProvider bufferSource, Vec3d origin, Vec3d target, int ticks, float partialTick) {
        matrixStack.push();
        this.face(matrixStack, origin, target);
        this.render(matrixStack, bufferSource, ticks, partialTick);
        matrixStack.pop();
    }

    public void render(MatrixStack matrixStack, VertexConsumerProvider bufferSource, int ticks, float partialTick) {
        var rot = this.rotation;

        if (this.rotationSpeed > 0F) {
            rot += (ticks + partialTick) * rotationSpeed;
        }

        matrixStack.push();
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rot % 360F));

        var consumer = bufferSource.getBuffer(this.normalTransparency ? ModRenderLayers.LASER_NORMAL_TRANSPARENCY : ModRenderLayers.LASER);
        var size = new Vector2f(this.size).mul(this.opacityAndSizeModifier);
        var box = new Box(-size.x / 2F, 0, -size.y / 2F, size.x / 2F, this.length, size.y / 2F);

        if (this.coreOpacity > 0F) {
            var coreColor = this.coreColor;
            renderFilledBox(matrixStack, consumer, box, coreColor.getRed() / 255F, coreColor.getGreen() / 255F, coreColor.getBlue() / 255F, this.coreOpacity * this.opacityAndSizeModifier, 15728640);
        }

        var glowColor = getRenderedGlowColor(ticks, partialTick);
        var r = glowColor.getRed() / 255F;
        var g = glowColor.getGreen() / 255F;
        var b = glowColor.getBlue() / 255F;

        if (this.glowOpacity > 0F) {
            for (int i = 0; i < this.bloom + 1; i++) {
                renderFilledBox(matrixStack, consumer, box.expand(i * 0.5F * 0.0625F), r, g, b, (1F / i / 2) * this.glowOpacity * this.opacityAndSizeModifier, 15728640);
            }
        }

        matrixStack.pop();
    }

    private Color getRenderedGlowColor(int ticks, float partialTick) {
        if (this.rainbow > 0F) {
            int rate = Math.max((int) (25 * (1F - this.rainbow)), 1);
            int j = ticks / rate;
            int k = DyeColor.values().length;
            int l = j % k;
            int m = (j + 1) % k;
            float f = ((float) (ticks % rate) + partialTick) / (float) rate;
            float[] fs = SheepEntity.getRgbColor(DyeColor.byId(l));
            float[] gs = SheepEntity.getRgbColor(DyeColor.byId(m));
            return new Color(fs[0] * (1.0F - f) + gs[0] * f, fs[1] * (1.0F - f) + gs[1] * f, fs[2] * (1.0F - f) + gs[2] * f);
        } else {
            return this.glowColor;
        }
    }

    public static void faceVec(MatrixStack poseStack, Vec3d src, Vec3d dst) {
        double x = dst.x - src.x;
        double y = dst.y - src.y;
        double z = dst.z - src.z;
        double diff = MathHelper.sqrt((float) (x * x + z * z));
        float yaw = (float) (Math.atan2(z, x) * 180 / Math.PI) - 90;
        float pitch = (float) -(Math.atan2(y, diff) * 180 / Math.PI);

        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));
        poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
    }

    public static void renderFilledBox(MatrixStack stack, VertexConsumer vertexConsumer, Box box, float red, float green, float blue, float alpha, int combinedLightIn) {
        Matrix4f matrix = stack.peek().getPositionMatrix();
        vertexConsumer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha).light(combinedLightIn).next();
        vertexConsumer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha).light(combinedLightIn).next();
        vertexConsumer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha).light(combinedLightIn).next();
        vertexConsumer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha).light(combinedLightIn).next();

        vertexConsumer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha).light(combinedLightIn).next();
        vertexConsumer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha).light(combinedLightIn).next();
        vertexConsumer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha).light(combinedLightIn).next();
        vertexConsumer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha).light(combinedLightIn).next();

        vertexConsumer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha).light(combinedLightIn).next();
        vertexConsumer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha).light(combinedLightIn).next();
        vertexConsumer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha).light(combinedLightIn).next();
        vertexConsumer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha).light(combinedLightIn).next();

        vertexConsumer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha).light(combinedLightIn).next();
        vertexConsumer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha).light(combinedLightIn).next();
        vertexConsumer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha).light(combinedLightIn).next();
        vertexConsumer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha).light(combinedLightIn).next();

        vertexConsumer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha).light(combinedLightIn).next();
        vertexConsumer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha).light(combinedLightIn).next();
        vertexConsumer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha).light(combinedLightIn).next();
        vertexConsumer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha).light(combinedLightIn).next();

        vertexConsumer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha).light(combinedLightIn).next();
        vertexConsumer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha).light(combinedLightIn).next();
        vertexConsumer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha).light(combinedLightIn).next();
        vertexConsumer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha).light(combinedLightIn).next();
    }
}
