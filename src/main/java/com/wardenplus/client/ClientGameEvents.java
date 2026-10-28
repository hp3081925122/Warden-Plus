package com.wardenplus.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wardenplus.WardenPlus;
import com.wardenplus.client.vfx.VfxManager;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.util.Mth;

@EventBusSubscriber(modid = WardenPlus.MOD_ID, value = Dist.CLIENT)
public final class ClientGameEvents {
    private ClientGameEvents() {
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.isPaused()) {
            return;
        }
        VfxManager.tick();
        ScreenFx.tick();
        ClientDarkness.tick();
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || VfxManager.isEmpty()) {
            return;
        }
        PoseStack pose = event.getPoseStack();
        if (pose == null) {
            pose = new PoseStack();
        }
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Draw.begin(pose.last(), event.getCamera(), partial);
        VfxManager.render(partial);
        Draw.end();
    }

    @SubscribeEvent
    public static void onCamera(ViewportEvent.ComputeCameraAngles event) {
        event.setPitch(event.getPitch() + ScreenFx.shakePitch());
        event.setYaw(event.getYaw() + ScreenFx.shakeYaw());
        event.setRoll(event.getRoll() + ScreenFx.shakeRoll());
    }

    @SubscribeEvent
    public static void onFog(ViewportEvent.RenderFog event) {
        float f = ClientDarkness.factor((float) event.getPartialTick());
        if (f <= 0.0F) {
            return;
        }
        float target = Math.max(0.05F, ClientDarkness.visibility());
        float far = Mth.lerp(f, event.getFarPlaneDistance(), target);
        if (far >= event.getFarPlaneDistance()) {
            return;
        }
        float near = event.getMode() == FogRenderer.FogMode.FOG_SKY || target < 1.0F ? 0.0F : far * 0.75F;
        event.setFarPlaneDistance(far);
        event.setNearPlaneDistance(Math.min(far, Mth.lerp(f, event.getNearPlaneDistance(), near)));
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        float f = ClientDarkness.factor((float) event.getPartialTick());
        if (f <= 0.0F) {
            return;
        }
        float k = (1.0F - f) * (1.0F - f);
        event.setRed(event.getRed() * k);
        event.setGreen(event.getGreen() * k);
        event.setBlue(event.getBlue() * k);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        VfxManager.clear();
        ClientWardenState.clear();
        ClientDarkness.clear();
    }
}
