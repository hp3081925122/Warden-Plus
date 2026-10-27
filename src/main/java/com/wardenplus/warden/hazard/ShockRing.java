package com.wardenplus.warden.hazard;

import com.wardenplus.warden.Combat;
import com.wardenplus.warden.WardenController;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ShockRing implements Hazard {
    private final Vec3 center;
    private final double speed;
    private final double maxRadius;
    private final float damage;
    private final double up;
    private final double push;
    private final IntSet hit = new IntOpenHashSet();
    private int delay;
    private double radius;

    public ShockRing(Vec3 center, double speed, double maxRadius, float damage, int delay, double push, double up) {
        this.center = center;
        this.speed = speed;
        this.maxRadius = maxRadius;
        this.damage = damage;
        this.delay = delay;
        this.push = push;
        this.up = up;
    }

    @Override
    public boolean tick(WardenController controller) {
        if (delay > 0) {
            delay--;
            return true;
        }
        Warden warden = controller.warden;
        double prev = radius;
        radius = Math.min(maxRadius, radius + speed);
        AABB box = new AABB(center.x - radius - 1, center.y - 2, center.z - radius - 1, center.x + radius + 1, center.y + 3, center.z + radius + 1);
        for (LivingEntity e : Combat.victims(warden, box)) {
            if (hit.contains(e.getId())) {
                continue;
            }
            double dx = e.getX() - center.x;
            double dz = e.getZ() - center.z;
            double d = Math.sqrt(dx * dx + dz * dz);
            if (d < prev - 0.9 || d > radius + 0.9) {
                continue;
            }
            double gy = Combat.groundY(warden.level(), e.getX(), e.getY(), e.getZ());
            if (Math.abs(gy - center.y) > 2.5 || !Combat.onGroundNear(e, gy)) {
                continue;
            }
            hit.add(e.getId());
            if (e.hurt(Combat.physical(warden), damage)) {
                Combat.knock(e, center.x, center.z, push, up);
            }
        }
        return radius < maxRadius;
    }
}
