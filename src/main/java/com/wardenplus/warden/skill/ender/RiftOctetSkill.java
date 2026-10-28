package com.wardenplus.warden.skill.ender;

import com.wardenplus.WpConfig;
import com.wardenplus.network.Vfx;
import com.wardenplus.warden.Combat;
import com.wardenplus.warden.EnderOps;
import com.wardenplus.warden.SkillType;
import com.wardenplus.warden.WardenController;
import com.wardenplus.warden.hazard.ShockRing;
import com.wardenplus.warden.hazard.SonicBeam;
import com.wardenplus.warden.skill.WardenSkill;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class RiftOctetSkill extends WardenSkill {
    private static final int COUNT = 8;
    private static final double RING = 8.0;
    private static final double CAGE = 6.5;
    private static final double HEIGHT = 1.0;
    private static final double SKY = 9.0;
    private static final double BLAST = 4.5;
    private static final int FRONT = 4;
    private static final int OPEN = 8;
    private static final int OPEN_GAP = 2;
    private static final int FRONT_LOCK = 20;
    private static final int FRONT_DELAY = 8;
    private static final int CHAIN = FRONT_LOCK + FRONT_DELAY;
    private static final int CHAIN_GAP = 5;
    private static final int CHAIN_DELAY = 14;
    private static final int RELEASE = CHAIN + (COUNT - 1) * CHAIN_GAP + CHAIN_DELAY + 3;
    private static final int SKY_DELAY = 26;
    private static final int IMPACT = RELEASE + SKY_DELAY;
    private static final int STUN_AT = IMPACT + 6;
    private static final int RIFT_LIFE = 400;
    private Vec3 center;
    private Vec3 frontRift;
    private final Vec3[] rifts = new Vec3[COUNT];
    private final int[] tags = new int[COUNT];
    private int frontTag;
    private int first;
    private int stun;
    private boolean stunned;
    private boolean caged;
    private LivingEntity prisoner;

    public static boolean canStart(WardenController c, LivingEntity target, double dist) {
        return target != null && target.onGround();
    }

    @Override
    public int duration() {
        return STUN_AT + stun;
    }

    @Override
    public void start(WardenController c, LivingEntity target) {
        Warden w = c.warden;
        c.stopMoving();
        stun = WpConfig.SONIC_STUN_TICKS.get();
        prisoner = target;
        center = EnderFx.ground(w, target.position());
        Vec3 spot = null;
        for (int i = 0; i < 10; i++) {
            Vec3 s = EnderOps.findSpot(w, center, 11.0, 15.0, 6);
            if (s == null) {
                continue;
            }
            spot = s;
            if (!Combat.blocked(w.level(), s.add(0, 1.8, 0), center.add(0, 1.5, 0), w)) {
                break;
            }
        }
        if (spot != null) {
            EnderOps.teleport(w, w, spot);
        }
        c.face(center);
        Vec3 away = w.position().subtract(center).multiply(1, 0, 1);
        double base = away.lengthSqr() < 1.0E-4 ? 0 : Math.atan2(away.z, away.x);
        for (int i = 0; i < COUNT; i++) {
            double a = base + Math.PI * 2 * i / COUNT;
            rifts[i] = center.add(Math.cos(a) * RING, HEIGHT, Math.sin(a) * RING);
            tags[i] = EnderFx.tag(w, i + 1);
        }
        first = 0;
        frontTag = EnderFx.tag(w, 10);
        sound(w, SoundEvents.WARDEN_ROAR, 4.0F, 0.8F);
    }

    private Vec3 forward(Warden w) {
        float yaw = w.getYRot() * Mth.DEG_TO_RAD;
        return new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
    }

    @Override
    public void tick(WardenController c, LivingEntity target, int t) {
        Warden w = c.warden;
        c.stopMoving();
        if (t < STUN_AT) {
            c.face(center);
        }
        if (caged) {
            confine(w);
        }
        if (t == FRONT) {
            Vec3 fwd = forward(w);
            frontRift = SonicCharge.chest(w).add(fwd.scale(2.6));
            EnderFx.rift(w, frontRift, fwd.reverse(), 1.4F, RIFT_LIFE, frontTag, -1);
            Vfx.anim(w, Vfx.ANIM_SONIC, CHAIN + 4 - FRONT);
            Vfx.send(w, Vfx.SOUL_GATHER, CHAIN - FRONT);
            sound(w, SoundEvents.WARDEN_SONIC_CHARGE, 3.0F, 1.0F);
            sound(w, frontRift, SoundEvents.PORTAL_TRIGGER, 1.5F, 1.4F);
        }
        for (int i = 0; i < COUNT; i++) {
            if (t == OPEN + i * OPEN_GAP) {
                Vec3 normal = center.add(0, HEIGHT, 0).subtract(rifts[i]).normalize();
                EnderFx.rift(w, rifts[i], normal, 1.3F, RIFT_LIFE, tags[i], -1);
                sound(w, rifts[i], SoundEvents.ENDERMAN_TELEPORT, 1.2F, 0.6F + i * 0.08F);
            }
        }
        if (t == OPEN) {
            caged = prisoner != null;
            Vfx.send(w, Vfx.RIFT_ZONE, RELEASE - OPEN, Vfx.pos(center, (float) CAGE, 0, 0));
        }
        if (t == FRONT_LOCK && frontRift != null) {
            Vec3 from = SonicCharge.chest(w);
            Vfx.send(w, Vfx.SONIC_LOCK, FRONT_DELAY + 14, (float) from.x, (float) from.y, (float) from.z, (float) frontRift.x, (float) frontRift.y, (float) frontRift.z, FRONT_DELAY);
        }
        if (t == CHAIN) {
            sound(w, SoundEvents.WARDEN_SONIC_BOOM, 3.0F, 0.9F);
        }
        for (int k = 0; k < COUNT; k++) {
            if (t == CHAIN + k * CHAIN_GAP) {
                lockRift(c, (first + k) % COUNT, k);
            }
        }
        if (t == RELEASE) {
            release(c);
        }
        if (t == IMPACT) {
            impact(c);
        }
        if (t == STUN_AT) {
            beginStun(c);
        }
    }

    private void lockRift(WardenController c, int i, int order) {
        Warden w = c.warden;
        Vec3 from = rifts[i];
        Vec3 dir = center.add(0, HEIGHT, 0).subtract(from).normalize();
        Vec3 to = from.add(dir.scale(RING * 2.0));
        float damage = Combat.damage(w, SkillType.RIFT_OCTET);
        c.addHazard(new SonicBeam(from, to, CHAIN_DELAY, damage, 1.0));
        Vfx.send(w, Vfx.SONIC_LOCK, CHAIN_DELAY + 14, (float) from.x, (float) from.y, (float) from.z, (float) to.x, (float) to.y, (float) to.z, CHAIN_DELAY);
        sound(w, from, SoundEvents.WARDEN_SONIC_CHARGE, 1.6F, 1.1F + order * 0.06F);
    }

    private void confine(Warden w) {
        LivingEntity e = prisoner;
        if (e == null || !e.isAlive() || e.isRemoved() || e.isSpectator() || e.level() != w.level()) {
            caged = false;
            return;
        }
        double dx = e.getX() - center.x;
        double dz = e.getZ() - center.z;
        if (dx * dx + dz * dz <= CAGE * CAGE && Math.abs(e.getY() - center.y) <= CAGE) {
            return;
        }
        Vec3 back = EnderOps.standAt(e, center.x, center.y, center.z);
        EnderOps.teleport(w, e, back != null ? back : center);
        e.setDeltaMovement(Vec3.ZERO);
        e.hurtMarked = true;
    }

    private void release(WardenController c) {
        Warden w = c.warden;
        caged = false;
        for (int tag : tags) {
            EnderFx.riftEnd(w, tag);
        }
        EnderFx.riftEnd(w, frontTag);
        Vec3 sky = center.add(0, SKY, 0);
        EnderFx.rift(w, sky, EnderFx.UP.reverse(), 2.4F, SKY_DELAY + 12, EnderFx.tag(w, 11), -1);
        Vfx.send(w, Vfx.RIFT_ZONE, SKY_DELAY + 10, Vfx.pos(center, (float) BLAST, 1, SKY_DELAY));
        Vfx.send(w, Vfx.SONIC_LOCK, SKY_DELAY + 14, (float) sky.x, (float) sky.y, (float) sky.z, (float) center.x, (float) center.y, (float) center.z, SKY_DELAY);
        sound(w, sky, SoundEvents.PORTAL_TRIGGER, 3.0F, 0.7F);
        sound(w, sky, SoundEvents.WARDEN_SONIC_CHARGE, 3.0F, 0.8F);
    }

    private void impact(WardenController c) {
        Warden w = c.warden;
        float damage = Combat.damage(w, SkillType.RIFT_OCTET) * 1.5F;
        for (LivingEntity e : Combat.victims(w, new AABB(center, center).inflate(BLAST, SKY, BLAST))) {
            if (e.distanceToSqr(center.x, e.getY(), center.z) > BLAST * BLAST || e.getY() > center.y + SKY || e.getY() < center.y - 2.0) {
                continue;
            }
            e.hurt(w.damageSources().sonicBoom(w), damage); if (true) {
                Combat.knock(e, center.x, center.z, 0.6, 0.35);
            }
        }
        c.addHazard(new ShockRing(center, 0.7, 9.0, damage * 0.3F, 0, 0.35, 0.25));
        Vfx.send(w, Vfx.SHOCK_RING, 16, Vfx.pos(center, 0.7F, 9.0F, 0, 1.5F, Vfx.PALETTE_ENDER));
        Vfx.send(w, Vfx.CRACK, 110, Vfx.pos(center, (float) BLAST + 0.5F, Vfx.PALETTE_ENDER));
        Vfx.send(w, Vfx.VOID_IMPACT, 36, Vfx.pos(center, 1.1F));
        sound(w, center, SoundEvents.WARDEN_SONIC_BOOM, 4.0F, 0.7F);
        sound(w, center, SoundEvents.GENERIC_EXPLODE.value(), 3.0F, 0.6F);
    }

    private void beginStun(WardenController c) {
        Warden w = c.warden;
        if (stunned || stun <= 0) {
            return;
        }
        stunned = true;
        c.damageTakenScale = WpConfig.SONIC_STUN_DAMAGE_TAKEN.get().floatValue();
        Vfx.anim(w, Vfx.ANIM_STUN, stun);
        Vfx.send(w, Vfx.STUN, stun);
        sound(w, SoundEvents.WARDEN_HURT, 3.0F, 0.5F);
        sound(w, SoundEvents.BEACON_DEACTIVATE, 3.0F, 0.6F);
    }

    @Override
    public void stop(WardenController c, boolean interrupted) {
        c.damageTakenScale = 1.0F;
        if (caged || interrupted) {
            caged = false;
            Warden w = c.warden;
            for (int tag : tags) {
                if (tag != 0) {
                    EnderFx.riftEnd(w, tag);
                }
            }
            if (frontTag != 0) {
                EnderFx.riftEnd(w, frontTag);
            }
        }
    }
}




