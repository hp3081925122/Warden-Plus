package com.wardenplus.client.vfx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wardenplus.client.Draw;
import com.wardenplus.client.WpRenderTypes;
import com.wardenplus.warden.Combat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class BurrowVfx extends VfxBase {
    private static final int TRAIL_LIFE = 34;
    private final List<float[]> trail = new ArrayList<>();
    private final Vec3 origin;
    private int endAge = -1;

    public BurrowVfx(int id, Vec3 origin, int life) {
        super(id, life + TRAIL_LIFE);
        this.origin = origin;
    }

    public void end() {
        if (endAge < 0) {
            endAge = age;
        }
    }

    @Override
    public boolean tick() {
        return super.tick() && (endAge < 0 || age - endAge < TRAIL_LIFE);
    }

    @Override
    protected void onTick() {
        trail.removeIf(p -> age - p[3] > TRAIL_LIFE);
        Entity e = entity();
        if (e == null || endAge >= 0 || age > life - TRAIL_LIFE) {
            return;
        }
        if (age < 18) {
            for (int i = 0; i < 6; i++) {
                double a = RANDOM.nextDouble() * Math.PI * 2;
                double r = 1.0 + RANDOM.nextDouble() * 0.6;
                debris(origin.x + Math.cos(a) * r, origin.y, origin.z + Math.sin(a) * r, -Math.cos(a) * 0.05, 0.25 + RANDOM.nextDouble() * 0.2, -Math.sin(a) * 0.05);
            }
            if (age % 3 == 0) {
                trail.add(new float[]{(float) origin.x, (float) origin.y, (float) origin.z, age, 2.4F, RANDOM.nextFloat() * Mth.TWO_PI});
            }
            return;
        }
        double gy = Combat.groundY(e.level(), e.getX(), e.getY() + 3.3, e.getZ());
        for (int i = 0; i < 4; i++) {
            debris(e.getX() + RANDOM.nextGaussian() * 0.7, gy, e.getZ() + RANDOM.nextGaussian() * 0.7, 0, 0.18 + RANDOM.nextDouble() * 0.2, 0);
        }
        if (RANDOM.nextInt(2) == 0) {
            particle(ParticleTypes.SCULK_SOUL, e.getX() + RANDOM.nextGaussian() * 0.6, gy + 0.2, e.getZ() + RANDOM.nextGaussian() * 0.6, 0, 0.05, 0);
        }
        if (age % 2 == 0) {
            trail.add(new float[]{(float) e.getX(), (float) gy, (float) e.getZ(), age, 1.3F, RANDOM.nextFloat() * Mth.TWO_PI});
        }
    }

    @Override
    public void render(float partial) {
        float t = time(partial);
        VertexConsumer glow = Draw.add(WpRenderTypes.GLOW);
        VertexConsumer crack = Draw.add(WpRenderTypes.CRACK);
        VertexConsumer dark = Draw.shade(WpRenderTypes.CRACK);
        for (float[] p : trail) {
            float f = 1 - (t - p[3]) / TRAIL_LIFE;
            if (f <= 0) {
                continue;
            }
            Draw.flat(dark, p[0], p[1] + 0.02, p[2], p[4], p[5], DEEP[0], DEEP[1], DEEP[2], 0.75F * f);
            Draw.flat(crack, p[0], p[1] + 0.035, p[2], p[4], p[5], CYAN[0], CYAN[1], CYAN[2], 0.85F * f * f);
            Draw.flat(glow, p[0], p[1] + 0.03, p[2], p[4] * 1.3F, 0, TEAL[0], TEAL[1], TEAL[2], 0.5F * f);
        }
    }
}
