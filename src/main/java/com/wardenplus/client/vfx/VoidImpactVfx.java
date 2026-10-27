package com.wardenplus.client.vfx;

import com.wardenplus.client.Draw;
import com.wardenplus.client.ScreenFx;
import com.wardenplus.client.WpRenderTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class VoidImpactVfx extends VfxBase {
    private final Vec3 c;
    private final float power;

    public VoidImpactVfx(int id, Vec3 center, float power, int life) {
        super(id, life);
        this.c = center;
        this.power = power;
    }

    @Override
    protected void onTick() {
        if (age == 0) {
            ScreenFx.shakeAt(c, 4.5F * power, 30);
            if (localPlayer() != null && localPlayer().position().distanceTo(c) < 12) {
                ScreenFx.flash(0.55F, 0.2F, 0.9F, 0.5F);
            }
            for (int i = 0; i < 60; i++) {
                double a = RANDOM.nextDouble() * Math.PI * 2;
                double s = 0.3 + RANDOM.nextDouble() * 0.5;
                debris(c.x, c.y, c.z, Math.cos(a) * s, 0.4 + RANDOM.nextDouble() * 0.6, Math.sin(a) * s);
            }
            for (int i = 0; i < 40; i++) {
                particle(ParticleTypes.DRAGON_BREATH, c.x, c.y + 0.3, c.z, RANDOM.nextGaussian() * 0.35, RANDOM.nextDouble() * 0.3, RANDOM.nextGaussian() * 0.35);
                particle(ParticleTypes.REVERSE_PORTAL, c.x, c.y + 0.5, c.z, RANDOM.nextGaussian() * 0.5, RANDOM.nextDouble() * 0.8, RANDOM.nextGaussian() * 0.5);
            }
            particle(ParticleTypes.EXPLOSION_EMITTER, c.x, c.y + 0.5, c.z, 0, 0, 0);
        }
    }

    @Override
    public void render(float partial) {
        float t = time(partial);
        float f = Mth.clamp(1 - t / life, 0, 1);
        float burst = Mth.clamp(1 - t / 10.0F, 0, 1);
        float h = 14.0F * ease(t / 4.0F) * power;
        Draw.wall(Draw.add(WpRenderTypes.FADE), c.x, c.y, c.z, 1.2F + 2.5F * (1 - burst), h * f, 28, 3, -t * 0.08F, VOID[0], VOID[1], VOID[2], 0.9F * burst, 0);
        Draw.wall(Draw.add(WpRenderTypes.FADE), c.x, c.y, c.z, 0.6F + 1.2F * (1 - burst), h * 1.2F * f, 20, 2, t * 0.1F, VOID_PALE[0], VOID_PALE[1], VOID_PALE[2], burst, 0);
        Draw.billboard(Draw.add(WpRenderTypes.GLOW), c.x, c.y + 0.8, c.z, (2.0F + 5.0F * (1 - burst)) * power, 0, VOID[0], VOID[1], VOID[2], burst);
        Draw.flat(Draw.add(WpRenderTypes.RUNE), c.x, c.y + 0.06, c.z, 4.5F * power, t * 0.04F, VOID[0], VOID[1], VOID[2], 0.8F * f);
    }
}
