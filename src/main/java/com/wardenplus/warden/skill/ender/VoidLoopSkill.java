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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class VoidLoopSkill extends WardenSkill {
    private static final int GRAB = 13;
    private static final int LIFT = 24;
    private static final int THROW = 34;
    private static final int RETURN = 40;
    private static final int MISS_END = 24;
    private static final int LOOPS = 2;
    private static final int MAX_FALL = 90;
    private static final double MIN_CLEARANCE = 14.0;
    private static final double LOWER = 3.5;
    private int phase;
    private int fallTicks;
    private int loops;
    private int end = 400;
    private double groundY;
    private double upperY;
    private double lowerY;
    private Vec3 column;
    private LivingEntity held;
    private boolean airborne;
    private int lowerTag;
    private int upperTag;
    private double fallSpeed;

    public static boolean canStart(WardenController c, LivingEntity target, double dist) {
        if (target == null || target.isPassenger() || target.isVehicle() || !Combat.onGroundNear(target, Combat.groundY(target.level(), target.getX(), target.getY(), target.getZ()))) {
            return false;
        }
        Vec3 g = new Vec3(target.getX(), target.getY(), target.getZ());
        return EnderOps.clearance(c.warden, g, MIN_CLEARANCE + 1) >= MIN_CLEARANCE && EnderOps.clearance(c.warden, c.warden.position(), MIN_CLEARANCE + 1) >= MIN_CLEARANCE;
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
        Vfx.anim(w, Vfx.ANIM_GRAB, GRAB + 10);
        sound(w, SoundEvents.WARDEN_ANGRY, 3.0F, 1.2F);
    }

    private static Vec3 right(Warden w, double dist) {
        float yaw = w.getYRot() * Mth.DEG_TO_RAD;
        return new Vec3(-Math.cos(yaw) * dist, 0.0, -Math.sin(yaw) * dist);
    }

    private Vec3 hand(Warden w) {
        return w.position().add(forward(w, 1.7)).add(right(w, 0.4)).add(0, 1.7, 0);
    }

    private void hold(Warden w) {
        Vec3 h = hand(w);
        held.teleportTo(h.x, h.y - held.getBbHeight() * 0.5, h.z);
        held.setDeltaMovement(Vec3.ZERO);
        held.hurtMarked = true;
        held.resetFallDistance();
    }

    @Override
    public void tick(WardenController c, LivingEntity target, int t) {
        Warden w = c.warden;
        switch (phase) {
            case 0 -> {
                c.face(c.aimPos(target));
                if (t < GRAB) {
                    if (t >= 9) {
                        Vec3 f = forward(w, 0.3);
                        w.setDeltaMovement(f.x, w.getDeltaMovement().y, f.z);
                    } else {
                        c.stopMoving();
                    }
                    return;
                }
                c.stopMoving();
                if (target != null && hand(w).distanceTo(EnderOps.center(target)) <= 2.3 + target.getBbWidth() * 0.5) {
                    held = target;
                    phase = 1;
                    held.hurt(Combat.physical(w), Combat.damage(w, SkillType.VOID_LOOP) * 0.25F);
                    hold(w);
                    Vfx.anim(w, Vfx.ANIM_GRAB_HOLD, THROW + 8 - GRAB);
                    sound(w, SoundEvents.WARDEN_ATTACK_IMPACT, 2.0F, 1.3F);
                    sound(w, SoundEvents.PLAYER_ATTACK_STRONG, 2.0F, 0.6F);
                } else {
                    phase = 9;
                    end = t + MISS_END;
                    sound(w, SoundEvents.PLAYER_ATTACK_SWEEP, 2.0F, 0.5F);
                }
            }
            case 1 -> {
                c.stopMoving();
                if (!valid()) {
                    abort(c, t);
                    return;
                }
                hold(w);
                if (t < LIFT) {
                    return;
                }
                groundY = Combat.groundY(w.level(), w.getX(), w.getY(), w.getZ());
                double room = Math.min(EnderOps.clearance(w, new Vec3(w.getX(), groundY, w.getZ()), 26), EnderOps.clearance(held, new Vec3(held.getX(), groundY, held.getZ()), 26));
                if (room < MIN_CLEARANCE) {
                    abort(c, t);
                    return;
                }
                double lift = Math.min(22.0, room - 4.0);
                airborne = true;
                w.setNoGravity(true);
                c.invulnerable = true;
                EnderOps.teleport(w, w, new Vec3(w.getX(), groundY + lift, w.getZ()));
                w.setDeltaMovement(Vec3.ZERO);
                Vec3 h = hand(w);
                EnderOps.teleport(w, held, new Vec3(h.x, h.y - held.getBbHeight() * 0.5, h.z));
                column = new Vec3(h.x, 0, h.z);
                double colGround = Combat.groundY(w.level(), column.x, groundY + 1, column.z);
                groundY = colGround;
                lowerY = groundY + LOWER;
                upperY = groundY + lift - 2.0;
                phase = 2;
            }
            case 2 -> {
                w.setDeltaMovement(Vec3.ZERO);
                if (!valid()) {
                    abort(c, t);
                    return;
                }
                hold(w);
                if (t < THROW) {
                    return;
                }
                lowerTag = EnderFx.tag(w, 1);
                upperTag = EnderFx.tag(w, 2);
                EnderFx.rift(w, new Vec3(column.x, lowerY, column.z), EnderFx.UP, 1.9F, MAX_FALL + 20, lowerTag, -1);
                EnderFx.rift(w, new Vec3(column.x, upperY + held.getBbHeight() + 0.3, column.z), EnderFx.UP.reverse(), 1.9F, MAX_FALL + 20, upperTag, -1);
                fallSpeed = 1.5;
                launch();
                sound(w, SoundEvents.WARDEN_ROAR, 3.0F, 1.4F);
                sound(w, SoundEvents.PORTAL_TRIGGER, 2.0F, 1.2F);
                phase = 3;
            }
            case 3 -> {
                if (t >= RETURN && airborne) {
                    land(c);
                }
                if (!valid()) {
                    closeRifts(w);
                    end = t + 10;
                    phase = 9;
                    return;
                }
                fallTicks++;
                held.resetFallDistance();
                if (loops < LOOPS && held.getY() <= lowerY) {
                    loops++;
                    fallSpeed = Math.min(3.6, fallSpeed + 0.75);
                    EnderOps.teleport(w, held, new Vec3(column.x, upperY, column.z));
                    launch();
                    sound(w, new Vec3(column.x, upperY, column.z), SoundEvents.PORTAL_TRAVEL, 0.6F, 1.8F);
                    if (loops >= LOOPS) {
                        EnderFx.riftEnd(w, lowerTag);
                        EnderFx.riftEnd(w, upperTag);
                    }
                    return;
                }
                boolean down = held.onGround() || held.getY() <= groundY + 0.25;
                if (loops >= LOOPS && down || fallTicks >= MAX_FALL) {
                    impact(c, w);
                    end = t + 20;
                    phase = 9;
                }
            }
            default -> {
                if (airborne && t >= RETURN) {
                    land(c);
                }
            }
        }
    }

    private void launch() {
        held.setDeltaMovement(0, -fallSpeed, 0);
        held.hurtMarked = true;
        held.resetFallDistance();
    }

    private boolean valid() {
        return held != null && held.isAlive() && !held.isRemoved() && !held.isSpectator();
    }

    private void impact(WardenController c, Warden w) {
        Vec3 at = new Vec3(held.getX(), Combat.groundY(w.level(), held.getX(), held.getY() + 0.5, held.getZ()), held.getZ());
        float base = Combat.damage(w, SkillType.VOID_LOOP);
        float damage = base * (1.0F + 0.25F * loops);
        held.resetFallDistance();
        held.hurt(Combat.physical(w), damage); if (true) {
            double resist = Mth.clamp(1.0 - held.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.0, 1.0);
            held.setDeltaMovement(0, 0.35 * resist, 0);
            held.hurtMarked = true;
        }
        for (LivingEntity e : Combat.victims(w, new AABB(at, at).inflate(4.0, 2.5, 4.0))) {
            if (e == held || e.distanceToSqr(at) > 16.0) {
                continue;
            }
            e.hurt(Combat.physical(w), base * 0.5F); if (true) {
                Combat.knock(e, at.x, at.z, 0.6, 0.4);
            }
        }
        c.addHazard(new ShockRing(at, 0.8, 10.0, base * 0.3F, 2, 0.35, 0.3));
        Vfx.send(w, Vfx.VOID_IMPACT, 40, Vfx.pos(at, 1.0F + 0.3F * loops));
        Vfx.send(w, Vfx.SHOCK_RING, 16, Vfx.pos(at, 0.8F, 10.0F, 2, 1.6F, Vfx.PALETTE_ENDER));
        Vfx.send(w, Vfx.CRACK, 120, Vfx.pos(at, 5.0F, Vfx.PALETTE_ENDER));
        sound(w, at, SoundEvents.GENERIC_EXPLODE.value(), 4.0F, 0.55F);
        sound(w, at, SoundEvents.WARDEN_ATTACK_IMPACT, 5.0F, 0.5F);
        sound(w, at, SoundEvents.ANVIL_LAND, 2.0F, 0.5F);
    }

    private void land(WardenController c) {
        Warden w = c.warden;
        airborne = false;
        Vec3 near = held != null ? held.position() : w.position();
        Vec3 ref = new Vec3(near.x, groundY, near.z);
        Vec3 spot = EnderOps.findSpot(w, ref, 4.0, 7.0, 24);
        if (spot == null) {
            spot = new Vec3(w.getX(), Combat.groundY(w.level(), w.getX(), groundY + 1, w.getZ()), w.getZ());
        }
        w.setNoGravity(false);
        c.invulnerable = false;
        EnderOps.teleport(w, w, spot);
        c.face(ref);
        Vfx.anim(w, Vfx.ANIM_NONE, 1);
    }

    private void closeRifts(Warden w) {
        if (lowerTag != 0) {
            EnderFx.riftEnd(w, lowerTag);
            EnderFx.riftEnd(w, upperTag);
        }
    }

    private void abort(WardenController c, int t) {
        if (airborne) {
            land(c);
        }
        closeRifts(c.warden);
        phase = 9;
        end = t + 6;
    }

    @Override
    public void stop(WardenController c, boolean interrupted) {
        if (airborne) {
            land(c);
        }
        if (phase == 3) {
            closeRifts(c.warden);
        }
        c.warden.setNoGravity(false);
        c.invulnerable = false;
    }
}




