package com.wardenplus.client.vfx;

import com.wardenplus.client.Draw;
import com.wardenplus.client.ScreenFx;
import com.wardenplus.client.WpRenderTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class BlastVfx extends VfxBase {
    private final Vec3 c;
    private final float radius;

    public BlastVfx(int id, Vec3 center, float radius, int life) {
        super(id, life);
        this.c = center;
        this.radius = radius;
    }

    @Override
    protected void onTick() {
        if (age != 0) {
            return;
        }
        Player p = localPlayer();
        boolean near = p != null && p.position().distanceTo(c) < radius * 1.4;
        ScreenFx.flash(0.8F, 1.0F, 1.0F, near ? 0.85F : 0.3F);
        ScreenFx.shakeAt(c, 7.0F, radius * 2.2F);
        for (int i = 0; i < 90; i++) {
            Vec3 d = new Vec3(RANDOM.nextGaussian(), RANDOM.nextGaussian() * 0.5, RANDOM.nextGaussian()).normalize().scale(0.6 + RANDOM.nextDouble() * 0.9);
            particle(i % 3 == 0 ? ParticleTypes.END_ROD : ParticleTypes.SCULK_SOUL, c.x, c.y, c.z, d.x, d.y, d.z);
        }
        for (int i = 0; i < 60; i++) {
            double a = RANDOM.nextDouble() * Math.PI * 2;
            double r = RANDOM.nextDouble() * 6;
            debris(c.x + Math.cos(a) * r, c.y - 1.9, c.z + Math.sin(a) * r, Math.cos(a) * 0.3, 0.5 + RANDOM.nextDouble() * 0.6, Math.sin(a) * 0.3);
        }
    }

    @Override
    public void render(float partial) {
        float t = time(partial);
        float f = Math.max(0, 1 - t / life);
        float r = radius * ease(t / 11.0F);
        Draw.sphere(Draw.add(WpRenderTypes.NOISE), c, Math.max(0.5F, r), 18, 40, t * 0.02F, -t * 0.04F, PALE[0], PALE[1], PALE[2], 0.12F * f, 1.1F * f, Math.PI);
        Draw.sphere(Draw.add(WpRenderTypes.WHITE), c, Math.max(0.4F, r * 0.92F), 12, 28, 0, 0, CYAN[0], CYAN[1], CYAN[2], 0.0F, 0.7F * f, Math.PI);
        double gy = c.y - 1.9;
        Draw.groundRing(Draw.add(WpRenderTypes.BAND), c.x, gy + 0.07, c.z, r - 1.6F, r + 0.8F, 128, PALE[0], PALE[1], PALE[2], f);
        Draw.wall(Draw.add(WpRenderTypes.FADE), c.x, gy, c.z, r, 4.0F * f + 0.5F, 128, 16, t * 0.05F, CYAN[0], CYAN[1], CYAN[2], 0.8F * f, 0);
        Draw.billboard(Draw.add(WpRenderTypes.GLOW), c.x, c.y, c.z, 9.0F * f + 1, 0, PALE[0], PALE[1], PALE[2], f);
        Draw.flat(Draw.add(WpRenderTypes.CRACK), c.x, gy + 0.04, c.z, 9.0F, 1.3F, CYAN[0], CYAN[1], CYAN[2], f);
    }
}
