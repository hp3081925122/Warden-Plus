package com.wardenplus.mixin.client;

import com.wardenplus.client.anim.WardenAnimator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.warden.WardenModel;
import net.minecraft.client.renderer.entity.state.WardenRenderState;
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
 @Shadow @Final protected ModelPart body; @Shadow @Final protected ModelPart head; @Shadow @Final protected ModelPart leftArm; @Shadow @Final protected ModelPart rightArm;
 @Shadow @Final protected ModelPart leftRibcage; @Shadow @Final protected ModelPart rightRibcage; @Shadow @Final protected ModelPart leftTendril; @Shadow @Final protected ModelPart rightTendril;
 @Unique private WardenAnimator.Parts wardenplus$parts;
 @Inject(method="setupAnim(Lnet/minecraft/client/renderer/entity/state/WardenRenderState;)V",at=@At("TAIL"))
 private void wardenplus$animate(WardenRenderState state, CallbackInfo ci){
  Warden w=find(state); if(w==null)return;
  if(wardenplus$parts==null){WardenAnimator.Parts p=new WardenAnimator.Parts();p.body=body;p.head=head;p.leftArm=leftArm;p.rightArm=rightArm;p.leftRib=leftRibcage;p.rightRib=rightRibcage;p.leftTendril=leftTendril;p.rightTendril=rightTendril;wardenplus$parts=p;}
  WardenAnimator.apply(w,state.ageInTicks,wardenplus$parts);
 }
 @Unique private static Warden find(WardenRenderState s){if(Minecraft.getInstance().level==null)return null;return Minecraft.getInstance().level.getEntitiesOfClass(Warden.class,new net.minecraft.world.phys.AABB(s.x-1,s.y-1,s.z-1,s.x+1,s.y+3,s.z+1)).stream().min((a,b)->Double.compare(a.distanceToSqr(s.x,s.y,s.z),b.distanceToSqr(s.x,s.y,s.z))).orElse(null);}
}
