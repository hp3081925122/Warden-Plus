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
                    .setShaderState(StateAccess.positionColorTexLightmapShader())
                    .setTextureState(new RenderStateShard.TextureStateShard(t, false, false))
                    .setTransparencyState(StateAccess.lightningTransparency())
                    .setCullState(StateAccess.noCull())
                    .setLightmapState(StateAccess.lightmap())
                    .setWriteMaskState(StateAccess.colorWrite())
                    .setDepthTestState(StateAccess.lequalDepthTest())
                    .createCompositeState(false)));

    private static final Function<ResourceLocation, RenderType> TRANSLUCENT = Util.memoize(t -> RenderType.create(
            "wardenplus_translucent",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            8192,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(StateAccess.positionColorTexLightmapShader())
                    .setTextureState(new RenderStateShard.TextureStateShard(t, false, false))
                    .setTransparencyState(StateAccess.translucentTransparency())
                    .setCullState(StateAccess.noCull())
                    .setLightmapState(StateAccess.lightmap())
                    .setWriteMaskState(StateAccess.colorWrite())
                    .setDepthTestState(StateAccess.lequalDepthTest())
                    .createCompositeState(false)));

    public static final RenderType SOLID = RenderType.create(
            "wardenplus_solid",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            8192,
            true,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(StateAccess.positionColorTexLightmapShader())
                    .setTextureState(new RenderStateShard.TextureStateShard(WHITE, false, false))
                    .setTransparencyState(StateAccess.noTransparency())
                    .setCullState(StateAccess.noCull())
                    .setLightmapState(StateAccess.lightmap())
                    .setWriteMaskState(StateAccess.colorDepthWrite())
                    .setDepthTestState(StateAccess.lequalDepthTest())
                    .createCompositeState(false));

    private WpRenderTypes() {
    }

    private static ResourceLocation tex(String name) {
        return new ResourceLocation(WardenPlus.MOD_ID, "textures/vfx/" + name + ".png");
    }

    public static RenderType additive(ResourceLocation texture) {
        return ADDITIVE.apply(texture);
    }

    public static RenderType translucent(ResourceLocation texture) {
        return TRANSLUCENT.apply(texture);
    }

    private static final class StateAccess extends RenderStateShard {
        private StateAccess() {
            super("wardenplus_state_access", () -> {
            }, () -> {
            });
        }

        private static ShaderStateShard positionColorTexLightmapShader() {
            return POSITION_COLOR_TEX_LIGHTMAP_SHADER;
        }

        private static TransparencyStateShard lightningTransparency() {
            return LIGHTNING_TRANSPARENCY;
        }

        private static TransparencyStateShard translucentTransparency() {
            return TRANSLUCENT_TRANSPARENCY;
        }

        private static TransparencyStateShard noTransparency() {
            return NO_TRANSPARENCY;
        }

        private static CullStateShard noCull() {
            return NO_CULL;
        }

        private static LightmapStateShard lightmap() {
            return LIGHTMAP;
        }

        private static WriteMaskStateShard colorWrite() {
            return COLOR_WRITE;
        }

        private static WriteMaskStateShard colorDepthWrite() {
            return COLOR_DEPTH_WRITE;
        }

        private static DepthTestStateShard lequalDepthTest() {
            return LEQUAL_DEPTH_TEST;
        }
    }
}
