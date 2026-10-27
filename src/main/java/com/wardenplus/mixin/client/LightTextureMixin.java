package com.wardenplus.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.wardenplus.client.ClientDarkness;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightTexture.class)
public abstract class LightTextureMixin {
    @ModifyReturnValue(method = "getDarknessGamma", at = @At("RETURN"))
    private float wardenplus$darkness(float original) {
        return Math.max(original, ClientDarkness.factor(Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false)));
    }
}
