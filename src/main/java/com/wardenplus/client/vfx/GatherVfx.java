package com.wardenplus.client.vfx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wardenplus.client.Draw;
import com.wardenplus.client.WpRenderTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class GatherVfx extends VfxBase {
    public GatherVfx(int id, int life) {
        super(id, life + 4);
    }

    private Vec3[] hands(Entity e, float partial, float p) {
        float yaw = (e instanceof LivingEntity le ? Mth.rotLerp(partial, le.yBodyRotO, le.yBodyRot) : e.getYRot()) * Mth.DEG_TO_RAD;
        Vec3 base = e.getPosition(partial);
        Vec3 side = new Vec3(Math.cos(yaw), 0, Math.sin(yaw)).scale(0.95 + 0.25 * p);
        Vec3 fwd = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw)).scale(0.3);
        double y = 2.1 + 1.9 * p;
        return new Vec3[]{base.add(side).add(fwd).add(0, y, 0), base.subtract(side).add(fwd).add(0, y, 0)};
    }

    @Override
    protected void onTick() {
        Entity e = entity();
        if (e == null) {
            return;
        }
        float p = smooth((float) age / (life - 4));
        for (Vec3 h : hands(e, 1, p)) {
            for (int i = 0; i < 3; i++) {
                Vec3 dir = new Vec3(RANDOM.nextGaussian(), RANDOM.nextGaussian() * 0.6, RANDOM.nextGaussian()).normalize();
                Vec3 start = h.add(dir.scale(3 + RANDOM.nextDouble() * 2));
                Vec3 vel = h.subtract(start).scale(0.11);
                particle(ParticleTypes.SCULK_SOUL, start.x, start.y, start.z, vel.x, vel.y, vel.z);
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
        float p = smooth(t / (life - 4));
        float out = t > life - 4 ? Math.max(0, (life - t) / 4F) : 1;
        VertexConsumer vc = Draw.add(WpRenderTypes.GLOW);
        for (Vec3 h : hands(e, partial, p)) {
            float flick = 0.85F + 0.15F * Mth.sin(t * 1.7F);
            Draw.billboard(vc, h.x, h.y, h.z, (0.35F + 1.25F * p) * flick, t * 0.1F, CYAN[0], CYAN[1], CYAN[2], 0.85F * out);
            Draw.billboard(vc, h.x, h.y, h.z, (0.15F + 0.5F * p), -t * 0.15F, PALE[0], PALE[1], PALE[2], 0.9F * out);
        }
    }
}
