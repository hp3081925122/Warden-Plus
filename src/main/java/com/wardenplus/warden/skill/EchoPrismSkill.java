package com.wardenplus.warden.skill;

import com.wardenplus.network.Vfx;
import com.wardenplus.warden.Combat;
import com.wardenplus.warden.SkillType;
import com.wardenplus.warden.WardenController;
import com.wardenplus.warden.hazard.BeamHazard;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.Vec3;

public final class EchoPrismSkill extends WardenSkill {
    public static final int SUMMON = 16;
    public static final int GAP = 7;
    public static final int CHARGE = 12;
    private Vec3[] prisms;

    @Override
    public int duration() {
        return SUMMON + prisms.length * GAP + CHARGE + 8;
    }

    @Override
    public void start(WardenController c, LivingEntity target) {
        Warden w = c.warden;
        c.stopMoving();
        c.face(c.aimPos(target));
        int n = c.stage() >= 3 ? 6 : 4;
        prisms = new Vec3[n];
        float[] data = new float[n * 3];
        float yaw = w.getYRot() * Mth.DEG_TO_RAD;
        for (int i = 0; i < n; i++) {
            double a = yaw + Math.PI + (i - (n - 1) / 2.0) * (Math.PI * 1.2 / Math.max(1, n - 1));
            double r = 3.4;
            Vec3 p = w.position().add(-Math.sin(a) * r, 4.2 + Math.sin(i * 1.7) * 0.5, Math.cos(a) * r);
            prisms[i] = p;
            data[i * 3] = (float) p.x;
            data[i * 3 + 1] = (float) p.y;
            data[i * 3 + 2] = (float) p.z;
        }
        Vfx.anim(w, Vfx.ANIM_PRISM, duration());
        Vfx.send(w, Vfx.PRISMS, duration(), data);
        sound(w, SoundEvents.AMETHYST_BLOCK_RESONATE, 4.0F, 0.5F);
        sound(w, SoundEvents.WARDEN_SNIFF, 3.0F, 0.8F);
    }

    @Override
    public void tick(WardenController c, LivingEntity target, int t) {
        Warden w = c.warden;
        c.stopMoving();
        c.face(c.aimPos(target));
        if (t < SUMMON || (t - SUMMON) % GAP != 0) {
            return;
        }
        int i = (t - SUMMON) / GAP;
        if (i >= prisms.length) {
            return;
        }
        Vec3 from = prisms[i];
        Vec3 aim;
        if (target != null) {
            Vec3 v = target.getDeltaMovement();
            aim = target.position().add(v.x * 5, target.getBbHeight() * 0.5, v.z * 5);
        } else {
            aim = c.aimPos(null).add(0, 1, 0);
        }
        Vec3 dir = aim.subtract(from).normalize();
        Vec3 to = Combat.clipBlocks(w.level(), from, from.add(dir.scale(34)), w);
        c.addHazard(new BeamHazard(from, to, CHARGE, Combat.damage(w, SkillType.ECHO_PRISM)));
        Vfx.send(w, Vfx.BEAM, CHARGE + 10, (float) from.x, (float) from.y, (float) from.z, (float) to.x, (float) to.y, (float) to.z, CHARGE, i);
        sound(w, from, SoundEvents.AMETHYST_BLOCK_CHIME, 3.0F, 0.6F + i * 0.1F);
    }
}
