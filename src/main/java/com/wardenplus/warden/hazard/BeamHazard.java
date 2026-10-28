package com.wardenplus.warden.hazard;

import com.wardenplus.warden.Combat;
import com.wardenplus.warden.WardenController;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BeamHazard implements Hazard {
    private final Vec3 from;
    private final Vec3 to;
    private final float damage;
    private int delay;

    public BeamHazard(Vec3 from, Vec3 to, int delay, float damage) {
        this.from = from;
        this.to = to;
        this.delay = delay;
        this.damage = damage;
    }

    @Override
    public boolean tick(WardenController controller) {
        if (--delay > 0) {
            return true;
        }
        Warden warden = controller.warden;
        warden.level().playSound(null, from.x, from.y, from.z, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 3.0F, 1.7F);
        warden.level().playSound(null, to.x, to.y, to.z, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.HOSTILE, 2.0F, 0.7F);
        AABB box = new AABB(from, to).inflate(1.2);
        Vec3 dir = to.subtract(from).normalize();
        for (LivingEntity e : Combat.victims(warden, box)) {
            Vec3 c = e.position().add(0, e.getBbHeight() * 0.5, 0);
            if (Combat.segmentDistance(c, from, to) > 0.7 + e.getBbWidth() * 0.5) {
                continue;
            }
            e.hurt(Combat.echo(warden), damage); if (true) {
                Combat.knock(e, e.getX() - dir.x, e.getZ() - dir.z, 0.35, 0.1);
            }
        }
        return false;
    }
}




