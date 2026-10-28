package com.wardenplus.warden.skill.ender;

import com.wardenplus.network.Vfx;
import com.wardenplus.warden.Combat;
import com.wardenplus.warden.EnderOps;
import com.wardenplus.warden.SkillType;
import com.wardenplus.warden.WardenController;
import com.wardenplus.warden.skill.WardenSkill;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.Vec3;

public final class BlinkSlamSkill extends WardenSkill {
    private static final int PREP = 12;
    private static final int WIND = 14;
    private static final int RECOVER = 14;
    private int blinks;
    private int blinkIndex;
    private int phaseStart;
    private Vec3 dest;
    private boolean teleported;

    public static boolean canStart(WardenController c, LivingEntity target, double dist) {
        return target != null && pick(c.warden, target) != null;
    }

    private static Vec3 pick(Warden w, LivingEntity target) {
        Vec3 back = EnderOps.behind(target, w.position(), 2.8);
        Vec3 spot = EnderOps.standAt(w, back.x, target.getY(), back.z);
        if (spot == null) {
            spot = EnderOps.findSpot(w, target.position(), 2.5, 4.5, 16);
        }
        return spot;
    }

    @Override
    public int duration() {
        return blinks * (PREP + WIND) + RECOVER;
    }

    @Override
    public void start(WardenController c, LivingEntity target) {
        blinks = c.stage() >= 3 ? 2 : 1;
        c.stopMoving();
        beginBlink(c, target, 0);
    }

    private void beginBlink(WardenController c, LivingEntity target, int t) {
        Warden w = c.warden;
        phaseStart = t;
        teleported = false;
        dest = target != null ? pick(w, target) : null;
        Vfx.anim(w, Vfx.ANIM_BLINK, PREP + WIND + RECOVER);
        sound(w, SoundEvents.ENDERMAN_STARE, 2.5F, 0.6F);
        if (dest != null) {
            EnderFx.rift(w, dest.add(0, 0.05, 0), EnderFx.UP, 1.6F, PREP + 6, EnderFx.tag(w, blinkIndex), -1);
        }
    }

    @Override
    public void tick(WardenController c, LivingEntity target, int t) {
        Warden w = c.warden;
        c.stopMoving();
        int local = t - phaseStart;
        if (local < PREP) {
            c.face(c.aimPos(target));
            return;
        }
        if (local == PREP && !teleported) {
            teleported = true;
            if (dest != null) {
                EnderOps.teleport(w, w, dest);
            }
            c.face(c.aimPos(target));
            return;
        }
        if (local < PREP + WIND) {
            c.face(c.aimPos(target));
            return;
        }
        if (local == PREP + WIND) {
            Vec3 front = w.position().add(forward(w, 1.9));
            Vec3 center = EnderFx.ground(w, front);
            EnderFx.slam(c, center, Combat.damage(w, SkillType.BLINK_SLAM), 3.2, 2, 9.0);
            sound(w, center, SoundEvents.WARDEN_ATTACK_IMPACT, 4.0F, 0.6F);
            sound(w, center, SoundEvents.GENERIC_EXPLODE, 2.0F, 0.75F);
            blinkIndex++;
            if (blinkIndex < blinks) {
                beginBlink(c, target, t + 1);
            }
        }
    }
}
