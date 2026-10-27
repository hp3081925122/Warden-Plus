package com.wardenplus.warden.skill.ender;

import com.wardenplus.network.Vfx;
import com.wardenplus.warden.Combat;
import com.wardenplus.warden.EnderOps;
import com.wardenplus.warden.SkillType;
import com.wardenplus.warden.WardenController;
import com.wardenplus.warden.hazard.ShockRing;
import com.wardenplus.warden.skill.WardenSkill;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class GrappleSlamSkill extends WardenSkill {
    private static final int WIND = 9;
    private static final int REACH = 13;
    private static final int LIFT = 14;
    private static final int HOLD = 4;
    private static final int SLAM = 4;
    private static final int PIN = 6;
    private static final int CYCLE = LIFT + HOLD + SLAM + PIN;
    private static final int RECOVER = 14;
    private static final int MISS_END = 24;
    private LivingEntity held;
    private int slams;
    private int done;
    private int cycleStart;
    private int end = REACH + 1;
    private double overhead;

    public static boolean canStart(WardenController c, LivingEntity target, double dist) {
        return target != null && !target.isPassenger() && !target.isVehicle();
    }

    @Override
    public int duration() {
        return end;
    }

    @Override
    public void start(WardenController c, LivingEntity target) {
        Warden w = c.warden;
        c.stopMoving();
        c.face(c.aimPos(target));
        Vfx.anim(w, Vfx.ANIM_GRAB, REACH + 10);
        sound(w, SoundEvents.WARDEN_ANGRY, 3.0F, 1.2F);
    }

    private static Vec3 right(Warden w, double dist) {
        float yaw = w.getYRot() * Mth.DEG_TO_RAD;
        return new Vec3(-Math.cos(yaw) * dist, 0.0, -Math.sin(yaw) * dist);
    }

    private Vec3 hand(Warden w) {
        return w.position().add(forward(w, 1.7)).add(right(w, 0.4)).add(0, 1.7, 0);
    }

    @Override
    public void tick(WardenController c, LivingEntity target, int t) {
        Warden w = c.warden;
        if (t < REACH) {
            c.face(c.aimPos(target));
            if (t >= WIND) {
                Vec3 f = forward(w, 0.3);
                w.setDeltaMovement(f.x, w.getDeltaMovement().y, f.z);
            } else {
                c.stopMoving();
            }
            return;
        }
        c.stopMoving();
        if (t == REACH) {
            grab(c, target, t);
            return;
        }
        if (held == null || t >= end - RECOVER) {
            return;
        }
        if (!held.isAlive() || held.isRemoved() || held.isSpectator()) {
            held = null;
            end = t + RECOVER;
            Vfx.anim(w, Vfx.ANIM_NONE, 1);
            return;
        }
        int local = t - cycleStart;
        place(w, local);
        if (local == LIFT + HOLD + SLAM) {
            impact(c, w);
        }
        if (local == CYCLE - 1) {
            done++;
            if (done < slams) {
                cycleStart = t + 1;
                Vfx.anim(w, Vfx.ANIM_LIFT_SLAM, done + 1 < slams ? CYCLE : CYCLE + RECOVER);
                sound(w, SoundEvents.WARDEN_ANGRY, 2.5F, 1.0F);
            } else {
                Combat.knock(held, w.getX(), w.getZ(), 0.5, 0.3);
                held = null;
            }
        }
    }

    private void grab(WardenController c, LivingEntity target, int t) {
        Warden w = c.warden;
        if (target == null || hand(w).distanceTo(EnderOps.center(target)) > 2.3 + target.getBbWidth() * 0.5) {
            end = t + MISS_END;
            sound(w, SoundEvents.PLAYER_ATTACK_SWEEP, 2.0F, 0.5F);
            return;
        }
        held = target;
        slams = c.stage() >= 2 ? 2 : 1;
        cycleStart = t + 1;
        end = cycleStart + slams * CYCLE + RECOVER;
        double room = EnderOps.clearance(w, w.position(), 7.0);
        overhead = Mth.clamp(room - held.getBbHeight() * 0.5 - 0.2, 2.0, 5.5);
        Vfx.anim(w, Vfx.ANIM_LIFT_SLAM, slams > 1 ? CYCLE : CYCLE + RECOVER);
        sound(w, SoundEvents.WARDEN_ATTACK_IMPACT, 2.0F, 1.3F);
        sound(w, SoundEvents.PLAYER_ATTACK_STRONG, 2.0F, 0.6F);
    }

    private void place(Warden w, int local) {
        Vec3 base = w.position();
        Vec3 front = hand(w);
        Vec3 top = base.add(forward(w, 0.4)).add(right(w, 0.7)).add(0, overhead, 0);
        Vec3 slamAt = base.add(forward(w, 2.1)).add(right(w, 0.5));
        double gy = Combat.groundY(w.level(), slamAt.x, base.y + 1.0, slamAt.z);
        Vec3 ground = new Vec3(slamAt.x, gy + held.getBbHeight() * 0.5, slamAt.z);
        Vec3 pos;
        if (local < LIFT) {
            float s = smooth((float) local / LIFT);
            Vec3 arc = front.add(top.subtract(front).scale(s));
            pos = arc.add(forward(w, Math.sin(s * Math.PI) * 0.6));
        } else if (local < LIFT + HOLD) {
            pos = top;
        } else if (local < LIFT + HOLD + SLAM) {
            float s = (float) (local - LIFT - HOLD + 1) / SLAM;
            pos = top.add(ground.subtract(top).scale(s * s));
        } else {
            float s = smooth((float) (local - LIFT - HOLD - SLAM) / PIN);
            pos = ground.add(front.subtract(ground).scale(done + 1 < slams ? s : 0));
        }
        held.teleportTo(pos.x, pos.y - held.getBbHeight() * 0.5, pos.z);
        held.setDeltaMovement(Vec3.ZERO);
        held.hurtMarked = true;
        held.resetFallDistance();
    }

    private void impact(WardenController c, Warden w) {
        Vec3 at = new Vec3(held.getX(), Combat.groundY(w.level(), held.getX(), held.getY() + 0.5, held.getZ()), held.getZ());
        float damage = Combat.damage(w, SkillType.GRAPPLE_SLAM);
        held.hurt(Combat.physical(w), damage);
        for (LivingEntity e : Combat.victims(w, new AABB(at, at).inflate(3.0, 2.0, 3.0))) {
            if (e == held || e.distanceToSqr(at) > 9.0) {
                continue;
            }
            if (e.hurt(Combat.physical(w), damage * 0.5F)) {
                Combat.knock(e, at.x, at.z, 0.5, 0.35);
            }
        }
        boolean last = done + 1 >= slams;
        c.addHazard(new ShockRing(at, 0.7, last ? 8.0 : 6.0, damage * 0.3F, 1, 0.3, 0.25));
        Vfx.send(w, Vfx.SHOCK_RING, 14, Vfx.pos(at, 0.7F, last ? 8.0F : 6.0F, 1, last ? 1.4F : 1.0F, Vfx.PALETTE_ENDER));
        Vfx.send(w, Vfx.CRACK, 100, Vfx.pos(at, last ? 4.5F : 3.5F, Vfx.PALETTE_ENDER));
        Vfx.send(w, Vfx.VOID_IMPACT, 30, Vfx.pos(at, last ? 0.9F : 0.6F));
        sound(w, at, SoundEvents.WARDEN_ATTACK_IMPACT, 4.0F, 0.55F);
        sound(w, at, SoundEvents.GENERIC_EXPLODE.value(), last ? 3.0F : 2.0F, 0.7F);
        sound(w, at, SoundEvents.ANVIL_LAND, 1.5F, 0.5F);
    }

    private static float smooth(float x) {
        x = Mth.clamp(x, 0, 1);
        return x * x * (3 - 2 * x);
    }

    @Override
    public void stop(WardenController c, boolean interrupted) {
        if (held != null) {
            held.resetFallDistance();
            held = null;
        }
    }
}
