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

public final class EnderSwapSkill extends WardenSkill {
    private static final int MARK = 20;
    private static final int GATHER = 18;
    private static final int LOCK = 12;
    private static final int FIRE = 6;
    private boolean swapped;

    public static boolean canStart(WardenController c, LivingEntity target, double dist) {
        return target != null && !target.isPassenger() && !target.isVehicle();
    }

    @Override
    public int duration() {
        return MARK + GATHER + 12;
    }

    @Override
    public void start(WardenController c, LivingEntity target) {
        Warden w = c.warden;
        c.stopMoving();
        c.face(c.aimPos(target));
        Vfx.anim(w, Vfx.ANIM_SWAP, duration());
        EnderFx.mark(w, w, 1.8F, MARK + 4);
        if (target != null) {
            EnderFx.mark(w, target, 1.2F, MARK + 4);
        }
        sound(w, SoundEvents.PORTAL_TRIGGER, 2.0F, 1.5F);
        sound(w, SoundEvents.ENDERMAN_STARE, 2.0F, 0.8F);
    }

    @Override
    public void tick(WardenController c, LivingEntity target, int t) {
        Warden w = c.warden;
        c.stopMoving();
        c.face(c.aimPos(target));
        if (t == MARK) {
            swap(c, target);
            if (swapped) {
                SonicCharge.begin(c, target, GATHER, LOCK);
            }
            return;
        }
        if (swapped && t == MARK + LOCK) {
            SonicCharge.lock(c, target, FIRE, Combat.damage(w, SkillType.ENDER_SWAP));
        }
    }

    private void swap(WardenController c, LivingEntity target) {
        Warden w = c.warden;
        if (target == null || !target.isAlive() || target.distanceTo(w) > 32) {
            return;
        }
        Vec3 wp = w.position();
        Vec3 tp = target.position();
        Vec3 wardenDest = EnderOps.fits(w, tp.x, tp.y, tp.z) ? tp : EnderOps.findSpot(w, tp, 0.5, 2.5, 16);
        if (wardenDest == null || !EnderOps.fits(target, wp.x, wp.y, wp.z)) {
            return;
        }
        EnderOps.teleport(w, target, wp);
        EnderOps.teleport(w, w, wardenDest);
        target.setDeltaMovement(Vec3.ZERO);
        target.hurtMarked = true;
        swapped = true;
        c.face(target.position());
        sound(w, SoundEvents.ILLUSIONER_MIRROR_MOVE, 2.0F, 0.6F);
    }
}
