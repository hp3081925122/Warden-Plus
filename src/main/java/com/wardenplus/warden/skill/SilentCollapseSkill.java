package com.wardenplus.warden.skill;

import com.wardenplus.network.Vfx;
import com.wardenplus.warden.Combat;
import com.wardenplus.warden.SkillType;
import com.wardenplus.warden.WardenController;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class SilentCollapseSkill extends WardenSkill {
    private static final int CHARGE = 64;
    private static final double RADIUS = 22.0;

    @Override
    public int duration() {
        return CHARGE + 30;
    }

    @Override
    public boolean needsTarget() {
        return false;
    }

    @Override
    public void start(WardenController c, LivingEntity target) {
        Warden w = c.warden;
        c.stopMoving();
        Vfx.anim(w, Vfx.ANIM_COLLAPSE, duration());
        Vfx.send(w, Vfx.COLLAPSE, CHARGE, (float) RADIUS);
        sound(w, SoundEvents.WARDEN_ROAR, 5.0F, 0.6F);
    }

    @Override
    public void tick(WardenController c, LivingEntity target, int t) {
        Warden w = c.warden;
        c.stopMoving();
        Vec3 core = w.position().add(0, 1.6, 0);
        if (t == 18) {
            sound(w, SoundEvents.PORTAL_TRIGGER, 3.0F, 0.5F);
        }
        if (t == 44) {
            sound(w, SoundEvents.BEACON_DEACTIVATE, 4.0F, 0.5F);
        }
        if (t < CHARGE - 10) {
            for (LivingEntity e : Combat.victims(w, w.getBoundingBox().inflate(RADIUS))) {
                double d = e.distanceTo(w);
                if (d > 3.0 && d < RADIUS) {
                    Combat.pull(e, w.position(), 0.055);
                }
            }
        }
        if (t != CHARGE) {
            return;
        }
        float damage = Combat.damage(w, SkillType.SILENT_COLLAPSE);
        AABB box = new AABB(core, core).inflate(RADIUS);
        for (LivingEntity e : Combat.victims(w, box)) {
            if (e.distanceToSqr(core) > RADIUS * RADIUS) {
                continue;
            }
            float scale = 1.0F;
            if (e.isShiftKeyDown() || e.isCrouching()) {
                scale *= 0.35F;
            }
            if (Combat.blocked(w.level(), core, e.getEyePosition(), w)) {
                scale *= 0.35F;
            }
            scale = Math.max(scale, 0.15F);
            if (e.hurt(Combat.echo(w), damage * scale)) {
                Combat.knock(e, w.getX(), w.getZ(), 1.0 * scale + 0.3, 0.4);
                e.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0), w);
            }
        }
        Vfx.send(w, Vfx.BLAST, 26, Vfx.pos(core, (float) RADIUS));
        sound(w, SoundEvents.GENERIC_EXPLODE.value(), 5.0F, 0.45F);
        sound(w, SoundEvents.WARDEN_ATTACK_IMPACT, 5.0F, 0.5F);
        sound(w, SoundEvents.WARDEN_SONIC_BOOM, 2.0F, 0.5F);
    }
}
