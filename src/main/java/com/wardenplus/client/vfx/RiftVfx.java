package com.wardenplus.client.vfx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wardenplus.client.Draw;
import com.wardenplus.client.WpRenderTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class RiftVfx extends VfxBase {
    private static final int OPEN = 6;
    private static final int CLOSE = 7;
    private final Vec3 pos;
    private final Vec3 normal;
    private final Vec3 u;
    private final Vec3 w;
    private final float radius;
    private final int follow;
    private final int tag;
    private int closeAt = -1;

    public RiftVfx(int id, Vec3 pos, Vec3 normal, float radius, int tag, int follow, int life) {
        super(id, life + CLOSE);
        this.pos = pos;
        this.normal = normal.lengthSqr() < 1.0E-6 ? new Vec3(0, 1, 0) : normal.normalize();
        Vec3 ref = Math.abs(this.normal.y) > 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        this.u = this.normal.cross(ref).normalize();
        this.w = this.normal.cross(u).normalize();
        this.radius = radius;
        this.follow = follow;
        this.tag = tag;
    }

    public int tag() {
        return tag;
    }

    public void close() {
        if (closeAt < 0 || closeAt > age) {
            closeAt = age;
        }
    }

    private Vec3 center(float partial) {
        if (follow >= 0) {
            Entity e = entityById(follow);
            if (e != null) {
                return e.getPosition(partial).add(0, 0.06, 0);
            }
        }
        return pos;
    }

    private float openness(float t) {
        float in = ease(t / OPEN);
        int end = closeAt >= 0 ? closeAt : life - CLOSE;
        float out = Mth.clamp(1 - (t - end) / CLOSE, 0, 1);
        return in * out;
    }

    @Override
    protected void onTick() {
        float o = openness(age);
        if (o <= 0.05F) {
            return;
        }
        Vec3 c = center(1);
        for (int i = 0; i < 3; i++) {
            double a = RANDOM.nextDouble() * Math.PI * 2;
            Vec3 d = u.scale(Math.cos(a)).add(w.scale(Math.sin(a)));
            Vec3 p = c.add(d.scale(radius * o));
            particle(ParticleTypes.REVERSE_PORTAL, p.x, p.y, p.z, normal.x * 0.05, normal.y * 0.05, normal.z * 0.05);
        }
        if (RANDOM.nextInt(2) == 0) {
            particle(ParticleTypes.PORTAL, c.x, c.y, c.z, RANDOM.nextGaussian() * radius, RANDOM.nextGaussian() * radius, RANDOM.nextGaussian() * radius);
        }
    }

    @Override
    public void render(float partial) {
        float t = time(partial);
        float o = openness(t);
        if (o <= 0.01F) {
            return;
        }
        Vec3 c = center(partial);
        float r = radius * o;
        float pulse = 0.85F + 0.15F * Mth.sin(t * 0.5F);
        Draw.disc(Draw.shade(WpRenderTypes.GLOW), c, normal, r * 1.05F, 0, VOID_DEEP[0], VOID_DEEP[1], VOID_DEEP[2], 0.95F);
        Draw.disc(Draw.add(WpRenderTypes.NOISE), c.add(normal.scale(0.01)), normal, r * 0.95F, t * 0.08F, VOID_DIM[0], VOID_DIM[1], VOID_DIM[2], 0.9F);
        Draw.disc(Draw.add(WpRenderTypes.RUNE), c.add(normal.scale(0.02)), normal, r * 1.25F, -t * 0.05F, VOID[0], VOID[1], VOID[2], 0.7F * pulse);
        VertexConsumer band = Draw.add(WpRenderTypes.BAND);
        Draw.ring(band, c.add(normal.scale(0.03)), normal, r * 0.82F, r * 1.08F, 40, t * 0.03F, VOID[0], VOID[1], VOID[2], 0.0F, 0.95F * pulse);
        Draw.ring(band, c.add(normal.scale(0.035)), normal, r * 0.98F, r * 1.04F, 40, -t * 0.05F, VOID_PALE[0], VOID_PALE[1], VOID_PALE[2], 0.9F, 0.9F);
        Draw.disc(Draw.add(WpRenderTypes.GLOW), c.add(normal.scale(0.04)), normal, r * 1.8F, 0, VOID[0], VOID[1], VOID[2], 0.35F * pulse);
    }
}




