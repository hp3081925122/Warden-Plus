package com.wardenplus.client.vfx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wardenplus.client.Draw;
import com.wardenplus.client.ScreenFx;
import com.wardenplus.client.WpRenderTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class BeamVfx extends VfxBase {
    private final Vec3 from;
    private final Vec3 to;
    private final int delay;

    public BeamVfx(int id, Vec3 from, Vec3 to, int delay, int life) {
        super(id, life);
        this.from = from;
        this.to = to;
        this.delay = delay;
    }

    @Override
    protected void onTick() {
        if (age != delay) {
            return;
        }
        ScreenFx.shakeAt(to, 1.8F, 10);
        Vec3 d = to.subtract(from);
        int n = (int) d.length();
        for (int i = 0; i < n; i++) {
            Vec3 p = from.add(d.scale((double) i / Math.max(1, n)));
            particle(ParticleTypes.SCULK_CHARGE_POP, p.x + RANDOM.nextGaussian() * 0.15, p.y + RANDOM.nextGaussian() * 0.15, p.z + RANDOM.nextGaussian() * 0.15, 0, 0.02, 0);
        }
        for (int i = 0; i < 16; i++) {
            debris(to.x, to.y + 0.2, to.z, RANDOM.nextGaussian() * 0.2, 0.2 + RANDOM.nextDouble() * 0.3, RANDOM.nextGaussian() * 0.2);
            particle(ParticleTypes.END_ROD, to.x, to.y, to.z, RANDOM.nextGaussian() * 0.15, RANDOM.nextGaussian() * 0.15, RANDOM.nextGaussian() * 0.15);
        }
    }

    @Override
    public void render(float partial) {
        float t = time(partial);
        VertexConsumer band = Draw.add(WpRenderTypes.BAND);
        if (t < delay) {
            float p = t / delay;
            float flick = 0.6F + 0.4F * Mth.sin(t * (1.5F + 3 * p));
            Draw.beam(band, from, to, 0.05F + 0.08F * p, 0, 1, CYAN[0], CYAN[1], CYAN[2], (0.25F + 0.5F * p) * flick);
            Draw.billboard(Draw.add(WpRenderTypes.GLOW), to.x, to.y, to.z, 0.4F + 0.7F * p, t * 0.2F, CYAN[0], CYAN[1], CYAN[2], 0.5F + 0.4F * p);
            return;
        }
        float e = t - delay;
        float f = Math.max(0, 1 - e / (life - delay));
        if (f <= 0) {
            return;
        }
        float w = 1.1F * f;
        Draw.beam(band, from, to, w * 1.9F, -t * 0.3F, 2, CYAN[0], CYAN[1], CYAN[2], 0.75F * f);
        Draw.beam(band, from, to, w * 0.55F, 0, 1, PALE[0], PALE[1], PALE[2], 1.0F);
        VertexConsumer glow = Draw.add(WpRenderTypes.GLOW);
        Draw.billboard(glow, to.x, to.y, to.z, 2.6F * f, e * 0.3F, CYAN[0], CYAN[1], CYAN[2], 0.9F * f);
        Draw.billboard(glow, from.x, from.y, from.z, 1.6F * f, 0, PALE[0], PALE[1], PALE[2], 0.9F * f);
    }
}
