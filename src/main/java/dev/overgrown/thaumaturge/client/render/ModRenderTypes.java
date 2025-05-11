package dev.overgrown.thaumaturge.client.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormats;

public class ModRenderTypes extends RenderLayer {

    public static final RenderLayer LASER = of(Thaumaturge.MOD_ID + ":laser", VertexFormats.POSITION_COLOR_LIGHT, VertexFormat.DrawMode.QUADS, 256, false, true, MultiPhaseParameters.builder()
//            .setShaderState(RENDERTYPE_LIGHTNING_SHADER)
            .texture(NO_TEXTURE)
            .setCullState(NO_CULL)
            .setWriteMaskState(COLOR_DEPTH_WRITE)
            .setLightmapState(LIGHTMAP)
            .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
            .setLayeringState(POLYGON_OFFSET_LAYERING)
            .createCompositeState(true));

    public static final RenderLayer LASER_NORMAL_TRANSPARENCY = of(Thaumaturge.MOD_ID + ":laser_normal_transparency  ", VertexFormats.POSITION_COLOR_LIGHT, VertexFormat.DrawMode.QUADS, 256, false, true, MultiPhaseParameters.builder()
//            .setShaderState(RENDERTYPE_LIGHTNING_SHADER)
            .texture(NO_TEXTURE)
            .setCullState(NO_CULL)
            .setWriteMaskState(COLOR_DEPTH_WRITE)
            .setLightmapState(LIGHTMAP)
            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
            .setLayeringState(POLYGON_OFFSET_LAYERING)
            .createCompositeState(true));

}
