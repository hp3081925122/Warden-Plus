package com.wardenplus.client.vfx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wardenplus.client.Draw;
import com.wardenplus.client.ScreenFx;
import com.wardenplus.client.WpRenderTypes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class CollapseVfx extends VfxBase {
    private final float radius;
    private final List<double[]> streaks = new ArrayList<>();
    private Vec3 lastCore;

    public CollapseVfx(int id, float radius, int life) {
        super(id, life + 2);
        this.radius = radius;
    }

    private Vec3 core(Entity e, float partial) {
        return e.getPosition(partial).add(0, 1.9, 0);
    }

    @Override
    protected void onTick() {
        Entity e = entity();
        if (e == null) {
            return;
        }
        Vec3 core = core(e, 1);
        lastCore = core;
        float p = (float) age / life;
        int spawn = age < life - 10 ? 5 + (int) (p * 6) : 0;
        for (int i = 0; i < spawn; i++) {
            Vec3 dir = new Vec3(RANDOM.nextGaussian(), Math.abs(RANDOM.nextGaussian()) * 0.6 - 0.1, RANDOM.nextGaussian()).normalize();
            double dist = radius * (0.45 + RANDOM.nextDouble() * 0.5);
            Vec3 s = core.add(dir.scale(dist));
            streaks.add(new double[]{s.x, s.y, s.z, 0.15 + RANDOM.nextDouble() * 0.2, 0});
        }
        streaks.removeIf(s -> {
            Vec3 pos = new Vec3(s[0], s[1], s[2]);
            Vec3 to = core.subtract(pos);
            double d = to.length();
            if (d < 1.2) {
                return true;
            }
            s[3] = Math.min(2.2, s[3] * 1.12 + 0.03);
            Vec3 step = to.scale(Math.min(1, s[3] / d));
            s[0] += step.x;
            s[1] += step.y;
            s[2] += step.z;
            s[4] += 1;
            return false;
        });
        if (age % 2 == 0) {
            particle(ParticleTypes.SCULK_SOUL, core.x + RANDOM.nextGaussian() * 2, core.y + RANDOM.nextGaussian() * 2, core.z + RANDOM.nextGaussian() * 2, 0, 0, 0);
        }
        Player pl = localPlayer();
        if (pl != null && pl.position().distanceTo(e.position()) < radius) {
            ScreenFx.vignette(0.25F + 0.5F * p, 0.0F, 0.04F, 0.07F);
        }
        ScreenFx.shakeAt(e.position(), 0.25F + 1.4F * p, radius + 6);
    }

    @Override
    public void render(float partial) {
        Entity e = entity();
        if (e == null) {
            return;
        }
        float t = time(partial);
        float p = Mth.clamp(t / (life - 2), 0, 1);
        Vec3 core = core(e, partial);
        Vec3 feet = e.getPosition(partial);
        float flick = 0.6F + 0.4F * Mth.sin(t * 0.55F);
        Draw.groundRing(Draw.add(WpRenderTypes.BAND), feet.x, feet.y + 0.06, feet.z, radius - 0.6F, radius + 0.6F, 128, CYAN[0], CYAN[1], CYAN[2], (0.3F + 0.5F * p) * flick);
        Draw.flat(Draw.add(WpRenderTypes.RUNE), feet.x, feet.y + 0.05, feet.z, radius, -t * 0.01F, TEAL[0], TEAL[1], TEAL[2], 0.12F + 0.25F * p);
        Draw.flat(Draw.add(WpRenderTypes.RUNE), feet.x, feet.y + 0.055, feet.z, radius * 0.3F, t * 0.04F, CYAN[0], CYAN[1], CYAN[2], 0.3F + 0.5F * p);
        Draw.wall(Draw.add(WpRenderTypes.FADE), feet.x, feet.y, feet.z, radius, 3.0F * p + 0.5F, 128, 18, -t * 0.03F, CYAN[0], CYAN[1], CYAN[2], 0.3F * p, 0);
        VertexConsumer band = Draw.add(WpRenderTypes.BAND);
        for (double[] s : streaks) {
            Vec3 pos = new Vec3(s[0], s[1], s[2]);
            Vec3 dir = core.subtract(pos).normalize();
            Vec3 tail = pos.subtract(dir.scale(0.8 + s[3] * 1.4));
            Draw.beam(band, tail, pos, 0.02F, 0.1F, 0, 1, PALE[0], PALE[1], PALE[2], 0.0F, 0.9F);
        }
        float compress = t > life - 12 ? Math.max(0.05F, 1 - (t - (life - 12)) / 11.0F) : 1;
        float tremble = 1 + Mth.sin(t * 2.7F) * 0.04F * p;
        float rc = (0.35F + 2.0F * (float) Math.pow(p, 0.7)) * compress * tremble;
        Draw.sphere(Draw.shade(WpRenderTypes.WHITE), core, rc, 10, 20, 0, 0, 0.0F, 0.01F, 0.02F, 0.92F, 0.0F, Math.PI);
        Draw.sphere(Draw.add(WpRenderTypes.NOISE), core, rc * 1.1F, 10, 20, t * 0.03F, t * 0.05F, CYAN[0], CYAN[1], CYAN[2], 0.05F, 1.2F, Math.PI);
        VertexConsumer glow = Draw.add(WpRenderTypes.GLOW);
        Draw.billboard(glow, core.x, core.y, core.z, rc * 3.6F, t * 0.02F, TEAL[0], TEAL[1], TEAL[2], 0.65F);
        Draw.billboard(glow, core.x, core.y, core.z, rc * 1.9F, -t * 0.04F, CYAN[0], CYAN[1], CYAN[2], 0.45F + 0.4F * p);
    }
}
