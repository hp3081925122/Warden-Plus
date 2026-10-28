package com.wardenplus.client.vfx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wardenplus.client.Draw;
import com.wardenplus.client.ScreenFx;
import com.wardenplus.client.WpRenderTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class EnderTrailVfx extends VfxBase {
    private static final int TRAVEL = 7;
    private final Vec3 from;
    private final Vec3 to;
    private final float height;
    private final int mover;

    public EnderTrailVfx(int id, Vec3 from, Vec3 to, float height, int mover, int life) {
        super(id, life);
        this.from = from;
        this.to = to;
        this.height = Math.max(0.8F, height);
        this.mover = mover;
    }

    @Override
    protected void onTick() {
        Vec3 d = to.subtract(from);
        if (age == 0) {
            for (int i = 0; i < 36; i++) {
                particle(ParticleTypes.PORTAL, from.x + RANDOM.nextGaussian() * 0.4, from.y + RANDOM.nextGaussian() * height * 0.35, from.z + RANDOM.nextGaussian() * 0.4, RANDOM.nextGaussian() * 0.6, RANDOM.nextGaussian() * 0.6, RANDOM.nextGaussian() * 0.6);
            }
            Player p = localPlayer();
            if (p != null && p.getId() == mover) {
                ScreenFx.flash(0.45F, 0.1F, 0.6F, 0.4F);
            }
            ScreenFx.shakeAt(to, 0.8F, 14);
        }
        if (age == TRAVEL) {
            for (int i = 0; i < 36; i++) {
                particle(ParticleTypes.REVERSE_PORTAL, to.x + RANDOM.nextGaussian() * 0.35, to.y + RANDOM.nextGaussian() * height * 0.35, to.z + RANDOM.nextGaussian() * 0.35, RANDOM.nextGaussian() * 0.08, RANDOM.nextGaussian() * 0.08, RANDOM.nextGaussian() * 0.08);
            }
        }
        if (age < TRAVEL + 4) {
            int n = Math.max(2, (int) (d.length() * 0.8));
            for (int i = 0; i < n; i++) {
                Vec3 p = from.add(d.scale(RANDOM.nextDouble()));
                particle(ParticleTypes.REVERSE_PORTAL, p.x + RANDOM.nextGaussian() * 0.12, p.y + RANDOM.nextGaussian() * 0.12, p.z + RANDOM.nextGaussian() * 0.12, d.x * 0.01, d.y * 0.01, d.z * 0.01);
            }
        }
    }

    private void tear(VertexConsumer glow, VertexConsumer noise, Vec3 c, float open, float t) {
        if (open <= 0.01F) {
            return;
        }
        Vec3 top = c.add(0, height * 0.65 * open, 0);
        Vec3 bottom = c.add(0, -height * 0.65 * open, 0);
        Draw.beam(noise, bottom, top, 1.3F * open, t * 0.05F, 1.5F, VOID_DIM[0], VOID_DIM[1], VOID_DIM[2], 0.9F);
        Draw.beam(glow, bottom, top, 2.2F * open, 0, 1, VOID[0], VOID[1], VOID[2], 0.7F);
        Draw.beam(glow, bottom, top, 0.6F * open, 0, 1, VOID_PALE[0], VOID_PALE[1], VOID_PALE[2], 0.9F);
    }

    @Override
    public void render(float partial) {
        float t = time(partial);
        float fadeOut = Mth.clamp((life - t) / 10.0F, 0, 1);
        VertexConsumer glow = Draw.add(WpRenderTypes.GLOW);
        VertexConsumer noise = Draw.add(WpRenderTypes.NOISE);
        VertexConsumer band = Draw.add(WpRenderTypes.BAND);
        float openFrom = ease(t / 3.0F) * Mth.clamp(1 - (t - 6) / 8.0F, 0, 1);
        float openTo = ease((t - TRAVEL + 3) / 4.0F) * fadeOut;
        tear(glow, noise, from, openFrom, t);
        tear(glow, noise, to, openTo, t);
        float head = Mth.clamp(t / TRAVEL, 0, 1);
        float tail = Mth.clamp((t - 3) / (TRAVEL + 6), 0, 1);
        if (head > tail + 0.001F) {
            Vec3 d = to.subtract(from);
            Vec3 a = from.add(d.scale(tail));
            Vec3 b = from.add(d.scale(head));
            Draw.beam(band, a, b, 0.05F, 0.75F, -t * 0.4F, 2, VOID[0], VOID[1], VOID[2], 0.15F, 0.95F);
            Draw.beam(band, a, b, 0.02F, 0.28F, -t * 0.4F, 2, VOID_PALE[0], VOID_PALE[1], VOID_PALE[2], 0.2F, 1.0F);
            if (head < 1.0F) {
                Draw.billboard(glow, b.x, b.y, b.z, 0.9F, t * 0.3F, VOID[0], VOID[1], VOID[2], 1.0F);
                Draw.billboard(glow, b.x, b.y, b.z, 0.4F, -t * 0.4F, VOID_PALE[0], VOID_PALE[1], VOID_PALE[2], 1.0F);
            }
        }
        float ghost = Mth.clamp(1 - t / 16.0F, 0, 1);
        if (ghost > 0) {
            Vec3 d = to.subtract(from);
            for (int i = 1; i < 4; i++) {
                Vec3 p = from.add(d.scale(i / 4.0));
                Draw.billboard(glow, p.x, p.y, p.z, 0.35F * ghost, t * 0.2F + i, VOID[0], VOID[1], VOID[2], 0.6F * ghost);
            }
        }
    }
}




