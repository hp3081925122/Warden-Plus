package com.wardenplus.client.vfx;

import com.wardenplus.client.Draw;
import com.wardenplus.client.WpRenderTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class MarkVfx extends VfxBase {
    private final int follow;
    private final float radius;

    public MarkVfx(int id, int follow, float radius, int life) {
        super(id, life);
        this.follow = follow;
        this.radius = radius;
    }

    @Override
    protected void onTick() {
        Entity e = entityById(follow);
        if (e == null || age % 2 != 0) {
            return;
        }
        double a = RANDOM.nextDouble() * Math.PI * 2;
        particle(ParticleTypes.REVERSE_PORTAL, e.getX() + Math.cos(a) * radius, e.getY() + 0.1, e.getZ() + Math.sin(a) * radius, 0, 0.12, 0);
    }

    @Override
    public void render(float partial) {
        Entity e = entityById(follow);
        if (e == null) {
            return;
        }
        float t = time(partial);
        float p = Mth.clamp(t / life, 0, 1);
        float in = ease(t / 5.0F);
        float flick = 0.6F + 0.4F * Mth.sin(t * (0.6F + 2.4F * p));
        Vec3 c = e.getPosition(partial);
        float r = radius * (1.4F - 0.4F * in);
        Draw.flat(Draw.add(WpRenderTypes.RUNE), c.x, c.y + 0.05, c.z, r, t * (0.05F + 0.2F * p), VOID[0], VOID[1], VOID[2], 0.9F * in * flick);
        Draw.flat(Draw.add(WpRenderTypes.GLOW), c.x, c.y + 0.04, c.z, r * 1.5F, 0, VOID_DIM[0], VOID_DIM[1], VOID_DIM[2], 0.7F * in);
        Draw.wall(Draw.add(WpRenderTypes.FADE), c.x, c.y, c.z, r * 0.8F, (float) (e.getBbHeight() + 0.5) * p, 24, 2, t * 0.06F, VOID[0], VOID[1], VOID[2], 0.6F * in, 0);
    }
}




