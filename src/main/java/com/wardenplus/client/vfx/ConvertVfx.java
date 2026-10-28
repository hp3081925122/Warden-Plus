package com.wardenplus.client.vfx;

import com.wardenplus.client.Draw;
import com.wardenplus.client.ScreenFx;
import com.wardenplus.client.WpRenderTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class ConvertVfx extends VfxBase {
    private final int burst;
    private final boolean ender;
    private final float[][] pal;

    public ConvertVfx(int id, int palette, int burst, int life) {
        super(id, life);
        this.burst = burst;
        this.ender = palette == 1;
        this.pal = palette(palette);
    }

    @Override
    protected void onTick() {
        Entity e = entity();
        if (e == null) {
            return;
        }
        Vec3 base = e.position();
        if (age < burst) {
            for (int i = 0; i < 5; i++) {
                double a = RANDOM.nextDouble() * Math.PI * 2;
                double r = 3.5 + RANDOM.nextDouble() * 2.5;
                Vec3 start = base.add(Math.cos(a) * r, RANDOM.nextDouble() * 3.5, Math.sin(a) * r);
                Vec3 vel = base.add(0, 1.8, 0).subtract(start).scale(0.08);
                particle(ender ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.SCULK_SOUL, start.x, start.y, start.z, vel.x, vel.y, vel.z);
            }
            if (ender) {
                particle(ParticleTypes.PORTAL, base.x, base.y + 1.5, base.z, RANDOM.nextGaussian() * 2, RANDOM.nextGaussian() * 2, RANDOM.nextGaussian() * 2);
            }
            ScreenFx.shakeAt(base, 0.4F * age / burst, 24);
        } else if (age == burst) {
            ScreenFx.shakeAt(base, 4.0F, 32);
            if (localPlayer() != null && localPlayer().distanceTo(e) < 20) {
                ScreenFx.flash(pal[0][0], pal[0][1], pal[0][2], 0.45F);
            }
            for (int i = 0; i < 50; i++) {
                Vec3 d = new Vec3(RANDOM.nextGaussian(), Math.abs(RANDOM.nextGaussian()), RANDOM.nextGaussian()).normalize().scale(0.6);
                particle(ender ? PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0F) : ParticleTypes.END_ROD, base.x, base.y + 1.8, base.z, d.x, d.y * 0.6, d.z);
            }
        }
    }

    @Override
    public void render(float partial) {
        Entity e = entity();
        if (e == null) {
            return;
        }
        float t = time(partial);
        Vec3 base = e.getPosition(partial);
        float rise = ease(t / burst);
        float out = Mth.clamp((life - t) / 14.0F, 0, 1);
        float pop = t > burst ? Math.max(0, 1 - (t - burst) / 12.0F) : 0;
        float env = rise * out;
        Draw.wall(Draw.add(WpRenderTypes.FADE), base.x, base.y, base.z, 1.2F + 2.0F * pop, 18 * env, 28, 3, -t * 0.05F, pal[0][0], pal[0][1], pal[0][2], 0.7F * env, 0);
        Draw.wall(Draw.add(WpRenderTypes.FADE), base.x, base.y, base.z, 0.6F + 0.8F * pop, 24 * env, 18, 2, t * 0.08F, pal[1][0], pal[1][1], pal[1][2], 0.85F * env, 0);
        Draw.flat(Draw.add(WpRenderTypes.RUNE), base.x, base.y + 0.05, base.z, 3.0F + 3.5F * rise + 4.0F * pop, t * 0.03F, pal[0][0], pal[0][1], pal[0][2], 0.8F * out);
        Draw.flat(Draw.add(WpRenderTypes.GLOW), base.x, base.y + 0.04, base.z, 5.0F + 3.0F * pop, 0, pal[2][0], pal[2][1], pal[2][2], 0.6F * env);
        if (pop > 0) {
            Draw.groundRing(Draw.add(WpRenderTypes.BAND), base.x, base.y + 0.06, base.z, 9 * (1 - pop) , 9 * (1 - pop) + 1.4F, 64, pal[1][0], pal[1][1], pal[1][2], pop);
        }
        float heart = 0.8F + 0.2F * Mth.sin(t * 0.9F);
        Draw.billboard(Draw.add(WpRenderTypes.GLOW), base.x, base.y + 1.8, base.z, (1.2F + 3.5F * pop) * heart * env + 0.2F, 0, pal[0][0], pal[0][1], pal[0][2], env);
    }
}






