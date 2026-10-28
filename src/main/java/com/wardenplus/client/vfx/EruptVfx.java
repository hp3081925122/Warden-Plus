package com.wardenplus.client.vfx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wardenplus.client.Draw;
import com.wardenplus.client.ScreenFx;
import com.wardenplus.client.WpRenderTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class EruptVfx extends VfxBase {
    private final Vec3 c;
    private final float radius;
    private final int delay;

    public EruptVfx(int id, Vec3 pos, float radius, int delay, int life) {
        super(id, life);
        this.c = pos;
        this.radius = radius;
        this.delay = delay;
    }

    @Override
    protected void onTick() {
        if (age < delay) {
            float p = (float) age / delay;
            ScreenFx.shakeAt(c, 0.4F + 0.8F * p, 14);
            int n = 2 + (int) (p * 8);
            for (int i = 0; i < n; i++) {
                double a = RANDOM.nextDouble() * Math.PI * 2;
                double r = RANDOM.nextDouble() * radius;
                debris(c.x + Math.cos(a) * r, c.y, c.z + Math.sin(a) * r, 0, 0.12 + RANDOM.nextDouble() * 0.15 * p, 0);
            }
            return;
        }
        if (age == delay) {
            ScreenFx.shakeAt(c, 4.5F, 28);
            Vec3 cam = localPlayer() == null ? c : localPlayer().position();
            if (cam.distanceTo(c) < 8) {
                ScreenFx.flash(0.7F, 1.0F, 1.0F, 0.35F);
            }
            for (int i = 0; i < 70; i++) {
                double a = RANDOM.nextDouble() * Math.PI * 2;
                double sp = 0.15 + RANDOM.nextDouble() * 0.45;
                debris(c.x + Math.cos(a) * 0.8, c.y, c.z + Math.sin(a) * 0.8, Math.cos(a) * sp, 0.6 + RANDOM.nextDouble() * 0.8, Math.sin(a) * sp);
            }
            for (int i = 0; i < 24; i++) {
                particle(ParticleTypes.SCULK_SOUL, c.x + RANDOM.nextGaussian() * 0.8, c.y + RANDOM.nextDouble() * 2, c.z + RANDOM.nextGaussian() * 0.8, RANDOM.nextGaussian() * 0.05, 0.2 + RANDOM.nextDouble() * 0.2, RANDOM.nextGaussian() * 0.05);
            }
        }
    }

    @Override
    public void render(float partial) {
        float t = time(partial);
        if (t < delay) {
            float p = t / delay;
            float flick = 0.75F + 0.25F * Mth.sin(t * (1.0F + p * 2.5F));
            Draw.flat(Draw.add(WpRenderTypes.RUNE), c.x, c.y + 0.04, c.z, radius * (1.15F - 0.15F * p), -t * 0.13F, CYAN[0], CYAN[1], CYAN[2], (0.45F + 0.55F * p) * flick);
            Draw.flat(Draw.shade(WpRenderTypes.CRACK), c.x, c.y + 0.03, c.z, radius * p, 0.7F, DEEP[0], DEEP[1], DEEP[2], 0.8F * p);
            Draw.flat(Draw.add(WpRenderTypes.CRACK), c.x, c.y + 0.045, c.z, radius * p, 0.7F, CYAN[0], CYAN[1], CYAN[2], 0.9F * p);
            Draw.groundRing(Draw.add(WpRenderTypes.BAND), c.x, c.y + 0.05, c.z, radius - 0.3F, radius + 0.3F, 48, PALE[0], PALE[1], PALE[2], 0.6F * flick);
            return;
        }
        float e = t - delay;
        float f = Math.max(0, 1 - e / 22.0F);
        if (f <= 0) {
            return;
        }
        float h = 16.0F * ease(e / 4.0F);
        VertexConsumer wall = Draw.add(WpRenderTypes.FADE);
        Draw.wall(wall, c.x, c.y, c.z, radius * 0.55F * f, h, 28, 3, -t * 0.08F, CYAN[0], CYAN[1], CYAN[2], 0.95F * f, 0.0F);
        Draw.wall(wall, c.x, c.y, c.z, radius * 0.25F * f, h * 0.8F, 20, 2, t * 0.1F, PALE[0], PALE[1], PALE[2], 1.0F * f, 0.0F);
        VertexConsumer glow = Draw.add(WpRenderTypes.GLOW);
        Draw.billboard(glow, c.x, c.y + 1.0, c.z, radius * 1.6F * f, 0, CYAN[0], CYAN[1], CYAN[2], 0.8F * f);
        Draw.flat(glow, c.x, c.y + 0.05, c.z, radius * 1.8F, 0, TEAL[0], TEAL[1], TEAL[2], 0.6F * f);
    }
}




