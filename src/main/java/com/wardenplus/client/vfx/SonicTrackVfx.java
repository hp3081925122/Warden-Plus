package com.wardenplus.client.vfx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wardenplus.client.Draw;
import com.wardenplus.client.WpRenderTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class SonicTrackVfx extends VfxBase {
    private final int targetId;

    public SonicTrackVfx(int id, int targetId, int life) {
        super(id, life);
        this.targetId = targetId;
    }

    @Override
    public void render(float partial) {
        Entity e = entity();
        Entity target = entityById(targetId);
        if (e == null || target == null) {
            return;
        }
        float t = time(partial);
        float p = t / life;
        Vec3 from = chest(e, partial);
        Vec3 aim = target.getPosition(partial).add(0, target.getBbHeight() * 0.5, 0);
        Vec3 dir = aim.subtract(from);
        if (dir.lengthSqr() < 1.0E-4) {
            return;
        }
        Vec3 to = from.add(dir.normalize().scale(28.0));
        float flick = 0.55F + 0.45F * Mth.sin(t * (1.2F + 2.5F * p));
        float a = (0.18F + 0.35F * p) * flick;
        VertexConsumer band = Draw.add(WpRenderTypes.BAND);
        Draw.beam(band, from, to, 0.18F + 0.25F * p, -t * 0.25F, 6, VOID[0], VOID[1], VOID[2], a);
        Draw.beam(band, from, to, 0.05F + 0.08F * p, 0, 1, VOID_PALE[0], VOID_PALE[1], VOID_PALE[2], a * 0.9F);
        Draw.billboard(Draw.add(WpRenderTypes.GLOW), aim.x, aim.y, aim.z, 0.6F + 0.5F * p, t * 0.2F, VOID[0], VOID[1], VOID[2], 0.5F * flick);
    }
}
