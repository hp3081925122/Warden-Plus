package com.wardenplus.warden.skill;

import com.wardenplus.WpConfig;
import com.wardenplus.network.Vfx;
import com.wardenplus.warden.Combat;
import com.wardenplus.warden.SkillType;
import com.wardenplus.warden.WardenController;
import com.wardenplus.warden.hazard.ShockRing;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.Vec3;

public final class AwakenSkill extends WardenSkill {
    private static final int BURST = 26;

    @Override
    public int duration() {
        return 72;
    }

    @Override
    public boolean needsTarget() {
        return false;
    }

    @Override
    public void start(WardenController c, LivingEntity target) {
        Warden w = c.warden;
        c.stopMoving();
        c.damageTakenScale = WpConfig.AWAKEN_DAMAGE_TAKEN.get().floatValue();
        w.setPose(Pose.ROARING);
        Vfx.anim(w, Vfx.ANIM_AWAKEN, duration());
        Vfx.send(w, Vfx.AWAKEN, duration());
        sound(w, SoundEvents.WARDEN_ROAR, 5.0F, 0.85F);
    }

    @Override
    public void tick(WardenController c, LivingEntity target, int t) {
        Warden w = c.warden;
        c.stopMoving();
        if (t != BURST) {
            return;
        }
        double gy = Combat.groundY(w.level(), w.getX(), w.getY(), w.getZ());
        Vec3 center = new Vec3(w.getX(), gy, w.getZ());
        c.addHazard(new ShockRing(center, 0.85, 12.0, Combat.damage(w, SkillType.AWAKEN), 0, 0.9, 0.3));
        Vfx.send(w, Vfx.SHOCK_RING, 16, Vfx.pos(center, 0.85F, 12.0F, 0, 1.4F));
        Vfx.send(w, Vfx.CRACK, 120, Vfx.pos(center, 7.0F));
        sound(w, SoundEvents.WARDEN_HEARTBEAT, 8.0F, 0.5F);
        sound(w, SoundEvents.GENERIC_EXPLODE.value(), 3.0F, 0.5F);
    }

    @Override
    public void stop(WardenController c, boolean interrupted) {
        c.damageTakenScale = 1.0F;
        if (c.warden.hasPose(Pose.ROARING)) {
            c.warden.setPose(Pose.STANDING);
        }
    }
}




