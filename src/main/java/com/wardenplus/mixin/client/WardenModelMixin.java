package com.wardenplus.mixin.client;

import com.wardenplus.client.anim.WardenAnimator;
import net.minecraft.client.model.WardenModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.warden.Warden;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WardenModel.class)
public abstract class WardenModelMixin {
    @Shadow @Final protected ModelPart body;
    @Shadow @Final protected ModelPart head;
    @Shadow @Final protected ModelPart leftArm;
    @Shadow @Final protected ModelPart rightArm;
    @Shadow @Final protected ModelPart leftRibcage;
    @Shadow @Final protected ModelPart rightRibcage;
    @Shadow @Final protected ModelPart leftTendril;
    @Shadow @Final protected ModelPart rightTendril;
    @Unique
    private WardenAnimator.Parts wardenplus$parts;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/monster/warden/Warden;FFFFF)V", at = @At("TAIL"))
    private void wardenplus$animate(Warden warden, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (wardenplus$parts == null) {
            WardenAnimator.Parts p = new WardenAnimator.Parts();
            p.body = body;
            p.head = head;
            p.leftArm = leftArm;
            p.rightArm = rightArm;
            p.leftRib = leftRibcage;
            p.rightRib = rightRibcage;
            p.leftTendril = leftTendril;
            p.rightTendril = rightTendril;
            wardenplus$parts = p;
        }
        WardenAnimator.apply(warden, ageInTicks, wardenplus$parts);
    }
}
