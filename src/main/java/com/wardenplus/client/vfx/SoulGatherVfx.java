package com.wardenplus.client.vfx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wardenplus.client.Draw;
import com.wardenplus.client.WpRenderTypes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class SoulGatherVfx extends VfxBase {
    private static final int WISP_LIFE = 14;
    private final List<Wisp> wisps = new ArrayList<>();

    private static final class Wisp {
        final Vec3 offset;
        final float spin;
        final int born;

        Wisp(Vec3 offset, float spin, int born) {
            this.offset = offset;
            this.spin = spin;
            this.born = born;
        }
    }

    public SoulGatherVfx(int id, int life) {
        super(id, life + 4);
    }

    private Vec3 wispPos(Wisp wsp, Vec3 core, float t) {
        float p = smooth((t - wsp.born) / WISP_LIFE);
        float a = wsp.spin * p * 2.2F;
        double cos = Math.cos(a);
        double sin = Math.sin(a);
        Vec3 o = new Vec3(wsp.offset.x * cos - wsp.offset.z * sin, wsp.offset.y, wsp.offset.x * sin + wsp.offset.z * cos);
        return core.add(o.scale(1 - p));
    }

    @Override
    protected void onTick() {
        Entity e = entity();
        if (e == null) {
            return;
        }
        Vec3 core = chest(e, 1);
        wisps.removeIf(w -> age - w.born > WISP_LIFE);
        if (age < life - 4 - WISP_LIFE / 2) {
            for (int i = 0; i < 2; i++) {
                Vec3 dir = new Vec3(RANDOM.nextGaussian(), RANDOM.nextGaussian() * 0.5, RANDOM.nextGaussian()).normalize();
                wisps.add(new Wisp(dir.scale(5.0 + RANDOM.nextDouble() * 3.0), (RANDOM.nextFloat() - 0.5F) * 2, age));
            }
        }
        for (int i = 0; i < 4; i++) {
            Vec3 dir = new Vec3(RANDOM.nextGaussian(), RANDOM.nextGaussian() * 0.6, RANDOM.nextGaussian()).normalize();
            Vec3 start = core.add(dir.scale(4 + RANDOM.nextDouble() * 3));
            Vec3 vel = core.subtract(start).scale(0.09);
            particle(i % 2 == 0 ? ParticleTypes.SOUL : ParticleTypes.SCULK_SOUL, start.x, start.y, start.z, vel.x, vel.y, vel.z);
        }
        if (age % 3 == 0) {
            particle(ParticleTypes.SOUL_FIRE_FLAME, core.x, core.y, core.z, RANDOM.nextGaussian() * 0.02, 0.02, RANDOM.nextGaussian() * 0.02);
        }
    }

    @Override
    public void render(float partial) {
        Entity e = entity();
        if (e == null) {
            return;
        }
        float t = time(partial);
        Vec3 core = chest(e, partial);
        float p = smooth(t / (life - 4));
        float out = t > life - 4 ? Math.max(0, (life - t) / 4F) : 1;
        VertexConsumer glow = Draw.add(WpRenderTypes.GLOW);
        VertexConsumer band = Draw.add(WpRenderTypes.BAND);
        for (Wisp w : wisps) {
            float wt = (t - w.born) / WISP_LIFE;
            if (wt < 0 || wt > 1) {
                continue;
            }
            Vec3 head = wispPos(w, core, t);
            Vec3 tail = wispPos(w, core, Math.max(w.born, t - 3.5F));
            float a = Mth.sin(wt * Mth.PI) * out;
            Draw.beam(band, tail, head, 0.02F, 0.22F, 0, 1, PALE[0], PALE[1], PALE[2], 0.0F, 0.9F * a);
            Draw.billboard(glow, head.x, head.y, head.z, 0.32F, t * 0.3F, CYAN[0], CYAN[1], CYAN[2], 0.95F * a);
            Draw.billboard(glow, head.x, head.y, head.z, 0.14F, 0, PALE[0], PALE[1], PALE[2], a);
        }
        float flick = 0.85F + 0.15F * Mth.sin(t * 1.9F);
        Draw.billboard(Draw.shade(WpRenderTypes.GLOW), core.x, core.y, core.z, (0.5F + 1.2F * p) * flick, 0, VOID_DEEP[0], VOID_DEEP[1], VOID_DEEP[2], 0.7F * out);
        Draw.billboard(glow, core.x, core.y, core.z, (0.6F + 1.9F * p) * flick, t * 0.1F, VOID[0], VOID[1], VOID[2], 0.85F * out);
        Draw.billboard(glow, core.x, core.y, core.z, (0.3F + 1.0F * p) * flick, -t * 0.12F, CYAN[0], CYAN[1], CYAN[2], 0.9F * out);
        Draw.billboard(glow, core.x, core.y, core.z, 0.2F + 0.5F * p, 0, PALE[0], PALE[1], PALE[2], out);
        float yaw = (e instanceof LivingEntity le ? Mth.rotLerp(partial, le.yBodyRotO, le.yBodyRot) : e.getYRot()) * Mth.DEG_TO_RAD;
        Vec3 fwd = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
        Vec3 sigil = core.add(fwd.scale(0.5));
        float size = 1.0F + 1.4F * p;
        Draw.disc(Draw.add(WpRenderTypes.RUNE), sigil, fwd, size, t * 0.06F, VOID[0], VOID[1], VOID[2], 0.75F * p * out);
        Draw.disc(Draw.add(WpRenderTypes.RUNE), sigil.add(fwd.scale(0.02)), fwd, size * 0.62F, -t * 0.1F, VOID_PALE[0], VOID_PALE[1], VOID_PALE[2], 0.5F * p * out);
        Draw.ring(band, sigil.add(fwd.scale(0.03)), fwd, size * 1.02F, size * 1.12F, 40, t * 0.04F, VOID[0], VOID[1], VOID[2], 0.9F * p * out, 0.9F * p * out);
    }
}




