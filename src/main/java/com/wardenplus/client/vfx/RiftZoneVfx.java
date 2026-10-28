package com.wardenplus.client.vfx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wardenplus.client.Draw;
import com.wardenplus.client.WpRenderTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class RiftZoneVfx extends VfxBase {
    private static final int FADE = 6;
    private final Vec3 c;
    private final float radius;
    private final boolean warning;
    private final int delay;

    public RiftZoneVfx(int id, Vec3 center, float radius, int mode, int delay, int life) {
        super(id, life);
        this.c = center;
        this.radius = radius;
        this.warning = mode == 1;
        this.delay = Math.max(1, delay);
    }

    @Override
    protected void onTick() {
        if (warning) {
            if (age < delay && age % 2 == 0) {
                double a = RANDOM.nextDouble() * Math.PI * 2;
                double r = radius * Math.sqrt(RANDOM.nextDouble());
                particle(ParticleTypes.REVERSE_PORTAL, c.x + Math.cos(a) * r, c.y + 0.1, c.z + Math.sin(a) * r, 0, 0.15, 0);
            }
            return;
        }
        for (int i = 0; i < 2; i++) {
            double a = RANDOM.nextDouble() * Math.PI * 2;
            particle(ParticleTypes.PORTAL, c.x + Math.cos(a) * radius, c.y + 0.2 + RANDOM.nextDouble() * 2.0, c.z + Math.sin(a) * radius, 0, 0.05, 0);
        }
    }

    @Override
    public void render(float partial) {
        float t = time(partial);
        if (warning) {
            renderWarning(t);
        } else {
            renderBorder(t);
        }
    }

    private void renderBorder(float t) {
        float in = ease(t / FADE);
        float out = Mth.clamp((life - t) / FADE, 0, 1);
        float a = in * out;
        if (a <= 0.01F) {
            return;
        }
        float pulse = 0.75F + 0.25F * Mth.sin(t * 0.35F);
        double y = c.y + 0.04;
        Draw.groundRing(Draw.add(WpRenderTypes.BAND), c.x, y, c.z, radius - 0.35, radius + 0.1, 64, VOID[0], VOID[1], VOID[2], 0.9F * a * pulse);
        Draw.groundRing(Draw.add(WpRenderTypes.BAND), c.x, y + 0.01, c.z, radius - 0.08, radius + 0.04, 64, VOID_PALE[0], VOID_PALE[1], VOID_PALE[2], a);
        Draw.wall(Draw.add(WpRenderTypes.FADE), c.x, c.y, c.z, radius, 3.2 * in, 48, 6, t * 0.04F, VOID[0], VOID[1], VOID[2], 0.55F * a * pulse, 0);
        Draw.flat(Draw.add(WpRenderTypes.RUNE), c.x, y - 0.01, c.z, radius * 0.95F, t * 0.02F, VOID_DIM[0], VOID_DIM[1], VOID_DIM[2], 0.35F * a);
    }

    private void renderWarning(float t) {
        double y = c.y + 0.06;
        if (t < delay) {
            float p = t / delay;
            float flick = 0.65F + 0.35F * Mth.sin(t * (1.0F + 3.5F * p));
            float fill = radius * ease(p);
            Draw.flat(Draw.add(WpRenderTypes.GLOW), c.x, y, c.z, fill, 0, VOID[0], VOID[1], VOID[2], (0.25F + 0.45F * p) * flick);
            Draw.groundRing(Draw.add(WpRenderTypes.BAND), c.x, y + 0.01, c.z, radius - 0.25, radius + 0.05, 56, VOID_PALE[0], VOID_PALE[1], VOID_PALE[2], (0.5F + 0.5F * p) * flick);
            Draw.flat(Draw.add(WpRenderTypes.RUNE), c.x, y + 0.02, c.z, radius, -t * 0.08F, VOID[0], VOID[1], VOID[2], 0.6F * p * flick);
            return;
        }
        float f = Mth.clamp(1 - (t - delay) / Math.max(1, life - delay), 0, 1);
        if (f <= 0) {
            return;
        }
        VertexConsumer glow = Draw.add(WpRenderTypes.GLOW);
        Draw.flat(glow, c.x, y, c.z, radius * (1.0F + 0.4F * (1 - f)), 0, VOID_PALE[0], VOID_PALE[1], VOID_PALE[2], 0.9F * f);
    }
}




