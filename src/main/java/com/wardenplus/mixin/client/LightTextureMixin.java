package com.wardenplus.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.wardenplus.client.ClientDarkness;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.client.renderer.LightmapRenderStateExtractor")
public abstract class LightTextureMixin {
    @ModifyExpressionValue(method = "extract", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getEffectBlendFactor(Lnet/minecraft/core/Holder;F)F"))
    private float wardenplus$darkness(float original, @Local(argsOnly = true) float partialTicks) {
        return Math.max(original, ClientDarkness.factor(partialTicks));
    }
}
