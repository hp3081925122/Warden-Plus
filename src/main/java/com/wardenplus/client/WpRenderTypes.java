package com.wardenplus.client;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.wardenplus.WardenPlus;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

public final class WpRenderTypes {
    public static final Identifier GLOW = tex("glow");
    public static final Identifier BAND = tex("band");
    public static final Identifier FADE = tex("fade");
    public static final Identifier RUNE = tex("rune");
    public static final Identifier CRACK = tex("crack");
    public static final Identifier NOISE = tex("noise");
    public static final Identifier WHITE = tex("white");
    public static final Identifier VIGNETTE = tex("vignette");

    private static final RenderPipeline ADDITIVE_PIPELINE = pipeline("additive", BlendFunction.LIGHTNING, false);
    private static final RenderPipeline TRANSLUCENT_PIPELINE = pipeline("translucent", BlendFunction.TRANSLUCENT, false);
    private static final RenderPipeline SOLID_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(WardenPlus.MOD_ID, "pipeline/solid"))
            .withVertexShader("core/position_tex_color")
            .withFragmentShader("core/position_tex_color")
            .withSampler("Sampler0")
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .build();

    private static final Function<Identifier, RenderType> ADDITIVE = Util.memoize(t -> RenderType.create(
            "wardenplus_additive",
            RenderSetup.builder(ADDITIVE_PIPELINE).withTexture("Sampler0", t).bufferSize(8192).createRenderSetup()));
    private static final Function<Identifier, RenderType> TRANSLUCENT = Util.memoize(t -> RenderType.create(
            "wardenplus_translucent",
            RenderSetup.builder(TRANSLUCENT_PIPELINE).withTexture("Sampler0", t).bufferSize(8192).sortOnUpload().createRenderSetup()));
    public static final RenderType SOLID = RenderType.create(
            "wardenplus_solid",
            RenderSetup.builder(SOLID_PIPELINE).withTexture("Sampler0", WHITE).bufferSize(8192).createRenderSetup());

    private WpRenderTypes() {
    }

    private static Identifier tex(String n) {
        return Identifier.fromNamespaceAndPath(WardenPlus.MOD_ID, "textures/vfx/" + n + ".png");
    }

    private static RenderPipeline pipeline(String name, BlendFunction blend, boolean writeDepth) {
        return RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(WardenPlus.MOD_ID, "pipeline/" + name))
                .withVertexShader("core/position_tex_color")
                .withFragmentShader("core/position_tex_color")
                .withSampler("Sampler0")
                .withColorTargetState(new ColorTargetState(blend))
                .withCull(false)
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, writeDepth))
                .build();
    }

    public static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(ADDITIVE_PIPELINE);
        event.registerPipeline(TRANSLUCENT_PIPELINE);
        event.registerPipeline(SOLID_PIPELINE);
    }

    public static RenderType additive(Identifier texture) {
        return ADDITIVE.apply(texture);
    }

    public static RenderType translucent(Identifier texture) {
        return TRANSLUCENT.apply(texture);
    }
}
