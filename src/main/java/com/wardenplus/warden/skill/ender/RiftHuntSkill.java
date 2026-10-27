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

public final class RiftHuntSkill extends WardenSkill {
    private static final int WARN = 22;
    private static final int STRIKE = 12;
    private Vec3 dest;

    public static boolean canStart(WardenController c, LivingEntity target, double dist) {
        return target != null && EnderOps.findSpot(c.warden, target.position(), 3.0, 5.0, 12) != null;
    }

    @Override
    public int duration() {
        return dest == null ? 1 : WARN + STRIKE + 14;
    }

    @Override
    public void start(WardenController c, LivingEntity target) {
        Warden w = c.warden;
        c.stopMoving();
        dest = target != null ? EnderOps.findSpot(w, target.position(), 3.0, 5.0, 16) : null;
        if (dest == null) {
            return;
        }
        EnderFx.rift(w, dest.add(0, 0.05, 0), EnderFx.UP, 2.2F, WARN + 8, EnderFx.tag(w, 3), -1);
        EnderFx.rift(w, w.position().add(0, 0.05, 0), EnderFx.UP, 1.8F, WARN + 4, EnderFx.tag(w, 4), -1);
        Vfx.anim(w, Vfx.ANIM_BLINK, WARN + STRIKE + 14);
        sound(w, SoundEvents.WARDEN_SNIFF, 3.0F, 0.6F);
        sound(w, dest, SoundEvents.PORTAL_AMBIENT, 2.0F, 1.6F);
    }

    @Override
    public void tick(WardenController c, LivingEntity target, int t) {
        Warden w = c.warden;
        c.stopMoving();
        if (dest == null) {
            return;
        }
        c.face(t < WARN ? dest : c.aimPos(target));
        if (t == WARN) {
            EnderOps.teleport(w, w, dest);
            c.face(c.aimPos(target));
        }
        if (t == WARN + STRIKE) {
            Vec3 center = EnderFx.ground(w, w.position().add(forward(w, 1.6)));
            EnderFx.slam(c, center, Combat.damage(w, SkillType.RIFT_HUNT), 3.0, 1, 7.0);
            sound(w, center, SoundEvents.WARDEN_ATTACK_IMPACT, 3.5F, 0.7F);
        }
    }
}
