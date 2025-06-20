package dev.overgrown.thaumaturge.client.render;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record LaserRenderer(LaserPart glow,
                            LaserPart core,
                            int bloom, Vector2f size, boolean normalTransparency, float rotation,
                            float rotationSpeed) {

    public static final Codec<Vec2f> VEC2_CODEC = Codec.FLOAT.listOf().comapFlatMap((list) -> Util.decodeFixedLengthList(list, 2).map((floats) -> new Vec2f(floats.getFirst(), floats.get(1))), (vec2) -> List.of(vec2.x, vec2.y));
    public static final Codec<Vector2f> VECTOR_2F_CODEC = VEC2_CODEC.xmap(vec2 -> new Vector2f(vec2.x, vec2.y), vector2f -> new Vec2f(vector2f.x, vector2f.y));
    private static final Codec<Vector2f> VOXEL_VECTOR_2F = VECTOR_2F_CODEC.xmap(vector2f -> vector2f.div(16), vector2f -> vector2f.mul(16));
    public static final Codec<Float> NON_NEGATIVE_VOXEL_FLOAT = Codecs.NON_NEGATIVE_FLOAT.xmap(f -> f / 16, f -> f * 16);
    public static final Codec<Color> COLOR_CODEC = Codec.withAlternative(
            Codec.STRING.xmap(s -> Color.decode(s.startsWith("#") ? s : "#" + s), color -> "#" + Integer.toHexString(color.getRGB()).substring(2)),
            Codec.withAlternative(
                    Codec.INT.listOf(3, 3).xmap(integers -> new Color(integers.getFirst(), integers.get(1), integers.get(2)), color -> {
                        List<Integer> integers = new ArrayList<>();
                        integers.add(color.getRed());
                        integers.add(color.getGreen());
                        integers.add(color.getBlue());
                        return integers;
                    }),
                    Codec.INT.xmap(Color::new, Color::getRGB)
            )
    );
    public static final Codec<Float> FLOAT_OR_BOOLEAN_CODEC = Codec.either(Codec.FLOAT, Codec.BOOL).xmap(
            either -> either.map(
                    left -> left,
                    right -> right ? 1F : 0F
            ),
            f -> f == 0F || f == 1F ? Either.right(f == 1F) : Either.left(f)
    );

    public static final Codec<Vector2f> SIZE_CODEC = Codec.either(VOXEL_VECTOR_2F, NON_NEGATIVE_VOXEL_FLOAT).xmap(
            either -> either.map(vector2f -> vector2f, aFloat -> new Vector2f(aFloat, aFloat)),
            vector2f -> vector2f.x == vector2f.y ? Either.right(vector2f.x) : Either.left(vector2f));

    public static Codec<LaserRenderer> codec(int defaultBloom) {
        return RecordCodecBuilder.create(instance -> instance.group(
                LaserPart.CODEC.optionalFieldOf("glow", LaserPart.DEFAULT).forGetter(LaserRenderer::glow),
                LaserPart.CODEC.optionalFieldOf("core", LaserPart.DEFAULT).forGetter(LaserRenderer::core),
                Codecs.rangedInt(0, 10).optionalFieldOf("bloom", defaultBloom).forGetter(LaserRenderer::bloom),
                SIZE_CODEC.optionalFieldOf("size", new Vector2f(1 / 16F, 1 / 16F)).forGetter(LaserRenderer::size),
                Codec.BOOL.optionalFieldOf("normal_transparency", false).forGetter(LaserRenderer::normalTransparency),
                Codecs.rangedInclusiveFloat(0F, 360F).optionalFieldOf("rotation", 0F).forGetter(LaserRenderer::rotation),
                Codecs.NON_NEGATIVE_FLOAT.optionalFieldOf("rotation_speed", 0F).forGetter(LaserRenderer::rotationSpeed)
        ).apply(instance, LaserRenderer::new));
    }

    public void face(MatrixStack matrixStack, Vec3d origin, Vec3d target) {
        faceVec(matrixStack, origin, target);
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
    }

    public void faceAndRender(MatrixStack matrixStack, VertexConsumerProvider bufferSource, Vec3d origin, Vec3d target, int ticks, float partialTick) {
        this.faceAndRender(matrixStack, bufferSource, origin, target, ticks, partialTick, 1F, 1F, Vec2f.SOUTH_EAST_UNIT);
    }

    public void faceAndRender(MatrixStack matrixStack, VertexConsumerProvider bufferSource, Vec3d origin, Vec3d target, int ticks, float partialTick, float lengthMultiplier, float opacityMultiplier) {
        this.faceAndRender(matrixStack, bufferSource, origin, target, ticks, partialTick, lengthMultiplier, opacityMultiplier, Vec2f.SOUTH_EAST_UNIT);
    }

    public void faceAndRender(MatrixStack matrixStack, VertexConsumerProvider bufferSource, Vec3d origin, Vec3d target, int ticks, float partialTick, float lengthMultiplier, float opacityMultiplier, float sizeMultiplier) {
        this.faceAndRender(matrixStack, bufferSource, origin, target, ticks, partialTick, lengthMultiplier, opacityMultiplier, new Vec2f(sizeMultiplier, sizeMultiplier));
    }

    public void faceAndRender(MatrixStack matrixStack, VertexConsumerProvider bufferSource, Vec3d origin, Vec3d target, int ticks, float partialTick, float lengthMultiplier, float opacityMultiplier, Vec2f sizeMultiplier) {
        matrixStack.push();
        this.face(matrixStack, origin, target);
        this.render(matrixStack, bufferSource, ticks, origin.distanceTo(target) * lengthMultiplier, partialTick, opacityMultiplier, sizeMultiplier);
        matrixStack.pop();
    }

    public void render(MatrixStack matrixStack, VertexConsumerProvider bufferSource, int ticks, double length, float partialTick, float opacityMultiplier, Vec2f sizeMultiplier) {
        var rot = this.rotation;

        if (this.rotationSpeed > 0F) {
            rot += (ticks + partialTick) * rotationSpeed;
        }

        matrixStack.push();
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rot % 360F));

        var consumer = bufferSource.getBuffer(RenderLayer.getLightning());
        var size = new Vector2f(this.size).mul(sizeMultiplier.x, sizeMultiplier.y).mul(this.core.getPulseScale(ticks + partialTick));
        Box box = new Box(-size.x / 2F, 0, -size.y / 2F, size.x / 2F, length, size.y / 2F);

        var coreColor = computeColor(this.core, ticks, partialTick);
        var r = coreColor.getRed() / 255F;
        var g = coreColor.getGreen() / 255F;
        var b = coreColor.getBlue() / 255F;

        if (this.core.opacity > 0F) {
            renderFilledBox(matrixStack.peek().getPositionMatrix(), consumer, box, r, g, b, this.core.opacity * opacityMultiplier, LightmapTextureManager.MAX_SKY_LIGHT_COORDINATE);
        }

        var glowColor = computeColor(this.glow, ticks, partialTick);
        r = glowColor.getRed() / 255F;
        g = glowColor.getGreen() / 255F;
        b = glowColor.getBlue() / 255F;

        if (this.glow.opacity > 0F) {
            float pulse = this.glow.getPulseScale(ticks + partialTick);
            for (int i = 0; i < this.bloom + 1; i++) {
                renderFilledBox(matrixStack.peek().getPositionMatrix(), consumer, box.expand(i * 0.5F * 0.0625F * pulse), r, g, b, (1F / i / 2) * this.glow.opacity * opacityMultiplier, LightmapTextureManager.MAX_SKY_LIGHT_COORDINATE);
            }
        }

        matrixStack.pop();
    }

    private static Color computeColor(LaserPart part, int ticks, float partialTick) {
        if (part.rainbow > 0F) {
            int rate = Math.max((int) (25 * (1F - part.rainbow)), 1);
            int l = ticks / rate;
            int m = DyeColor.values().length;
            int n = l % m;
            int o = (l + 1) % m;
            float h = ((float) (ticks % rate) + partialTick) / rate;
            int p = DyeColor.byIndex(n).getSignColor();
            int q = DyeColor.byIndex(o).getSignColor();
            return new Color(ColorHelper.lerp(h, p, q));
        } else {
            return part.color;
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

    public static void renderFilledBox(Matrix4f matrix, VertexConsumer vertexConsumer, Box box, float red, float green, float blue, float alpha, int combinedLightIn) {
        vertexConsumer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha).light(combinedLightIn);
        vertexConsumer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha).light(combinedLightIn);
        vertexConsumer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha).light(combinedLightIn);
        vertexConsumer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha).light(combinedLightIn);

        vertexConsumer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha).light(combinedLightIn);
        vertexConsumer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha).light(combinedLightIn);
        vertexConsumer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha).light(combinedLightIn);
        vertexConsumer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha).light(combinedLightIn);

        vertexConsumer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha).light(combinedLightIn);
        vertexConsumer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha).light(combinedLightIn);
        vertexConsumer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha).light(combinedLightIn);
        vertexConsumer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha).light(combinedLightIn);

        vertexConsumer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha).light(combinedLightIn);
        vertexConsumer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha).light(combinedLightIn);
        vertexConsumer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha).light(combinedLightIn);
        vertexConsumer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha).light(combinedLightIn);

        vertexConsumer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha).light(combinedLightIn);
        vertexConsumer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha).light(combinedLightIn);
        vertexConsumer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha).light(combinedLightIn);
        vertexConsumer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha).light(combinedLightIn);

        vertexConsumer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha).light(combinedLightIn);
        vertexConsumer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha).light(combinedLightIn);
        vertexConsumer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha).light(combinedLightIn);
        vertexConsumer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha).light(combinedLightIn);
    }

    public record LaserPart(Color color, float opacity, float rainbow, @Nullable Pulse pulse) {

        public static final LaserPart DEFAULT = new LaserPart(Color.WHITE, 1F, 0F, null);

        private static final Codec<LaserPart> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                COLOR_CODEC.optionalFieldOf("color", Color.WHITE).forGetter(LaserPart::color),
                Codecs.rangedInclusiveFloat(0F, 1F).optionalFieldOf("opacity", 1F).forGetter(LaserPart::opacity),
                FLOAT_OR_BOOLEAN_CODEC.optionalFieldOf("rainbow", 0F).forGetter(LaserPart::rainbow),
                Pulse.CODEC.optionalFieldOf("pulse").forGetter(p -> Optional.ofNullable(p.pulse()))
        ).apply(instance, (color, opacity, rainbow, pulse) -> new LaserPart(color, opacity, rainbow, pulse.orElse(null))));

        public static final Codec<LaserPart> CODEC = Codec.either(COLOR_CODEC, DIRECT_CODEC).xmap(colorLaserPartEither ->
                        colorLaserPartEither.map(color1 -> new LaserPart(color1, 1F, 0F, null), laserPart -> laserPart),
                laserPart -> laserPart.opacity == 1F && laserPart.rainbow == 0F && laserPart.pulse == null ?
                        Either.left(laserPart.color()) :
                        Either.right(laserPart));

        public float getPulseScale(float time) {
            return this.pulse != null ? 1F + (this.pulse.scale() * MathHelper.sin(time * this.pulse.frequency())) : 1F;
        }

    }

    public record Pulse(float scale, float frequency) {

        public static final Codec<Pulse> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codecs.NON_NEGATIVE_FLOAT.optionalFieldOf("scale", 1F).forGetter(Pulse::scale),
                Codecs.rangedInclusiveFloat(0F, 10F).optionalFieldOf("frequency", 1F).forGetter(Pulse::frequency)
        ).apply(instance, Pulse::new));

    }
}
