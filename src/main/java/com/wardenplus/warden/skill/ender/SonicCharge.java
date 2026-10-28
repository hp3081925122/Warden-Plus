package com.wardenplus.warden.skill.ender;

import com.wardenplus.network.Vfx;
import com.wardenplus.warden.WardenController;
import com.wardenplus.warden.hazard.SonicBeam;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.Vec3;

final class SonicCharge {
    static final double RANGE = 28.0;
    static final double WIDTH = 1.1;

    private SonicCharge() {
    }

    static Vec3 chest(Warden w) {
        float yaw = w.getYRot() * ((float) Math.PI / 180F);
        return w.position().add(-Math.sin(yaw) * 0.6, 1.75, Math.cos(yaw) * 0.6);
    }

    static void begin(WardenController c, LivingEntity target, int gather, int lock) {
        Warden w = c.warden;
        Vfx.send(w, Vfx.SOUL_GATHER, gather);
        Vfx.send(w, Vfx.SONIC_TRACK, lock, target != null ? target.getId() : -1);
        w.level().playSound(null, w.getX(), w.getY(), w.getZ(), SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.HOSTILE, 3.0F, 1.0F);
    }

    static void lock(WardenController c, LivingEntity target, int fireDelay, float damage) {
        Warden w = c.warden;
        Vec3 from = chest(w);
        Vec3 aim = target != null ? target.position().add(0, target.getBbHeight() * 0.5, 0) : c.aimPos(null).add(0, 1.0, 0);
        Vec3 dir = aim.subtract(from);
        if (dir.lengthSqr() < 1.0E-4) {
            dir = new Vec3(0, 0, 1);
        }
        Vec3 to = from.add(dir.normalize().scale(RANGE));
        c.addHazard(new SonicBeam(from, to, fireDelay, damage, WIDTH));
        Vfx.send(w, Vfx.SONIC_LOCK, fireDelay + 14, (float) from.x, (float) from.y, (float) from.z, (float) to.x, (float) to.y, (float) to.z, fireDelay);
    }
}




