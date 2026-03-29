package dev.overgrown.thaumaturge.client.render;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

public class ModRenderLayers extends RenderLayer {

    public ModRenderLayers(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
    }

    public static final RenderLayer LASER = of(Thaumaturge.MOD_ID + ":laser", VertexFormats.POSITION_COLOR_LIGHT, VertexFormat.DrawMode.QUADS, 256, false, true, RenderLayer.MultiPhaseParameters.builder()
            .program(LIGHTNING_PROGRAM)
            .texture(NO_TEXTURE)
            .cull(DISABLE_CULLING)
            .writeMaskState(ALL_MASK)
            .lightmap(ENABLE_LIGHTMAP)
            .transparency(LIGHTNING_TRANSPARENCY)
            .layering(POLYGON_OFFSET_LAYERING)
            .build(true));

    public static final RenderLayer LASER_NORMAL_TRANSPARENCY = of(Thaumaturge.MOD_ID + ":laser_normal_transparency  ", VertexFormats.POSITION_COLOR_LIGHT, VertexFormat.DrawMode.QUADS, 256, false, true, RenderLayer.MultiPhaseParameters.builder()
            .program(LIGHTNING_PROGRAM)
            .texture(NO_TEXTURE)
            .cull(DISABLE_CULLING)
            .writeMaskState(ALL_MASK)
            .lightmap(ENABLE_LIGHTMAP)
            .transparency(TRANSLUCENT_TRANSPARENCY)
            .layering(POLYGON_OFFSET_LAYERING)
            .build(true));
}
