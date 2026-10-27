package com.wardenplus.client.vfx;

import com.wardenplus.client.Draw;
import com.wardenplus.client.ScreenFx;
import com.wardenplus.client.WpRenderTypes;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.phys.Vec3;

public final class HeardVfx extends VfxBase {
    private final int victimId;

    public HeardVfx(int wardenId, int victimId, int life) {
        super(wardenId, life);
        this.victimId = victimId;
    }

    private Entity victim() {
        return level() == null ? null : level().getEntity(victimId);
    }

    @Override
    protected void onTick() {
        if (age != 0) {
            return;
        }
        Entity w = entity();
        Entity v = victim();
        if (w == null || v == null) {
            return;
        }
        particle(new VibrationParticleOption(new EntityPositionSource(w, 1.8F), 10), v.getX(), v.getY() + v.getBbHeight() * 0.5, v.getZ(), 0, 0, 0);
        if (v == localPlayer()) {
            ScreenFx.flash(0.2F, 0.85F, 1.0F, 0.3F);
            ScreenFx.shakeAt(v.position(), 1.6F, 4);
        }
    }

    @Override
    public void render(float partial) {
        Entity w = entity();
        Entity v = victim();
        if (w == null || v == null) {
            return;
        }
        float t = time(partial);
        Vec3 from = v.getPosition(partial).add(0, v.getBbHeight() * 0.55, 0);
        Vec3 to = w.getPosition(partial).add(0, 1.8, 0);
        float p = Math.min(1.3F, t / 9.0F);
        Vec3 head = from.lerp(to, Math.min(1, p));
        Vec3 tail = from.lerp(to, Math.max(0, p - 0.35F));
        float f = Math.max(0, 1 - t / life);
        Draw.beam(Draw.add(WpRenderTypes.BAND), tail, head, 0.12F, 0.45F, 0, 1, CYAN[0], CYAN[1], CYAN[2], 0.0F, 1.0F * f);
        Draw.beam(Draw.add(WpRenderTypes.BAND), from, to, 0.05F, 0.05F, -t * 0.2F, 4, PALE[0], PALE[1], PALE[2], 0.35F * f, 0.35F * f);
        Vec3 feet = v.getPosition(partial);
        float rr = 0.4F + t * 0.16F;
        Draw.groundRing(Draw.add(WpRenderTypes.BAND), feet.x, feet.y + 0.05, feet.z, rr - 0.25F, rr + 0.15F, 32, CYAN[0], CYAN[1], CYAN[2], f);
        Draw.billboard(Draw.add(WpRenderTypes.GLOW), from.x, from.y, from.z, 0.9F * f + 0.3F, 0, CYAN[0], CYAN[1], CYAN[2], 0.7F * f);
    }
}
