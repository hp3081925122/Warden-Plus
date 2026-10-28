package com.wardenplus.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class ScreenFx {
    private static final RandomSource RANDOM = RandomSource.create();
    private static float shake, shakeDecay, flash, flashR, flashG, flashB, vignette, vignetteTarget, pulse;
    private static float vignetteR = .05F, vignetteG = .3F, vignetteB = .4F;
    private ScreenFx() {}
    public static void shakeAt(Vec3 pos,float strength,float range){Player p=Minecraft.getInstance().player;if(p==null)return;double d=p.position().distanceTo(pos);if(d>range)return;float s=strength*(float)(1-d/range);if(s>shake){shake=s;shakeDecay=Math.max(.05F,s/14);}}
    public static void flash(float r,float g,float b,float a){if(a>flash){flash=a;flashR=r;flashG=g;flashB=b;}}
    public static void vignette(float a,float r,float g,float b){vignetteTarget=Math.max(vignetteTarget,a);vignetteR=r;vignetteG=g;vignetteB=b;}
    public static void pulse(float a){pulse=Math.max(pulse,a);}
    public static void tick(){shake=Math.max(0,shake-shakeDecay);flash=Math.max(0,flash-.06F);pulse=Math.max(0,pulse-.08F);vignette+=(vignetteTarget-vignette)*.2F;vignetteTarget=0;}
    public static float shakePitch(){return shake<=0?0:(RANDOM.nextFloat()-.5F)*shake*2;}
    public static float shakeYaw(){return shake<=0?0:(RANDOM.nextFloat()-.5F)*shake*2;}
    public static float shakeRoll(){return shake<=0?0:(RANDOM.nextFloat()-.5F)*shake*1.2F;}
    public static void render(GuiGraphicsExtractor g, DeltaTracker delta){int w=g.guiWidth(),h=g.guiHeight();float v=Mth.clamp(vignette+pulse,0,1);if(v>.01F)g.fill(0,0,w,h,((int)(v*120)<<24)|((int)(vignetteR*255)<<16)|((int)(vignetteG*255)<<8)|(int)(vignetteB*255));if(flash>.01F)g.fill(0,0,w,h,((int)(Mth.clamp(flash,0,1)*255)<<24)|((int)(flashR*255)<<16)|((int)(flashG*255)<<8)|(int)(flashB*255));}
}


