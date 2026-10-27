package com.wardenplus.client.vfx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wardenplus.client.Draw;
import com.wardenplus.client.WpRenderTypes;
import com.wardenplus.warden.skill.EchoPrismSkill;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class PrismVfx extends VfxBase {
    private final Vec3[] prisms;

    public PrismVfx(int id, float[] data, int life) {
        super(id, life);
        prisms = new Vec3[data.length / 3];
        for (int i = 0; i < prisms.length; i++) {
            prisms[i] = v(data, i * 3);
        }
    }

    private int lockTime(int i) {
        return EchoPrismSkill.SUMMON + i * EchoPrismSkill.GAP;
    }

    private int fireTime(int i) {
        return lockTime(i) + EchoPrismSkill.CHARGE;
    }

    @Override
    protected void onTick() {
        for (int i = 0; i < prisms.length; i++) {
            Vec3 p = prisms[i];
            if (age == fireTime(i)) {
                for (int k = 0; k < 18; k++) {
                    particle(ParticleTypes.END_ROD, p.x, p.y, p.z, RANDOM.nextGaussian() * 0.12, RANDOM.nextGaussian() * 0.12, RANDOM.nextGaussian() * 0.12);
                }
                particle(ParticleTypes.FLASH, p.x, p.y, p.z, 0, 0, 0);
            } else if (age < fireTime(i) && age > i * 2 && RANDOM.nextInt(3) == 0) {
                particle(ParticleTypes.SCULK_CHARGE_POP, p.x + RANDOM.nextGaussian() * 0.3, p.y + RANDOM.nextGaussian() * 0.3, p.z + RANDOM.nextGaussian() * 0.3, 0, -0.02, 0);
            }
        }
    }

    @Override
    public void render(float partial) {
        float t = time(partial);
        Entity w = entity();
        Vec3 chest = w == null ? null : w.getPosition(partial).add(0, 2.2, 0);
        VertexConsumer glow = Draw.add(WpRenderTypes.GLOW);
        VertexConsumer white = Draw.add(WpRenderTypes.WHITE);
        VertexConsumer solid = Draw.solid();
        for (int i = 0; i < prisms.length; i++) {
            if (t >= fireTime(i) + 1) {
                continue;
            }
            float appear = ease((t - i * 2) / 10.0F);
            if (appear <= 0) {
                continue;
            }
            float charge = t > lockTime(i) ? Mth.clamp((t - lockTime(i)) / EchoPrismSkill.CHARGE, 0, 1) : 0;
            Vec3 p = prisms[i].add(0, Mth.sin(t * 0.12F + i) * 0.15, 0);
            float spin = t * (0.06F + charge * 0.4F) + i;
            float size = 0.42F * appear * (1 + charge * 0.25F);
            Draw.crystal(solid, p, size, size * 2.1F, spin, DEEP[0] * 2.0F, DEEP[1] * 2.6F, DEEP[2] * 2.6F, 1, true);
            Draw.crystal(white, p, size * 1.12F, size * 2.35F, spin, CYAN[0], CYAN[1], CYAN[2], 0.35F + 0.5F * charge, false);
            float flick = 0.85F + 0.15F * Mth.sin(t * 2.3F + i);
            Draw.billboard(glow, p.x, p.y, p.z, (0.9F + 1.6F * charge) * appear * flick, t * 0.05F, CYAN[0], CYAN[1], CYAN[2], 0.7F);
            Draw.billboard(glow, p.x, p.y, p.z, (0.3F + 0.6F * charge) * appear, 0, PALE[0], PALE[1], PALE[2], 0.9F);
            if (chest != null) {
                Draw.beam(Draw.add(WpRenderTypes.BAND), chest, p, 0.05F, 0.12F, -t * 0.15F, 3, TEAL[0], TEAL[1], TEAL[2], 0.25F * appear, 0.55F * appear);
            }
        }
    }
}
