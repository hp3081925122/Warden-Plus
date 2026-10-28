package com.wardenplus.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wardenplus.WardenPlus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.monster.warden.WardenModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.WardenRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.warden.Warden;

public final class WardenResonanceLayer extends RenderLayer<WardenRenderState, WardenModel> {
    private static final Identifier VEINS = Identifier.fromNamespaceAndPath(WardenPlus.MOD_ID, "textures/entity/warden_resonance.png");
    public WardenResonanceLayer(RenderLayerParent<WardenRenderState, WardenModel> parent) { super(parent); }
    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, int light, WardenRenderState state, float yRot, float xRot) {
        if (state.isInvisible) return;
        Warden warden = findWarden(state);
        if (warden == null) return;
        ClientWardenState.State s = ClientWardenState.peek(warden.getId());
        if (s == null || s.variant == 0) return;
        float heart = state.heartAnimation;
        float wave = 0.5F + 0.5F * Mth.sin(state.ageInTicks * 0.12F);
        float awaken = s.stage >= 2 ? 1.0F : s.awaken;
        float alpha = Mth.clamp((0.35F + 0.4F * heart + 0.2F * wave) * awaken, 0, 1);
        int color = s.variant == 2 ? ARGB.color((int)(alpha * 255), 185, 90, 255) : ARGB.color((int)(alpha * 255), 70, 230, 255);
        collector.submitModel(getParentModel(), state, pose, RenderTypes.entityTranslucentEmissive(VEINS), light, 0, color, null, state.outlineColor, null);
    }
    private static Warden findWarden(WardenRenderState state) {
        if (Minecraft.getInstance().level == null) return null;
        return Minecraft.getInstance().level.getEntitiesOfClass(Warden.class, new net.minecraft.world.phys.AABB(state.x - 1, state.y - 1, state.z - 1, state.x + 1, state.y + 3, state.z + 1)).stream().min((a,b) -> Double.compare(a.distanceToSqr(state.x,state.y,state.z), b.distanceToSqr(state.x,state.y,state.z))).orElse(null);
    }
}


