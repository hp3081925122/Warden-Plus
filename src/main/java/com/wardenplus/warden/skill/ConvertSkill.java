package com.wardenplus.warden.skill;

import com.wardenplus.network.Vfx;
import com.wardenplus.warden.Combat;
import com.wardenplus.warden.WardenController;
import com.wardenplus.warden.WardenVariant;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.warden.AngerLevel;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public final class ConvertSkill extends WardenSkill {
    public static final int BURST = 40;
    private final WardenVariant variant;
    private final Player converter;

    public ConvertSkill(WardenVariant variant, Player converter) {
        this.variant = variant;
        this.converter = converter;
    }

    @Override
    public int duration() {
        return BURST + 20;
    }

    @Override
    public boolean needsTarget() {
        return false;
    }

    @Override
    public void start(WardenController c, LivingEntity target) {
        Warden w = c.warden;
        c.stopMoving();
        c.invulnerable = true;
        w.setPose(Pose.ROARING);
        int palette = variant == WardenVariant.ENDER ? Vfx.PALETTE_ENDER : Vfx.PALETTE_ECHO;
        Vfx.anim(w, Vfx.ANIM_CONVERT, duration());
        Vfx.send(w, Vfx.CONVERT, duration(), palette, BURST);
        sound(w, variant == WardenVariant.ENDER ? SoundEvents.END_PORTAL_FRAME_FILL : SoundEvents.SCULK_SHRIEKER_SHRIEK, 4.0F, 0.6F);
        sound(w, SoundEvents.WARDEN_HEARTBEAT, 6.0F, 0.6F);
    }

    @Override
    public void tick(WardenController c, LivingEntity target, int t) {
        Warden w = c.warden;
        c.stopMoving();
        if (t % 10 == 0 && t < BURST) {
            sound(w, SoundEvents.WARDEN_HEARTBEAT, 5.0F, 0.6F + t * 0.01F);
        }
        if (t != BURST) {
            return;
        }
        for (LivingEntity e : Combat.victims(w, new AABB(w.blockPosition()).inflate(6.0))) {
            if (e.distanceToSqr(w) <= 36.0) {
                Combat.knock(e, w.getX(), w.getZ(), 0.8, 0.3);
            }
        }
        sound(w, SoundEvents.WARDEN_ROAR, 5.0F, variant == WardenVariant.ENDER ? 0.7F : 0.9F);
        sound(w, variant == WardenVariant.ENDER ? SoundEvents.END_PORTAL_SPAWN : SoundEvents.GENERIC_EXPLODE, 2.0F, 0.8F);
    }

    @Override
    public void stop(WardenController c, boolean interrupted) {
        Warden w = c.warden;
        c.invulnerable = false;
        if (w.hasPose(Pose.ROARING)) {
            w.setPose(Pose.STANDING);
        }
        if (converter != null && converter.isAlive() && !converter.isSpectator() && w.canTargetEntity(converter)) {
            w.increaseAngerAt(converter, AngerLevel.ANGRY.getMinimumAnger() + 20, false);
        }
    }
}
