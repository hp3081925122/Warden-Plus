package com.wardenplus.client.vfx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wardenplus.client.Draw;
import com.wardenplus.client.ScreenFx;
import com.wardenplus.client.WpRenderTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class SonicLockVfx extends VfxBase {
    private final Vec3 from;
    private final Vec3 to;
    private final Vec3 dir;
    private final int delay;

    public SonicLockVfx(int id, Vec3 from, Vec3 to, int delay, int life) {
        super(id, life);
        this.from = from;
        this.to = to;
        this.dir = to.subtract(from).normalize();
        this.delay = Math.max(1, delay);
    }

    @Override
    protected void onTick() {
        if (age != delay) {
            return;
        }
        Vec3 d = to.subtract(from);
        int n = (int) d.length();
        for (int i = 1; i < n; i++) {
            Vec3 p = from.add(dir.scale(i));
            particle(ParticleTypes.SONIC_BOOM, p.x, p.y, p.z, 0, 0, 0);
            if (i % 2 == 0) {
                particle(ParticleTypes.REVERSE_PORTAL, p.x, p.y, p.z, RANDOM.nextGaussian() * 0.1, RANDOM.nextGaussian() * 0.1, RANDOM.nextGaussian() * 0.1);
            }
        }
        ScreenFx.shakeAt(from, 2.4F, 24);
        Player p = localPlayer();
        if (p != null && Draw.camera().distanceTo(from) < 32) {
            double dist = p.position().add(0, p.getBbHeight() * 0.5, 0).subtract(from).cross(dir).length();
            if (dist < 3.0) {
                ScreenFx.flash(0.6F, 0.3F, 1.0F, 0.35F);
            }
        }
    }

    @Override
    public void render(float partial) {
        float t = time(partial);
        VertexConsumer band = Draw.add(WpRenderTypes.BAND);
        VertexConsumer glow = Draw.add(WpRenderTypes.GLOW);
        if (t < delay) {
            float p = t / delay;
            float flick = 0.7F + 0.3F * Mth.sin(t * (2.0F + 4.0F * p));
            Draw.beam(band, from, to, 0.35F + 0.75F * p, -t * 0.5F, 8, VOID[0], VOID[1], VOID[2], (0.45F + 0.4F * p) * flick);
            Draw.beam(band, from, to, 0.08F + 0.15F * p, 0, 1, VOID_PALE[0], VOID_PALE[1], VOID_PALE[2], 0.9F * flick);
            double len = from.distanceTo(to);
            float travel = (t * 0.9F) % 3.0F;
            for (double s = travel; s < len; s += 3.0) {
                Vec3 c = from.add(dir.scale(s));
                float fade = (float) (1.0 - s / len);
                Draw.ring(band, c, dir, 0.45F + 0.3F * p, 0.6F + 0.35F * p, 20, 0, VOID[0], VOID[1], VOID[2], 0.0F, 0.75F * fade * flick);
            }
            Draw.billboard(glow, from.x, from.y, from.z, 0.8F + 1.4F * p, t * 0.3F, VOID[0], VOID[1], VOID[2], 0.9F);
            return;
        }
        float e = t - delay;
        float f = Math.max(0, 1 - e / Math.max(1, life - delay));
        if (f <= 0) {
            return;
        }
        Draw.beam(band, from, to, 3.2F * f, -t * 0.6F, 3, VOID[0], VOID[1], VOID[2], 0.85F * f);
        Draw.beam(band, from, to, 1.5F * f, -t * 0.9F, 2, CYAN[0], CYAN[1], CYAN[2], 0.8F * f);
        Draw.beam(band, from, to, 0.6F * f, 0, 1, VOID_PALE[0], VOID_PALE[1], VOID_PALE[2], f);
        double len = from.distanceTo(to);
        for (double s = 1.5; s < len; s += 2.5) {
            Vec3 c = from.add(dir.scale(s));
            float grow = (float) (0.6 + e * 0.25 + s * 0.02);
            Draw.ring(band, c, dir, grow * 0.7F, grow, 24, 0, VOID_PALE[0], VOID_PALE[1], VOID_PALE[2], 0.0F, 0.7F * f);
        }
        Draw.billboard(glow, from.x, from.y, from.z, 2.8F * f, 0, VOID_PALE[0], VOID_PALE[1], VOID_PALE[2], f);
    }
}
