package com.wardenplus.client.vfx;

import com.wardenplus.client.ClientWardenState;
import com.wardenplus.client.Draw;
import com.wardenplus.client.ScreenFx;
import com.wardenplus.client.WpRenderTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class AwakenVfx extends VfxBase {
    private static final int BURST = 26;

    public AwakenVfx(int id, int life) {
        super(id, life);
    }

    @Override
    protected void onTick() {
        Entity e = entity();
        if (e == null) {
            return;
        }
        ClientWardenState.State s = ClientWardenState.get(entityId);
        s.awaken = Math.max(s.awaken, Math.min(1, (float) age / BURST));
        Vec3 base = e.position();
        for (int i = 0; i < 4; i++) {
            double a = age * 0.35 + i * Math.PI / 2;
            double r = 1.6 + Math.sin(age * 0.1) * 0.3;
            particle(ParticleTypes.SCULK_SOUL, base.x + Math.cos(a) * r, base.y + 0.2 + (age % 20) * 0.15, base.z + Math.sin(a) * r, 0, 0.08, 0);
        }
        if (age < BURST) {
            ScreenFx.shakeAt(base, 0.6F, 30);
        } else if (age == BURST) {
            ScreenFx.shakeAt(base, 5.0F, 36);
            if (localPlayer() != null && localPlayer().distanceTo(e) < 20) {
                ScreenFx.flash(0.3F, 0.9F, 1.0F, 0.45F);
            }
            for (int i = 0; i < 40; i++) {
                Vec3 d = new Vec3(RANDOM.nextGaussian(), Math.abs(RANDOM.nextGaussian()), RANDOM.nextGaussian()).normalize().scale(0.5);
                particle(ParticleTypes.END_ROD, base.x, base.y + 1.9, base.z, d.x, d.y, d.z);
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
        float rise = ease(t / BURST);
        float out = Math.min(1, (life - t) / 18.0F);
        float burst = t > BURST ? Math.max(0, 1 - (t - BURST) / 14.0F) : 0;
        float env = Math.max(0, rise * out);
        Draw.wall(Draw.add(WpRenderTypes.FADE), base.x, base.y + 1.6, base.z, 0.7F + 1.6F * burst, 22 * env, 24, 3, -t * 0.06F, CYAN[0], CYAN[1], CYAN[2], 0.75F * env, 0);
        Draw.wall(Draw.add(WpRenderTypes.FADE), base.x, base.y + 1.6, base.z, 0.3F + 0.6F * burst, 26 * env, 16, 2, t * 0.09F, PALE[0], PALE[1], PALE[2], 0.9F * env, 0);
        float heart = 0.8F + 0.2F * Mth.sin(t * 0.8F);
        Draw.billboard(Draw.add(WpRenderTypes.GLOW), base.x, base.y + 1.9, base.z, (1.5F + 4F * burst) * heart * env + 0.2F, 0, CYAN[0], CYAN[1], CYAN[2], env);
        Draw.flat(Draw.add(WpRenderTypes.RUNE), base.x, base.y + 0.05, base.z, 5.0F + 3.0F * burst, t * 0.02F, CYAN[0], CYAN[1], CYAN[2], 0.6F * env);
    }
}




