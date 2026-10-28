package com.wardenplus.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.wardenplus.warden.WardenControllers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.monster.warden.Warden;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Warden.class)
public abstract class WardenMixin {
    @WrapWithCondition(method = "customServerAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/Brain;tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)V"))
    private boolean wardenplus$lockBrain(Brain<?> brain, ServerLevel level, LivingEntity entity) {
        return !WardenControllers.isBrainLocked((Warden) (Object) this);
    }
}




