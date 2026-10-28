package com.wardenplus.warden.skill;

import com.wardenplus.network.Vfx;
import com.wardenplus.warden.Combat;
import com.wardenplus.warden.SkillType;
import com.wardenplus.warden.WardenController;
import com.wardenplus.warden.hazard.ShockRing;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class AbyssalBurrowSkill extends WardenSkill {
    private static final int SINK = 18;
    private static final int MAX_TRAVEL = 64;
    private static final int WARN = 14;
    private static final int RECOVER = 16;
    private static final double DEPTH = 3.2;
    private int phase;
    private int phaseTick;
    private double surfaceY;
    private Vec3 eruptPos;
    private boolean hidden;
    private boolean done;

    @Override
    public int duration() {
        return done ? 0 : 400;
    }

    @Override
    public void start(WardenController c, LivingEntity target) {
        Warden w = c.warden;
        c.stopMoving();
        surfaceY = w.getY();
        hidden = true;
        c.invulnerable = true;
        w.setNoGravity(true);
        w.noPhysics = true;
        Vfx.anim(w, Vfx.ANIM_SINK, SINK + 6);
        Vfx.send(w, Vfx.BURROW, SINK + MAX_TRAVEL + WARN + 4, Vfx.pos(w.position()));
        sound(w, SoundEvents.WARDEN_DIG, 3.0F, 0.9F);
    }

    @Override
    public void tick(WardenController c, LivingEntity target, int t) {
        Warden w = c.warden;
        w.setDeltaMovement(Vec3.ZERO);
        w.getNavigation().stop();
        phaseTick++;
        switch (phase) {
            case 0 -> {
                w.setPos(w.getX(), w.getY() - DEPTH / SINK, w.getZ());
                if (phaseTick % 4 == 0) {
                    sound(w, SoundEvents.SCULK_BLOCK_SPREAD, 2.0F, 0.6F);
                }
                if (phaseTick >= SINK) {
                    w.setInvisible(true);
                    next();
                }
            }
            case 1 -> {
                Vec3 dest = c.aimPos(target);
                double speed = c.stage() >= 2 ? 0.75 : 0.58;
                double dx = dest.x - w.getX();
                double dz = dest.z - w.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                double step = Math.min(speed, dist);
                double nx = w.getX() + (dist > 1.0E-4 ? dx / dist * step : 0);
                double nz = w.getZ() + (dist > 1.0E-4 ? dz / dist * step : 0);
                surfaceY = Combat.groundY(w.level(), nx, dest.y, nz);
                w.setPos(nx, surfaceY - DEPTH, nz);
                if (phaseTick % 5 == 0) {
                    sound(w, new Vec3(nx, surfaceY, nz), SoundEvents.SCULK_BLOCK_SPREAD, 2.5F, 0.5F);
                }
                if (dist < 1.2 || phaseTick >= MAX_TRAVEL) {
                    eruptPos = new Vec3(nx, surfaceY, nz);
                    Vfx.send(w, Vfx.ERUPT, WARN + 30, Vfx.pos(eruptPos, 3.5F, WARN));
                    sound(w, eruptPos, SoundEvents.WARDEN_EMERGE, 3.0F, 1.2F);
                    next();
                }
            }
            case 2 -> {
                w.setPos(eruptPos.x, eruptPos.y - DEPTH, eruptPos.z);
                if (phaseTick >= WARN) {
                    erupt(c);
                    next();
                }
            }
            case 3 -> {
                if (phaseTick >= RECOVER) {
                    done = true;
                }
            }
            default -> done = true;
        }
    }

    private void next() {
        phase++;
        phaseTick = 0;
    }

    private void erupt(WardenController c) {
        Warden w = c.warden;
        restore(c);
        w.setPos(eruptPos.x, eruptPos.y + 0.05, eruptPos.z);
        w.setDeltaMovement(0, 0.55, 0);
        Vfx.anim(w, Vfx.ANIM_EMERGE, RECOVER + 6);
        float damage = Combat.damage(w, SkillType.ABYSSAL_BURROW);
        AABB box = new AABB(eruptPos.x - 3.5, eruptPos.y - 1, eruptPos.z - 3.5, eruptPos.x + 3.5, eruptPos.y + 4, eruptPos.z + 3.5);
        for (LivingEntity e : Combat.victims(w, box)) {
            if (e.distanceToSqr(eruptPos.x, e.getY(), eruptPos.z) > 3.5 * 3.5) {
                continue;
            }
            e.hurt(Combat.physical(w), damage); if (true) {
                Combat.knock(e, eruptPos.x, eruptPos.z, 0.6, 0.85);
            }
        }
        c.addHazard(new ShockRing(eruptPos, 0.7, 7.0, damage * 0.4F, 2, 0.3, 0.2));
        Vfx.send(w, Vfx.SHOCK_RING, 14, Vfx.pos(eruptPos, 0.7F, 7.0F, 2, 0.6F));
        Vfx.send(w, Vfx.CRACK, 80, Vfx.pos(eruptPos, 4.0F));
        sound(w, eruptPos, SoundEvents.WARDEN_ATTACK_IMPACT, 4.0F, 0.6F);
        sound(w, eruptPos, SoundEvents.GENERIC_EXPLODE.value(), 2.5F, 0.7F);
        sound(w, eruptPos, SoundEvents.WARDEN_ROAR, 2.0F, 1.2F);
    }

    private void restore(WardenController c) {
        if (!hidden) {
            return;
        }
        hidden = false;
        Warden w = c.warden;
        c.invulnerable = false;
        w.setInvisible(false);
        w.setNoGravity(false);
        w.noPhysics = false;
    }

    @Override
    public void stop(WardenController c, boolean interrupted) {
        if (hidden) {
            Warden w = c.warden;
            restore(c);
            double gy = Combat.groundY(w.level(), w.getX(), surfaceY, w.getZ());
            w.setPos(w.getX(), gy + 0.05, w.getZ());
        }
    }
}




