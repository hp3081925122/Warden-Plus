package com.wardenplus.client;

import net.minecraft.client.renderer.fog.FogData;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wardenplus.WardenPlus;
import com.wardenplus.client.vfx.VfxManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid=WardenPlus.MOD_ID,value=Dist.CLIENT)
public final class ClientGameEvents {
 private ClientGameEvents(){}
 @SubscribeEvent public static void onTick(ClientTickEvent.Post e){Minecraft mc=Minecraft.getInstance();if(mc.level==null||mc.isPaused())return;VfxManager.tick();ScreenFx.tick();ClientDarkness.tick();}
 @SubscribeEvent public static void onRender(RenderLevelStageEvent.AfterTranslucentParticles e){if(VfxManager.isEmpty())return;PoseStack pose=e.getPoseStack();Draw.begin(pose.last(),Minecraft.getInstance().gameRenderer.getMainCamera(),0);VfxManager.render(0);Draw.end();}
 @SubscribeEvent public static void onCamera(ViewportEvent.ComputeCameraAngles e){e.setPitch(e.getPitch()+ScreenFx.shakePitch());e.setYaw(e.getYaw()+ScreenFx.shakeYaw());e.setRoll(e.getRoll()+ScreenFx.shakeRoll());}
 @SubscribeEvent public static void onFog(ViewportEvent.RenderFog e){float f=ClientDarkness.factor((float)e.getPartialTick());if(f<=0)return;float target=Math.max(.05F,ClientDarkness.visibility()*(1-.5F*ClientDarkness.pulse((float)e.getPartialTick())));float far=Mth.lerp(f,e.getFarPlaneDistance(),target);if(far>=e.getFarPlaneDistance())return;float near=target<1?0:far*.3F;e.setFarPlaneDistance(far);e.setNearPlaneDistance(Math.min(far,Mth.lerp(f,e.getNearPlaneDistance(),near)));FogData d=e.getFogData();d.skyEnd=Math.min(d.skyEnd,far);d.cloudEnd=Math.min(d.cloudEnd,far);}
 @SubscribeEvent public static void onFogColor(ViewportEvent.ComputeFogColor e){float f=ClientDarkness.factor((float)e.getPartialTick());if(f<=0)return;float k=(1-f)*(1-f);e.setRed(e.getRed()*k);e.setGreen(e.getGreen()*k);e.setBlue(e.getBlue()*k);}
 @SubscribeEvent public static void onLogout(ClientPlayerNetworkEvent.LoggingOut e){VfxManager.clear();ClientWardenState.clear();ClientDarkness.clear();}
}
