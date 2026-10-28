package com.wardenplus.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class ScreenFx {
    private static final RandomSource RANDOM = RandomSource.create();
    private static float shake;
    private static float shakeDecay;
    private static float flash;
    private static float flashR;
    private static float flashG;
    private static float flashB;
    private static float vignette;
    private static float vignetteTarget;
    private static float vignetteR = 0.05F;
    private static float vignetteG = 0.3F;
    private static float vignetteB = 0.4F;
    private static float pulse;

    private ScreenFx() {
    }

    public static void shakeAt(Vec3 pos, float strength, float range) {
        Player p = Minecraft.getInstance().player;
        if (p == null) {
            return;
        }
        double d = p.position().distanceTo(pos);
        if (d > range) {
            return;
        }
        float s = strength * (float) (1.0 - d / range);
        if (s > shake) {
            shake = s;
            shakeDecay = Math.max(0.05F, s / 14.0F);
        }
    }

    public static void flash(float r, float g, float b, float amount) {
        if (amount > flash) {
            flash = amount;
            flashR = r;
            flashG = g;
            flashB = b;
        }
    }

    public static void vignette(float amount, float r, float g, float b) {
        vignetteTarget = Math.max(vignetteTarget, amount);
        vignetteR = r;
        vignetteG = g;
        vignetteB = b;
    }

    public static void pulse(float amount) {
        pulse = Math.max(pulse, amount);
    }

    public static void tick() {
        shake = Math.max(0, shake - shakeDecay);
        flash = Math.max(0, flash - 0.06F);
        pulse = Math.max(0, pulse - 0.08F);
        vignette += (vignetteTarget - vignette) * 0.2F;
        vignetteTarget = 0;
    }

    public static float shakePitch() {
        return shake <= 0 ? 0 : (RANDOM.nextFloat() - 0.5F) * shake * 2.0F;
    }

    public static float shakeYaw() {
        return shake <= 0 ? 0 : (RANDOM.nextFloat() - 0.5F) * shake * 2.0F;
    }

    public static float shakeRoll() {
        return shake <= 0 ? 0 : (RANDOM.nextFloat() - 0.5F) * shake * 1.2F;
    }

    public static void render(ForgeGui gui, GuiGraphics g, float partialTick, int width, int height) {
        int w = width;
        int h = height;
        float v = Mth.clamp(vignette + pulse, 0, 1);
        if (v > 0.01F) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            g.setColor(vignetteR, vignetteG, vignetteB, v);
            g.blit(WpRenderTypes.VIGNETTE, 0, 0, 0, 0, w, h, w, h);
            g.setColor(1, 1, 1, 1);
            RenderSystem.disableBlend();
        }
        if (flash > 0.01F) {
            int a = (int) (Mth.clamp(flash, 0, 1) * 255) << 24;
            int rgb = ((int) (flashR * 255) << 16) | ((int) (flashG * 255) << 8) | (int) (flashB * 255);
            g.fill(0, 0, w, h, a | rgb);
        }
    }
}
