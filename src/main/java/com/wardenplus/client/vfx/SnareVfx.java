package com.wardenplus.client.vfx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wardenplus.client.Draw;
import com.wardenplus.client.ScreenFx;
import com.wardenplus.client.WpRenderTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class SnareVfx extends VfxBase {
    private static final int TENDRILS = 4;
    private static final int SEGMENTS = 12;
    private final Vec3 c;
    private final int delay;
    private final float seed;

    public SnareVfx(int id, Vec3 pos, int delay, int life) {
        super(id, life);
        this.c = pos;
        this.delay = delay;
        this.seed = RANDOM.nextFloat() * Mth.TWO_PI;
    }

    @Override
    protected void onTick() {
        if (age < delay) {
            if (RANDOM.nextInt(3) == 0) {
                double a = RANDOM.nextDouble() * Math.PI * 2;
                double r = RANDOM.nextDouble() * 1.4;
                particle(ParticleTypes.SCULK_CHARGE_POP, c.x + Math.cos(a) * r, c.y + 0.1, c.z + Math.sin(a) * r, 0, 0.03, 0);
            }
            return;
        }
        if (age == delay) {
            ScreenFx.shakeAt(c, 1.2F, 12);
            for (int i = 0; i < 26; i++) {
                double a = RANDOM.nextDouble() * Math.PI * 2;
                debris(c.x + Math.cos(a) * 0.6, c.y, c.z + Math.sin(a) * 0.6, Math.cos(a) * 0.18, 0.4 + RANDOM.nextDouble() * 0.4, Math.sin(a) * 0.18);
            }
            for (int i = 0; i < 8; i++) {
                particle(ParticleTypes.SCULK_SOUL, c.x + RANDOM.nextGaussian() * 0.5, c.y + 0.3, c.z + RANDOM.nextGaussian() * 0.5, 0, 0.12 + RANDOM.nextDouble() * 0.1, 0);
            }
        }
    }

    @Override
    public void render(float partial) {
        float t = time(partial);
        if (t < delay) {
            float p = t / delay;
            float flick = p > 0.7F ? 0.7F + 0.3F * Mth.sin(t * 2.4F) : 1.0F;
            Draw.flat(Draw.add(WpRenderTypes.RUNE), c.x, c.y + 0.04, c.z, 1.7F - 0.25F * p, seed + t * 0.09F, CYAN[0], CYAN[1], CYAN[2], (0.35F + 0.6F * p) * flick);
            Draw.flat(Draw.add(WpRenderTypes.GLOW), c.x, c.y + 0.035, c.z, 1.5F, 0, TEAL[0], TEAL[1], TEAL[2], 0.65F * p);
            Draw.flat(Draw.shade(WpRenderTypes.CRACK), c.x, c.y + 0.03, c.z, 1.2F * p, seed, DEEP[0], DEEP[1], DEEP[2], 0.7F * p);
            return;
        }
        float e = t - delay;
        float grow = ease(e / 5.0F);
        float retract = e > 24 ? Math.max(0, 1 - (e - 24) / 10.0F) : 1;
        float g = grow * retract;
        if (g <= 0.01F) {
            return;
        }
        VertexConsumer solid = Draw.solid();
        for (int k = 0; k < TENDRILS; k++) {
            float baseA = seed + k * Mth.TWO_PI / TENDRILS;
            float h = 3.4F * g * (0.85F + 0.3F * ((k * 7) % 3) / 2F);
            Vec3[] pts = new Vec3[SEGMENTS + 1];
            float[] radii = new float[SEGMENTS + 1];
            for (int i = 0; i <= SEGMENTS; i++) {
                float s = (float) i / SEGMENTS;
                float swirl = baseA + s * 1.9F + Mth.sin(t * 0.18F + k * 1.3F + s * 3) * 0.35F;
                float out = 0.35F + Mth.sin(s * Mth.PI * 0.85F) * 0.75F * (1 - s * 0.4F);
                pts[i] = new Vec3(c.x + Mth.cos(swirl) * out, c.y - 0.3 + s * h, c.z + Mth.sin(swirl) * out);
                radii[i] = (0.3F * (1 - s) + 0.035F) * (0.6F + 0.4F * g);
            }
            Draw.tube(solid, pts, radii, DEEP[0] * 1.4F, DEEP[1] * 1.6F, DEEP[2] * 1.7F, 0.95F);
            Vec3 tip = pts[SEGMENTS];
            Draw.billboard(Draw.add(WpRenderTypes.GLOW), tip.x, tip.y, tip.z, 0.45F * g, 0, CYAN[0], CYAN[1], CYAN[2], 0.9F * g);
        }
        Draw.flat(Draw.add(WpRenderTypes.GLOW), c.x, c.y + 0.04, c.z, 2.1F, 0, CYAN[0], CYAN[1], CYAN[2], 0.55F * g);
        Draw.flat(Draw.add(WpRenderTypes.CRACK), c.x, c.y + 0.045, c.z, 1.8F, seed, CYAN[0], CYAN[1], CYAN[2], 0.8F * g);
        Draw.flat(Draw.shade(WpRenderTypes.CRACK), c.x, c.y + 0.03, c.z, 1.8F, seed, DEEP[0], DEEP[1], DEEP[2], 0.8F * retract);
    }
}
