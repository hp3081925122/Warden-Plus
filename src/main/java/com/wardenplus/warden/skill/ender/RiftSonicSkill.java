package com.wardenplus.warden.skill.ender;

import com.wardenplus.WpConfig;
import com.wardenplus.network.Vfx;
import com.wardenplus.warden.Combat;
import com.wardenplus.warden.EnderOps;
import com.wardenplus.warden.SkillType;
import com.wardenplus.warden.WardenController;
import com.wardenplus.warden.skill.WardenSkill;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.Vec3;

public final class RiftSonicSkill extends WardenSkill {
    private static final int INTRO = 24;
    private static final int BLINK = 8;
    private static final int GATHER = 28;
    private static final int LOCK = 20;
    private static final int AFTER = 6;
    private static final int CYCLE = BLINK + GATHER + AFTER;
    private static final int DARK_REFRESH = 5;
    private static final int DARK_HOLD = 30;
    private int jumps;
    private int stun;
    private boolean stunned;

    @Override
    public int duration() {
        return INTRO + jumps * CYCLE + stun;
    }

    @Override
    public boolean needsTarget() {
        return false;
    }

    @Override
    public void start(WardenController c, LivingEntity target) {
        Warden w = c.warden;
        c.stopMoving();
        int min = WpConfig.SONIC_MIN_JUMPS.get();
        int max = Math.max(min, WpConfig.SONIC_MAX_JUMPS.get());
        jumps = c.stage() >= 3 ? max : min + w.getRandom().nextInt(max - min + 1);
        stun = WpConfig.SONIC_STUN_TICKS.get();
        w.setPose(Pose.ROARING);
        Vfx.anim(w, Vfx.ANIM_SONIC, INTRO);
        sound(w, SoundEvents.WARDEN_ROAR, 5.0F, 0.7F);
        sound(w, SoundEvents.ENDER_DRAGON_GROWL, 3.0F, 0.6F);
    }

    @Override
    public void tick(WardenController c, LivingEntity target, int t) {
        Warden w = c.warden;
        c.stopMoving();
        if (t % DARK_REFRESH == 0 && t < INTRO + jumps * CYCLE) {
            int sent = EnderOps.darkness(w, WpConfig.DARKNESS_RADIUS.get(), DARK_HOLD);
            if (t == 0) {
                EnderOps.logDarkness(w, WpConfig.DARKNESS_RADIUS.get(), sent);
            }
        }
        if (t < INTRO) {
            if (t == INTRO - 1 && w.hasPose(Pose.ROARING)) {
                w.setPose(Pose.STANDING);
            }
            return;
        }
        int k = t - INTRO;
        int cycle = k / CYCLE;
        if (cycle >= jumps) {
            tickStun(c, k - jumps * CYCLE);
            return;
        }
        int local = k % CYCLE;
        if (local == 0) {
            blink(c, target);
            return;
        }
        if (local < BLINK) {
            c.face(c.aimPos(target));
            return;
        }
        if (local == BLINK) {
            c.face(c.aimPos(target));
            Vfx.anim(w, Vfx.ANIM_SONIC, GATHER + AFTER);
            SonicCharge.begin(c, target, GATHER, LOCK);
            return;
        }
        if (local < BLINK + LOCK) {
            c.face(c.aimPos(target));
            return;
        }
        if (local == BLINK + LOCK) {
            SonicCharge.lock(c, target, GATHER - LOCK, Combat.damage(w, SkillType.RIFT_SONIC));
        }
    }

    private void blink(WardenController c, LivingEntity target) {
        Warden w = c.warden;
        Vec3 center = c.aimPos(target);
        Vec3 best = null;
        for (int i = 0; i < 10; i++) {
            Vec3 spot = EnderOps.findSpot(w, center, 8.0, 14.0, 6);
            if (spot == null) {
                continue;
            }
            best = spot;
            Vec3 eye = spot.add(0, 1.8, 0);
            Vec3 aim = target != null ? target.getEyePosition() : center.add(0, 1.0, 0);
            if (!Combat.blocked(w.level(), eye, aim, w)) {
                break;
            }
        }
        if (best != null) {
            EnderOps.teleport(w, w, best);
        }
        c.face(c.aimPos(target));
    }

    private void tickStun(WardenController c, int s) {
        Warden w = c.warden;
        if (!stunned) {
            stunned = true;
            c.damageTakenScale = WpConfig.SONIC_STUN_DAMAGE_TAKEN.get().floatValue();
            Vfx.anim(w, Vfx.ANIM_STUN, stun);
            Vfx.send(w, Vfx.STUN, stun);
            sound(w, SoundEvents.WARDEN_HURT, 3.0F, 0.5F);
            sound(w, SoundEvents.BEACON_DEACTIVATE, 3.0F, 0.6F);
        }
    }

    @Override
    public void stop(WardenController c, boolean interrupted) {
        c.damageTakenScale = 1.0F;
        if (c.warden.hasPose(Pose.ROARING)) {
            c.warden.setPose(Pose.STANDING);
        }
    }
}




