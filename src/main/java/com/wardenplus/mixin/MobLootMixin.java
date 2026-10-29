package com.wardenplus.mixin;

import com.wardenplus.WpConfig;
import com.wardenplus.warden.WardenVariant;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobLootMixin {
    @Inject(method = "getLootTable", at = @At("HEAD"), cancellable = true)
    private void wardenplus$enderLootTable(CallbackInfoReturnable<java.util.Optional<ResourceKey<LootTable>>> cir) {
        if (!((Object) this instanceof Warden warden) || WardenVariant.of(warden) != WardenVariant.ENDER) {
            return;
        }
        String value = WpConfig.ENDER_LOOT_TABLE.get();
        int separator = value.indexOf(':');
        String namespace = separator > 0 ? value.substring(0, separator) : "minecraft";
        String path = separator > 0 ? value.substring(separator + 1) : value;
        try {
            cir.setReturnValue(java.util.Optional.of(ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(namespace, path))));
        } catch (RuntimeException ignored) {
        }
    }
}
