package com.wardenplus.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.wardenplus.WardenPlus;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class WpRenderTypes {
    public static final ResourceLocation GLOW = tex("glow");
    public static final ResourceLocation BAND = tex("band");
    public static final ResourceLocation FADE = tex("fade");
    public static final ResourceLocation RUNE = tex("rune");
    public static final ResourceLocation CRACK = tex("crack");
    public static final ResourceLocation NOISE = tex("noise");
    public static final ResourceLocation WHITE = tex("white");
    public static final ResourceLocation VIGNETTE = tex("vignette");

    private static final Function<ResourceLocation, RenderType> ADDITIVE = Util.memoize(t -> RenderType.create(
            "wardenplus_additive",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            8192,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(t, false, false))
                    .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .createCompositeState(false)));

    private static final Function<ResourceLocation, RenderType> TRANSLUCENT = Util.memoize(t -> RenderType.create(
            "wardenplus_translucent",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            8192,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(t, false, false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .createCompositeState(false)));

    public static final RenderType SOLID = RenderType.create(
            "wardenplus_solid",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            8192,
            true,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(WHITE, false, false))
                    .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .createCompositeState(false));

    private WpRenderTypes() {
    }

    private static ResourceLocation tex(String name) {
        return ResourceLocation.fromNamespaceAndPath(WardenPlus.MOD_ID, "textures/vfx/" + name + ".png");
    }

    public static RenderType additive(ResourceLocation texture) {
        return ADDITIVE.apply(texture);
    }

    public static RenderType translucent(ResourceLocation texture) {
        return TRANSLUCENT.apply(texture);
    }
}
