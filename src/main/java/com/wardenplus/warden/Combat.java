package com.wardenplus.warden;

import com.wardenplus.WardenPlus;
import com.wardenplus.WpConfig;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class Combat {
    public static final ResourceKey<DamageType> ECHO = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(WardenPlus.MOD_ID, "echo"));

    private Combat() {
    }

    public static float damage(Warden warden, SkillType type) {
        double ratio = WpConfig.skill(type).damage().get();
        return (float) (warden.getAttributeValue(Attributes.ATTACK_DAMAGE) * ratio * WpConfig.SKILL_DAMAGE_MULTIPLIER.get());
    }

    public static DamageSource echo(Warden warden) {
        return new DamageSource(warden.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(ECHO), warden);
    }

    public static DamageSource physical(Warden warden) {
        return warden.damageSources().mobAttack(warden);
    }

    public static List<LivingEntity> victims(Warden warden, AABB box) {
        return warden.level().getEntitiesOfClass(LivingEntity.class, box, e -> e != warden && e.isAlive() && warden.canTargetEntity(e));
    }

    public static void knock(LivingEntity target, double fromX, double fromZ, double horizontal, double up) {
        double scale = WpConfig.KNOCKBACK_SCALE.get();
        double resist = Mth.clamp(1.0 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.0, 1.0);
        double dx = fromX - target.getX();
        double dz = fromZ - target.getZ();
        if (dx * dx + dz * dz < 1.0E-4) {
            dx = target.getRandom().nextDouble() - 0.5;
            dz = target.getRandom().nextDouble() - 0.5;
        }
        if (horizontal > 0) {
            target.knockback(horizontal * scale, dx, dz);
        }
        if (up > 0) {
            target.setDeltaMovement(target.getDeltaMovement().add(0.0, up * scale * resist, 0.0));
        }
        target.hurtMarked = true;
    }

    public static void pull(LivingEntity target, Vec3 center, double strength) {
        double resist = Mth.clamp(1.0 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.0, 1.0);
        Vec3 dir = center.subtract(target.position()).multiply(1.0, 0.0, 1.0);
        if (dir.lengthSqr() < 1.0E-4) {
            return;
        }
        Vec3 add = dir.normalize().scale(strength * resist * WpConfig.KNOCKBACK_SCALE.get());
        target.setDeltaMovement(target.getDeltaMovement().add(add));
        target.hurtMarked = true;
    }

    public static double groundY(Level level, double x, double y, double z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(Mth.floor(x), Mth.floor(y + 0.5), Mth.floor(z));
        if (solidTop(level, pos) != null) {
            for (int i = 0; i < 6; i++) {
                pos.move(Direction.UP);
                if (solidTop(level, pos) == null) {
                    pos.move(Direction.DOWN);
                    Double top = solidTop(level, pos);
                    return top == null ? y : top;
                }
            }
            return y;
        }
        for (int i = 0; i < 12; i++) {
            pos.move(Direction.DOWN);
            Double top = solidTop(level, pos);
            if (top != null) {
                return top;
            }
        }
        return y;
    }

    private static Double solidTop(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        VoxelShape shape = state.getCollisionShape(level, pos);
        if (shape.isEmpty()) {
            return null;
        }
        return pos.getY() + shape.max(Direction.Axis.Y);
    }

    public static boolean onGroundNear(LivingEntity e, double groundY) {
        return e.onGround() || e.getY() - groundY < 0.35;
    }

    public static boolean blocked(Level level, Vec3 from, Vec3 to, LivingEntity viewer) {
        return level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, viewer)).getType() != HitResult.Type.MISS;
    }

    public static Vec3 clipBlocks(Level level, Vec3 from, Vec3 to, LivingEntity viewer) {
        HitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, viewer));
        return hit.getType() == HitResult.Type.MISS ? to : hit.getLocation();
    }

    public static double segmentDistance(Vec3 p, Vec3 a, Vec3 b) {
        Vec3 ab = b.subtract(a);
        double len = ab.lengthSqr();
        if (len < 1.0E-6) {
            return p.distanceTo(a);
        }
        double t = Mth.clamp(p.subtract(a).dot(ab) / len, 0.0, 1.0);
        return p.distanceTo(a.add(ab.scale(t)));
    }
}





