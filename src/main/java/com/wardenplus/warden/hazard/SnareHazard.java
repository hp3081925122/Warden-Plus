package com.wardenplus.warden.hazard;

import com.wardenplus.warden.Combat;
import com.wardenplus.warden.WardenController;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class SnareHazard implements Hazard {
    private final Vec3 pos;
    private final float damage;
    private final double radius;
    private int delay;

    public SnareHazard(Vec3 pos, int delay, double radius, float damage) {
        this.pos = pos;
        this.delay = delay;
        this.radius = radius;
        this.damage = damage;
    }

    @Override
    public boolean tick(WardenController controller) {
        if (--delay > 0) {
            return true;
        }
        Warden warden = controller.warden;
        warden.level().playSound(null, pos.x, pos.y, pos.z, SoundEvents.SCULK_BLOCK_BREAK, SoundSource.HOSTILE, 2.0F, 0.6F);
        warden.level().playSound(null, pos.x, pos.y, pos.z, SoundEvents.WARDEN_ATTACK_IMPACT, SoundSource.HOSTILE, 1.2F, 1.5F);
        AABB box = new AABB(pos.x - radius, pos.y - 0.5, pos.z - radius, pos.x + radius, pos.y + 3.5, pos.z + radius);
        for (LivingEntity e : Combat.victims(warden, box)) {
            double dx = e.getX() - pos.x;
            double dz = e.getZ() - pos.z;
            if (dx * dx + dz * dz > radius * radius) {
                continue;
            }
            e.hurt(Combat.physical(warden), damage); if (true) {
                e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 2), warden);
                Vec3 v = e.getDeltaMovement();
                e.setDeltaMovement(v.x * 0.1, Math.min(v.y, -0.25), v.z * 0.1);
                e.hurtMarked = true;
            }
        }
        return false;
    }
}




