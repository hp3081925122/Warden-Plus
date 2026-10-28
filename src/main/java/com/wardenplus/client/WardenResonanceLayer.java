package com.wardenplus.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wardenplus.WardenPlus;
import net.minecraft.client.model.WardenModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.warden.Warden;

public final class WardenResonanceLayer extends RenderLayer<Warden, WardenModel<Warden>> {
    private static final ResourceLocation VEINS = new ResourceLocation(WardenPlus.MOD_ID, "textures/entity/warden_resonance.png");
    private static final ResourceLocation SWIRL = new ResourceLocation(WardenPlus.MOD_ID, "textures/entity/warden_swirl.png");

    public WardenResonanceLayer(RenderLayerParent<Warden, WardenModel<Warden>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, Warden warden, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (warden.isInvisible()) {
            return;
        }
        ClientWardenState.State s = ClientWardenState.peek(warden.getId());
        if (s == null || s.variant == 0) {
            return;
        }
        if (s.variant == 2) {
            renderEnder(pose, buffers, warden, s, partialTick, ageInTicks);
            return;
        }
        float awaken = s.stage >= 2 ? 1.0F : s.awaken;
        if (awaken <= 0.01F) {
            return;
        }
        float heart = warden.getHeartAnimation(partialTick);
        float wave = 0.5F + 0.5F * Mth.sin(ageInTicks * 0.12F);
        float alpha = Mth.clamp((0.35F + 0.4F * heart + 0.2F * wave) * awaken, 0, 1);
        int color = s.stage >= 3
                ? FastColor.ARGB32.color((int) (alpha * 255), 190, 255, 255)
                : FastColor.ARGB32.color((int) (alpha * 255), 70, 230, 255);
        renderModel(pose, buffers.getBuffer(RenderType.entityTranslucentEmissive(VEINS)), color);
        if (s.stage >= 3) {
            float scroll = ageInTicks * 0.008F;
            int swirl = FastColor.ARGB32.color(255, (int) (60 + 60 * heart), (int) (150 + 80 * heart), 200);
            getParentModel().getTendrilsLayerModelParts().forEach(p -> p.skipDraw = true);
            renderModel(pose, buffers.getBuffer(RenderType.energySwirl(SWIRL, scroll % 1.0F, (scroll * 0.6F) % 1.0F)), swirl);
            getParentModel().getTendrilsLayerModelParts().forEach(p -> p.skipDraw = false);
        }
    }

    private void renderEnder(PoseStack pose, MultiBufferSource buffers, Warden warden, ClientWardenState.State s, float partialTick, float ageInTicks) {
        float heart = warden.getHeartAnimation(partialTick);
        float wave = 0.5F + 0.5F * Mth.sin(ageInTicks * 0.09F);
        float base = s.stage >= 3 ? 0.75F : s.stage >= 2 ? 0.6F : 0.42F;
        float alpha = Mth.clamp(base + 0.3F * heart + 0.15F * wave, 0, 1);
        int color = s.stage >= 3
                ? FastColor.ARGB32.color((int) (alpha * 255), 240, 190, 255)
                : FastColor.ARGB32.color((int) (alpha * 255), 185, 90, 255);
        renderModel(pose, buffers.getBuffer(RenderType.entityTranslucentEmissive(VEINS)), color);
        if (s.stage >= 2) {
            float scroll = ageInTicks * 0.006F;
            int strength = s.stage >= 3 ? 150 : 95;
            int swirl = FastColor.ARGB32.color(255, (int) ((70 + 40 * heart) * strength / 150), (int) ((20 + 20 * heart) * strength / 150), (int) ((110 + 60 * heart) * strength / 150));
            getParentModel().getTendrilsLayerModelParts().forEach(p -> p.skipDraw = true);
            renderModel(pose, buffers.getBuffer(RenderType.energySwirl(SWIRL, (scroll * 0.7F) % 1.0F, scroll % 1.0F)), swirl);
            getParentModel().getTendrilsLayerModelParts().forEach(p -> p.skipDraw = false);
        }
    }

    private void renderModel(PoseStack pose, com.mojang.blaze3d.vertex.VertexConsumer consumer, int color) {
        float alpha = FastColor.ARGB32.alpha(color) / 255.0F;
        float red = FastColor.ARGB32.red(color) / 255.0F;
        float green = FastColor.ARGB32.green(color) / 255.0F;
        float blue = FastColor.ARGB32.blue(color) / 255.0F;
        getParentModel().renderToBuffer(pose, consumer, 0xF000F0, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
    }
}
