package com.wardenplus.client.vfx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wardenplus.client.Draw;
import com.wardenplus.client.WpRenderTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class StunVfx extends VfxBase {
    public StunVfx(int id, int life) {
        super(id, life);
    }

    private Vec3 head(Entity e, float partial) {
        return e.getPosition(partial).add(0, 3.4, 0);
    }

    @Override
    protected void onTick() {
        Entity e = entity();
        if (e == null) {
            return;
        }
        Vec3 h = head(e, 1);
        if (age % 2 == 0) {
            particle(ParticleTypes.REVERSE_PORTAL, h.x + RANDOM.nextGaussian() * 0.6, h.y - 1.2, h.z + RANDOM.nextGaussian() * 0.6, 0, -0.05, 0);
        }
        if (age % 8 == 0) {
            particle(ParticleTypes.SOUL, e.getX() + RANDOM.nextGaussian() * 0.5, e.getY() + 1.5, e.getZ() + RANDOM.nextGaussian() * 0.5, 0, 0.04, 0);
        }
    }

    @Override
    public void render(float partial) {
        Entity e = entity();
        if (e == null) {
            return;
        }
        float t = time(partial);
        float env = Mth.clamp(t / 5.0F, 0, 1) * Mth.clamp((life - t) / 8.0F, 0, 1);
        Vec3 h = head(e, partial);
        VertexConsumer glow = Draw.add(WpRenderTypes.GLOW);
        for (int i = 0; i < 4; i++) {
            double a = t * 0.22 + i * Math.PI / 2;
            double x = h.x + Math.cos(a) * 1.0;
            double z = h.z + Math.sin(a) * 1.0;
            double y = h.y + Math.sin(t * 0.3 + i) * 0.15;
            Draw.billboard(glow, x, y, z, 0.35F, t * 0.4F, VOID[0], VOID[1], VOID[2], 0.9F * env);
            Draw.billboard(glow, x, y, z, 0.15F, 0, VOID_PALE[0], VOID_PALE[1], VOID_PALE[2], env);
        }
        Draw.flat(Draw.add(WpRenderTypes.RUNE), h.x, h.y - 0.1, h.z, 1.3F, -t * 0.08F, VOID[0], VOID[1], VOID[2], 0.55F * env);
    }
}




