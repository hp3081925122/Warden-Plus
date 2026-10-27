package com.wardenplus.warden.hazard;

import com.wardenplus.warden.Combat;
import com.wardenplus.warden.WardenController;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class SonicBeam implements Hazard {
    private final Vec3 from;
    private final Vec3 to;
    private final float damage;
    private final double width;
    private int delay;

    public SonicBeam(Vec3 from, Vec3 to, int delay, float damage, double width) {
        this.from = from;
        this.to = to;
        this.delay = delay;
        this.damage = damage;
        this.width = width;
    }

    @Override
    public boolean tick(WardenController controller) {
        if (--delay > 0) {
            return true;
        }
        Warden warden = controller.warden;
        warden.level().playSound(null, from.x, from.y, from.z, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 3.0F, 1.0F);
        Vec3 dir = to.subtract(from).normalize();
        for (LivingEntity e : Combat.victims(warden, new AABB(from, to).inflate(width + 1.0))) {
            Vec3 c = e.position().add(0, e.getBbHeight() * 0.5, 0);
            if (Combat.segmentDistance(c, from, to) > width + e.getBbWidth() * 0.5) {
                continue;
            }
            if (e.hurt(warden.damageSources().sonicBoom(warden), damage)) {
                Combat.knock(e, e.getX() - dir.x, e.getZ() - dir.z, 0.8, 0.25);
            }
        }
        return false;
    }
}
