package com.wardenplus.warden.skill;

import com.wardenplus.network.Vfx;
import com.wardenplus.warden.Combat;
import com.wardenplus.warden.SkillType;
import com.wardenplus.warden.WardenController;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class HeartbeatDomainSkill extends WardenSkill {
    private static final int FORM = 14;
    private static final double RADIUS = 15.0;
    private final Int2ObjectOpenHashMap<Vec3> lastPos = new Int2ObjectOpenHashMap<>();
    private Vec3 center;
    private int beats;
    private int interval;

    @Override
    public int duration() {
        return FORM + beats * interval + 12;
    }

    @Override
    public boolean needsTarget() {
        return false;
    }

    @Override
    public void start(WardenController c, LivingEntity target) {
        Warden w = c.warden;
        c.stopMoving();
        beats = c.stage() >= 3 ? 7 : 6;
        interval = c.stage() >= 3 ? 16 : 20;
        center = w.position();
        Vfx.anim(w, Vfx.ANIM_DOMAIN, duration());
        Vfx.send(w, Vfx.DOMAIN, duration(), Vfx.pos(center, (float) RADIUS, interval, beats, FORM));
        sound(w, SoundEvents.BEACON_ACTIVATE, 4.0F, 0.5F);
        sound(w, SoundEvents.WARDEN_LISTENING_ANGRY, 4.0F, 0.8F);
    }

    @Override
    public void tick(WardenController c, LivingEntity target, int t) {
        Warden w = c.warden;
        c.stopMoving();
        if (target != null) {
            c.face(target.position());
        }
        if (t == FORM - 1) {
            for (LivingEntity e : inside(w)) {
                lastPos.put(e.getId(), e.position());
            }
        }
        if (t < FORM || (t - FORM) % interval != interval - 1 || t >= FORM + beats * interval) {
            return;
        }
        sound(w, SoundEvents.WARDEN_HEARTBEAT, 6.0F, 0.7F);
        float damage = Combat.damage(w, SkillType.HEARTBEAT_DOMAIN);
        for (LivingEntity e : inside(w)) {
            Vec3 prev = lastPos.get(e.getId());
            Vec3 now = e.position();
            lastPos.put(e.getId(), now);
            if (prev == null) {
                continue;
            }
            double moved = Math.sqrt(Math.pow(now.x - prev.x, 2) + Math.pow(now.z - prev.z, 2)) + Math.abs(now.y - prev.y) * 0.5;
            boolean silent = e.isShiftKeyDown() || e.isCrouching();
            if (moved < 0.5 || silent) {
                continue;
            }
            Vfx.send(w, Vfx.HEARD, 16, e.getId());
            e.hurt(Combat.echo(w), damage); if (true) {
                e.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0), w);
            }
        }
    }

    private java.util.List<LivingEntity> inside(Warden w) {
        AABB box = new AABB(center, center).inflate(RADIUS);
        return Combat.victims(w, box).stream().filter(e -> e.distanceToSqr(center) <= RADIUS * RADIUS).toList();
    }
}




