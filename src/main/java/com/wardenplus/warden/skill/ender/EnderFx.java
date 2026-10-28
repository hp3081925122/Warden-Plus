package com.wardenplus.warden.skill.ender;

import com.wardenplus.network.Vfx;
import com.wardenplus.warden.Combat;
import com.wardenplus.warden.WardenController;
import com.wardenplus.warden.hazard.ShockRing;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class EnderFx {
    static final Vec3 UP = new Vec3(0, 1, 0);

    private EnderFx() {
    }

    static void rift(Warden w, Vec3 pos, Vec3 normal, float radius, int life, int tag, int follow) {
        Vfx.send(w, Vfx.RIFT, life, Vfx.pos(pos, (float) normal.x, (float) normal.y, (float) normal.z, radius, tag, follow));
    }

    static void mark(Warden w, LivingEntity e, float radius, int life) {
        Vfx.send(w, Vfx.ENTITY_MARK, life, e.getId(), radius);
    }

    static void riftEnd(Warden w, int tag) {
        Vfx.send(w, Vfx.RIFT_END, 0, tag);
    }

    static int tag(Warden w, int index) {
        return (w.tickCount & 0xFFFF) * 16 + index;
    }

    static Vec3 ground(Warden w, Vec3 p) {
        return new Vec3(p.x, Combat.groundY(w.level(), p.x, p.y + 0.5, p.z), p.z);
    }

    static void slam(WardenController c, Vec3 center, float damage, double radius, int rings, double ringMax) {
        Warden w = c.warden;
        for (LivingEntity e : Combat.victims(w, new AABB(center, center).inflate(radius, 3, radius))) {
            if (e.distanceToSqr(center.x, e.getY(), center.z) > radius * radius || Math.abs(e.getY() - center.y) > 2.5) {
                continue;
            }
            e.hurt(Combat.physical(w), damage); if (true) {
                Combat.knock(e, center.x, center.z, 0.55, 0.45);
            }
        }
        for (int i = 0; i < rings; i++) {
            int delay = i * 6;
            c.addHazard(new ShockRing(center, 0.65, ringMax, damage * 0.45F, delay, 0.35, 0.25));
            Vfx.send(w, Vfx.SHOCK_RING, (int) Math.ceil(ringMax / 0.65) + delay, Vfx.pos(center, 0.65F, (float) ringMax, delay, i == 0 ? 1.2F : 0.8F, Vfx.PALETTE_ENDER));
        }
        Vfx.send(w, Vfx.CRACK, 90, Vfx.pos(center, (float) radius + 1.5F, Vfx.PALETTE_ENDER));
    }
}




