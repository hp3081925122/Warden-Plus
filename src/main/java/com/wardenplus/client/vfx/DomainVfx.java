package com.wardenplus.client.vfx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wardenplus.client.Draw;
import com.wardenplus.client.ScreenFx;
import com.wardenplus.client.WpRenderTypes;
import net.minecraft.core.particles.ShriekParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class DomainVfx extends VfxBase {
    private final Vec3 c;
    private final float radius;
    private final int interval;
    private final int beats;
    private final int form;

    public DomainVfx(int id, Vec3 center, float radius, int interval, int beats, int form, int life) {
        super(id, life);
        this.c = center;
        this.radius = radius;
        this.interval = interval;
        this.beats = beats;
        this.form = form;
    }

    private float beatTime(int k) {
        return form + (k + 1) * interval - 1;
    }

    private float fade(float t) {
        float in = ease(t / form);
        float out = Math.min(1, (life - t) / 12.0F);
        return Math.max(0, Math.min(in, out));
    }

    @Override
    protected void onTick() {
        Player p = localPlayer();
        boolean inside = p != null && p.position().distanceTo(c) < radius;
        if (inside) {
            ScreenFx.vignette(0.32F * fade(age), 0.03F, 0.32F, 0.4F);
        }
        for (int k = 0; k < beats; k++) {
            if ((int) beatTime(k) == age) {
                particle(new ShriekParticleOption(0), c.x, c.y + 0.6, c.z, 0, 0, 0);
                particle(new ShriekParticleOption(5), c.x, c.y + 0.6, c.z, 0, 0, 0);
                if (inside) {
                    ScreenFx.pulse(0.6F);
                    ScreenFx.shakeAt(c, 0.8F, radius);
                }
            }
        }
    }

    @Override
    public void render(float partial) {
        float t = time(partial);
        float f = fade(t);
        if (f <= 0.01F) {
            return;
        }
        float beat = 0;
        for (int k = 0; k < beats; k++) {
            float d = t - beatTime(k);
            if (d >= 0 && d < 16) {
                beat = Math.max(beat, 1 - d / 16.0F);
                float rr = d * 1.25F;
                Draw.groundRing(Draw.add(WpRenderTypes.BAND), c.x, c.y + 0.07, c.z, rr - 0.8F, rr + 0.4F, 96, PALE[0], PALE[1], PALE[2], 0.85F * (1 - d / 16.0F) * f);
            }
        }
        float r = radius * (0.2F + 0.8F * ease(t / form));
        VertexConsumer dome = Draw.add(WpRenderTypes.NOISE);
        Draw.sphere(dome, c, r, 14, 36, t * 0.004F, -t * 0.012F, CYAN[0], CYAN[1], CYAN[2], (0.025F + 0.08F * beat) * f, (0.38F + 0.45F * beat) * f, Math.PI / 2);
        Draw.flat(Draw.add(WpRenderTypes.RUNE), c.x, c.y + 0.05, c.z, r, t * 0.006F, TEAL[0], TEAL[1], TEAL[2], (0.35F + 0.4F * beat) * f);
        Draw.flat(Draw.add(WpRenderTypes.RUNE), c.x, c.y + 0.055, c.z, r * 0.45F, -t * 0.015F, CYAN[0], CYAN[1], CYAN[2], (0.3F + 0.5F * beat) * f);
        Draw.groundRing(Draw.add(WpRenderTypes.BAND), c.x, c.y + 0.06, c.z, r - 0.7F, r + 0.25F, 96, CYAN[0], CYAN[1], CYAN[2], 0.8F * f);
        Draw.wall(Draw.add(WpRenderTypes.FADE), c.x, c.y, c.z, r, 2.2F + 1.5F * beat, 96, 14, t * 0.02F, CYAN[0], CYAN[1], CYAN[2], (0.35F + 0.4F * beat) * f, 0);
        Draw.billboard(Draw.add(WpRenderTypes.GLOW), c.x, c.y + 1.9, c.z, 1.2F + 2.4F * beat, 0, CYAN[0], CYAN[1], CYAN[2], (0.4F + 0.6F * beat) * f * (0.85F + 0.15F * Mth.sin(t)));
    }
}




