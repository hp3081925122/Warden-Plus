package com.wardenplus.warden;

import com.mojang.logging.LogUtils;
import com.wardenplus.WpConfig;
import com.wardenplus.network.Vfx;
import com.wardenplus.network.VfxPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;

public final class EnderOps {
    private static final Logger LOGGER = LogUtils.getLogger();

    private EnderOps() {
    }

    public static Vec3 findSpot(Entity e, Vec3 center, double minR, double maxR, int attempts) {
        Level level = e.level();
        RandomSource random = e.getRandom();
        for (int i = 0; i < attempts; i++) {
            double a = random.nextDouble() * Math.PI * 2;
            double r = minR + random.nextDouble() * Math.max(0, maxR - minR);
            Vec3 spot = standAt(e, center.x + Math.cos(a) * r, center.y, center.z + Math.sin(a) * r);
            if (spot != null) {
                return spot;
            }
        }
        return null;
    }

    public static Vec3 standAt(Entity e, double x, double y, double z) {
        Level level = e.level();
        double gy = Combat.groundY(level, x, y + 4.0, z);
        if (Math.abs(gy - y) > 8.0) {
            gy = Combat.groundY(level, x, y, z);
            if (Math.abs(gy - y) > 8.0) {
                return null;
            }
        }
        return fits(e, x, gy, z) ? new Vec3(x, gy, z) : null;
    }

    public static boolean fits(Entity e, double x, double y, double z) {
        Level level = e.level();
        float hw = e.getBbWidth() / 2.0F;
        AABB box = new AABB(x - hw, y + 0.01, z - hw, x + hw, y + e.getBbHeight(), z + hw);
        if (!level.noCollision(e, box) || level.containsAnyLiquid(box)) {
            return false;
        }
        AABB floor = new AABB(x - hw, y - 0.5, z - hw, x + hw, y - 0.01, z + hw);
        return !level.noCollision(e, floor);
    }

    public static double clearance(Entity e, Vec3 ground, double max) {
        Level level = e.level();
        float hw = Math.max(e.getBbWidth(), 0.9F) / 2.0F;
        double h = 0;
        while (h < max) {
            AABB slice = new AABB(ground.x - hw, ground.y + h + 0.01, ground.z - hw, ground.x + hw, ground.y + h + 1.0, ground.z + hw);
            if (!level.noCollision(e, slice) || level.containsAnyLiquid(slice)) {
                break;
            }
            h += 1.0;
        }
        return h;
    }

    public static Vec3 center(Entity e) {
        return e.position().add(0, e.getBbHeight() * 0.5, 0);
    }

    public static void teleport(Warden source, Entity e, Vec3 to) {
        Vec3 from = center(e);
        Vec3 fromFeet = e.position();
        e.teleportTo(to.x, to.y, to.z);
        e.resetFallDistance();
        e.level().gameEvent(GameEvent.TELEPORT, fromFeet, GameEvent.Context.of(e));
        Vec3 dest = to.add(0, e.getBbHeight() * 0.5, 0);
        Vfx.send(source, Vfx.ENDER_TRAIL, 30, (float) from.x, (float) from.y, (float) from.z, (float) dest.x, (float) dest.y, (float) dest.z, e.getBbHeight(), e.getId());
        float pitch = e == source ? 0.55F : 1.0F;
        e.level().playSound(null, fromFeet.x, fromFeet.y, fromFeet.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 2.0F, pitch);
        e.level().playSound(null, to.x, to.y, to.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 2.0F, pitch);
    }

    public static boolean randomTeleport(Warden source, LivingEntity e, double minR, double maxR) {
        Vec3 spot = findSpot(e, e.position(), minR, maxR, 24);
        if (spot == null) {
            return false;
        }
        teleport(source, e, spot);
        return true;
    }

    public static int darkness(Warden source, double radius, int ticks) {
        if (!(source.level() instanceof ServerLevel level) || radius <= 0) {
            return 0;
        }
        int count = 0;
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || player.distanceToSqr(source) > radius * radius) {
                continue;
            }
            PacketDistributor.sendToPlayer(player, new VfxPayload(Vfx.DARKNESS, source.getId(), ticks, new float[]{WpConfig.DARKNESS_VISIBILITY.get().floatValue()}));
            count++;
        }
        return count;
    }

    public static void logDarkness(Warden source, double radius, int count) {
        LOGGER.debug("[WardenPlus] Rift sonic darkness sent by warden {} to {} player(s) within {} blocks", source.getId(), count, radius);
    }

    public static Vec3 behind(LivingEntity target, Vec3 from, double dist) {
        Vec3 dir = target.position().subtract(from).multiply(1, 0, 1);
        if (dir.lengthSqr() < 1.0E-4) {
            float yaw = target.getYRot() * Mth.DEG_TO_RAD;
            dir = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
        }
        return target.position().add(dir.normalize().scale(dist));
    }
}
