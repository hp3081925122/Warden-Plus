package com.wardenplus.warden.skill;

import com.wardenplus.warden.WardenController;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.Vec3;

public abstract class WardenSkill {
    public abstract int duration();

    public void start(WardenController c, LivingEntity target) {
    }

    public abstract void tick(WardenController c, LivingEntity target, int t);

    public void stop(WardenController c, boolean interrupted) {
    }

    public boolean needsTarget() {
        return true;
    }

    public boolean locksBrain() {
        return true;
    }

    protected static void sound(Warden warden, SoundEvent sound, float volume, float pitch) {
        warden.level().playSound(null, warden.getX(), warden.getY(), warden.getZ(), sound, SoundSource.HOSTILE, volume, pitch);
    }

    protected static void sound(Warden warden, Vec3 pos, SoundEvent sound, float volume, float pitch) {
        warden.level().playSound(null, pos.x, pos.y, pos.z, sound, SoundSource.HOSTILE, volume, pitch);
    }

    protected static Vec3 forward(Warden warden, double dist) {
        float yaw = warden.getYRot() * ((float) Math.PI / 180F);
        return new Vec3(-Math.sin(yaw) * dist, 0.0, Math.cos(yaw) * dist);
    }
}




