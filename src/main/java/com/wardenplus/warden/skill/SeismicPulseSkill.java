package com.wardenplus.warden.skill;

import com.wardenplus.network.Vfx;
import com.wardenplus.warden.Combat;
import com.wardenplus.warden.SkillType;
import com.wardenplus.warden.WardenController;
import com.wardenplus.warden.hazard.ShockRing;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.Vec3;

public final class SeismicPulseSkill extends WardenSkill {
    private static final int SLAM = 22;

    @Override
    public int duration() {
        return 40;
    }

    @Override
    public void start(WardenController c, LivingEntity target) {
        Warden w = c.warden;
        c.stopMoving();
        Vfx.anim(w, Vfx.ANIM_SLAM, duration());
        Vfx.send(w, Vfx.GATHER, SLAM);
        sound(w, SoundEvents.WARDEN_ANGRY, 3.0F, 0.7F);
    }

    @Override
    public void tick(WardenController c, LivingEntity target, int t) {
        Warden w = c.warden;
        c.stopMoving();
        if (t < SLAM) {
            c.face(c.aimPos(target));
            return;
        }
        if (t != SLAM) {
            return;
        }
        Vec3 front = w.position().add(forward(w, 1.8));
        double gy = Combat.groundY(w.level(), front.x, w.getY(), front.z);
        Vec3 center = new Vec3(front.x, gy, front.z);
        int rings = c.stage() >= 2 ? 3 : 2;
        double maxR = c.stage() >= 2 ? 13.0 : 11.0;
        float damage = Combat.damage(w, SkillType.SEISMIC_PULSE);
        for (int i = 0; i < rings; i++) {
            int delay = i * 7;
            c.addHazard(new ShockRing(center, 0.6, maxR, damage, delay, 0.5, 0.35));
            Vfx.send(w, Vfx.SHOCK_RING, (int) Math.ceil(maxR / 0.6) + delay, Vfx.pos(center, 0.6F, (float) maxR, delay, i == 0 ? 1.0F : 0.7F));
        }
        Vfx.send(w, Vfx.CRACK, 90, Vfx.pos(center, 5.5F));
        sound(w, center, SoundEvents.WARDEN_ATTACK_IMPACT, 4.0F, 0.55F);
        sound(w, center, SoundEvents.GENERIC_EXPLODE.value(), 2.0F, 0.6F);
    }
}




