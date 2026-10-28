package com.wardenplus.client.vfx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wardenplus.client.Draw;
import com.wardenplus.client.ScreenFx;
import com.wardenplus.client.WpRenderTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

public final class ShockRingVfx extends VfxBase {
    private final Vec3 c;
    private final float speed;
    private final float maxR;
    private final int delay;
    private final float power;
    private final float[] main;
    private final float[] pale;
    private final float[] dim;
    private final boolean ender;

    public ShockRingVfx(int id, Vec3 center, float speed, float maxR, int delay, float power, int palette) {
        super(id, delay + (int) Math.ceil(maxR / speed) + 8);
        this.c = center;
        this.speed = speed;
        this.maxR = maxR;
        this.delay = delay;
        this.power = power;
        float[][] p = palette(palette);
        this.main = p[0];
        this.pale = p[1];
        this.dim = p[2];
        this.ender = palette == 1;
    }

    private float radius(float t) {
        return Math.min(maxR, (t - delay) * speed);
    }

    @Override
    protected void onTick() {
        if (age < delay) {
            return;
        }
        if (age == delay) {
            ScreenFx.shakeAt(c, 2.2F * power, 26);
            for (int i = 0; i < 24; i++) {
                double a = RANDOM.nextDouble() * Math.PI * 2;
                debris(c.x + Math.cos(a) * 0.8, c.y, c.z + Math.sin(a) * 0.8, Math.cos(a) * 0.25, 0.35 + RANDOM.nextDouble() * 0.35, Math.sin(a) * 0.25);
            }
        }
        float r = radius(age);
        if (r >= maxR) {
            return;
        }
        int n = Math.min(18, 4 + (int) (r * 1.2F));
        for (int i = 0; i < n; i++) {
            double a = RANDOM.nextDouble() * Math.PI * 2;
            double x = c.x + Math.cos(a) * r;
            double z = c.z + Math.sin(a) * r;
            debris(x, c.y, z, Math.cos(a) * 0.08, 0.18 + RANDOM.nextDouble() * 0.22, Math.sin(a) * 0.08);
            if (RANDOM.nextInt(4) == 0) {
                particle(ender ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.SCULK_CHARGE_POP, x, c.y + 0.2, z, 0, 0.04, 0);
            }
        }
    }

    @Override
    public void render(float partial) {
        float t = time(partial);
        if (t < delay) {
            return;
        }
        float r = radius(t);
        float fade = 1.0F - r / maxR;
        fade = Math.max(0, fade) * Math.min(1, (t - delay) / 2.0F);
        if (fade <= 0.01F) {
            return;
        }
        double y = c.y + 0.06;
        VertexConsumer band = Draw.add(WpRenderTypes.BAND);
        Draw.groundRing(band, c.x, y, c.z, r - 1.1F * power, r + 0.5F, 80, main[0], main[1], main[2], 0.95F * fade);
        Draw.groundRing(band, c.x, y + 0.01, c.z, r - 0.35F, r + 0.25F, 80, pale[0], pale[1], pale[2], 0.9F * fade);
        Draw.groundRing(band, c.x, y - 0.01, c.z, r - 3.2F, r - 1.0F, 80, dim[0], dim[1], dim[2], 0.35F * fade);
        VertexConsumer wall = Draw.add(WpRenderTypes.FADE);
        Draw.wall(wall, c.x, c.y, c.z, r, 0.9F + 1.3F * power * fade, 80, 7, t * 0.03F, main[0], main[1], main[2], 0.75F * fade, 0.0F);
        Draw.wall(wall, c.x, c.y, c.z, r * 0.985F, 0.5F + 0.6F * power * fade, 80, 11, -t * 0.05F, pale[0], pale[1], pale[2], 0.6F * fade, 0.0F);
    }
}




