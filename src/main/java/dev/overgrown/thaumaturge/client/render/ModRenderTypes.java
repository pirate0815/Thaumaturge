package dev.overgrown.thaumaturge.client.render;

import net.minecraft.client.render.RenderLayer;

public abstract class ModRenderTypes extends RenderLayer {

    public ModRenderTypes(String name, int size, boolean hasCrumbling, boolean translucent, Runnable begin, Runnable end) {
        super(name, size, hasCrumbling, translucent, begin, end);
    }

//    public static final RenderLayer LASER = of(Thaumaturge.MOD_ID + ":laser", VertexFormats.POSITION_COLOR_LIGHT, VertexFormat.DrawMode.QUADS, 256, false, true, MultiPhaseParameters.builder()
////            .setShaderState(RENDERTYPE_LIGHTNING_SHADER)
//            .texture(NO_TEXTURE)
//            .setCullState(NO_CULL)
//            .setWriteMaskState(COLOR_DEPTH_WRITE)
//            .setLightmapState(LIGHTMAP)
//            .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
//            .setLayeringState(POLYGON_OFFSET_LAYERING)
//            .createCompositeState(true));
//
//    public static final RenderLayer LASER_NORMAL_TRANSPARENCY = of(Thaumaturge.MOD_ID + ":laser_normal_transparency  ", VertexFormats.POSITION_COLOR_LIGHT, VertexFormat.DrawMode.QUADS, 256, false, true, MultiPhaseParameters.builder()
////            .setShaderState(RENDERTYPE_LIGHTNING_SHADER)
//            .texture(NO_TEXTURE)
//            .setCullState(NO_CULL)
//            .setWriteMaskState(COLOR_DEPTH_WRITE)
//            .setLightmapState(LIGHTMAP)
//            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
//            .setLayeringState(POLYGON_OFFSET_LAYERING)
//            .createCompositeState(true));

}
