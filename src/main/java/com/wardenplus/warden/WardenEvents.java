package com.wardenplus.warden;

import com.wardenplus.WardenPlus;
import com.wardenplus.WpConfig;
import com.wardenplus.client.ClientWardenState;
import com.wardenplus.network.Vfx;
import com.wardenplus.network.VfxPayload;
import com.wardenplus.network.WpNetwork;
import com.wardenplus.warden.skill.ConvertSkill;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.network.PacketDistributor;

@EventBusSubscriber(modid = WardenPlus.MOD_ID)
public final class WardenEvents {
    private static final UUID HEALTH_ID = UUID.fromString("7c9a7f82-4ec6-4f20-8ac4-1d1e0c4a4b10");
    private static final UUID ATTACK_ID = UUID.fromString("3b41cc8e-76ba-4ca0-8e0b-4c0f1e1d9f3b");

    private WardenEvents() {
    }

    @SubscribeEvent
    public static void onTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Warden warden) || warden.level().isClientSide()) {
            return;
        }
        if (WardenVariant.of(warden) == WardenVariant.NONE) {
            return;
        }
        if (warden.tickCount % 100 == 0) {
            warden.getBrain().setMemoryWithExpiry(MemoryModuleType.DIG_COOLDOWN, Unit.INSTANCE, 1200L);
        }
        WardenController c = WardenControllers.get(warden);
        if (c != null) {
            c.tick();
        }
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Warden warden) || event.getLevel().isClientSide()) {
            return;
        }
        if (warden.isInvisible() && !warden.hasEffect(MobEffects.INVISIBILITY)) {
            warden.setInvisible(false);
            warden.setNoGravity(false);
            double gy = Combat.groundY(warden.level(), warden.getX(), warden.getY() + 3.2, warden.getZ());
            warden.setPos(warden.getX(), Math.max(gy, warden.getY()), warden.getZ());
        }
        if (WardenVariant.of(warden) == WardenVariant.NONE) {
            AttributeInstance health = warden.getAttribute(Attributes.MAX_HEALTH);
            if (health != null && health.getModifier(HEALTH_ID) != null) {
                health.removeModifier(HEALTH_ID);
                warden.setHealth(Math.min(warden.getHealth(), warden.getMaxHealth()));
            }
        }
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Warden warden) || !WpConfig.CONVERSION_ENABLED.get()) {
            return;
        }
        ItemStack stack = event.getItemStack();
        WardenVariant variant = conversionVariant(stack);
        if (variant == WardenVariant.NONE || !warden.isAlive()) {
            return;
        }
        Player player = event.getEntity();
        if (event.getLevel().isClientSide()) {
            if (ClientWardenState.variant(warden.getId()) == 0) {
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
            return;
        }
        if (WardenVariant.of(warden) != WardenVariant.NONE) {
            return;
        }
        WardenVariant.set(warden, variant);
        applyAttributes(warden, variant);
        warden.setPersistenceRequired();
        if (WpConfig.CONSUME_ITEM.get() && !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.swing(event.getHand(), true);
        Vfx.send(warden, Vfx.VARIANT, 0, variant.ordinal());
        WardenController c = WardenControllers.get(warden);
        if (c != null) {
            c.queue(new ConvertSkill(variant, player));
        }
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    private static WardenVariant conversionVariant(ItemStack stack) {
        if (stack.isEmpty()) {
            return WardenVariant.NONE;
        }
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (WpConfig.ECHO_ENABLED.get() && id.equals(WpConfig.ECHO_ITEM.get())) {
            return WardenVariant.ECHO;
        }
        if (id.equals(WpConfig.ENDER_ITEM.get())) {
            return WardenVariant.ENDER;
        }
        return WardenVariant.NONE;
    }

    private static void applyAttributes(Warden warden, WardenVariant variant) {
        CompoundTag tag = warden.getPersistentData();
        if (tag.getBoolean(WardenVariant.ATTR_KEY)) {
            return;
        }
        tag.putBoolean(WardenVariant.ATTR_KEY, true);
        double healthMult = variant == WardenVariant.ENDER ? WpConfig.ENDER_HEALTH_MULTIPLIER.get() : WpConfig.HEALTH_MULTIPLIER.get();
        AttributeInstance health = warden.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.removeModifier(HEALTH_ID);
            if (Math.abs(healthMult - 1.0) > 1.0E-6) {
                health.addPermanentModifier(new AttributeModifier(HEALTH_ID, "wardenplus_health_bonus", healthMult - 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
            warden.setHealth(warden.getMaxHealth());
        }
        if (variant == WardenVariant.ENDER) {
            double attackMult = WpConfig.ENDER_ATTACK_MULTIPLIER.get();
            AttributeInstance attack = warden.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attack != null && Math.abs(attackMult - 1.0) > 1.0E-6) {
                attack.removeModifier(ATTACK_ID);
                attack.addPermanentModifier(new AttributeModifier(ATTACK_ID, "wardenplus_attack_bonus", attackMult - 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
    }

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof Warden warden && !event.getLevel().isClientSide()) {
            WardenControllers.remove(warden);
        }
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof Warden warden) || warden.level().isClientSide()) {
            return;
        }
        WardenController c = WardenControllers.peek(warden);
        if (c == null) {
            return;
        }
        if (c.isInvulnerable() && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            event.setCanceled(true);
            return;
        }
        if (tryDodge(c, event.getSource())) {
            event.setCanceled(true);
            return;
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Warden warden) || warden.level().isClientSide()) {
            return;
        }
        WardenController c = WardenControllers.peek(warden);
        if (c != null && c.damageTakenScale != 1.0F) {
            event.setAmount(event.getAmount() * c.damageTakenScale);
        }
    }

    private static boolean tryDodge(WardenController c, DamageSource source) {
        if (c.variant != WardenVariant.ENDER || c.busy() || c.dodgeCooldown > 0 || !source.is(DamageTypeTags.IS_PROJECTILE)) {
            return false;
        }
        Warden warden = c.warden;
        if (warden.getRandom().nextDouble() >= WpConfig.DODGE_CHANCE.get()) {
            return false;
        }
        if (!EnderOps.randomTeleport(warden, warden, 3.0, 7.0)) {
            return false;
        }
        c.dodgeCooldown = WpConfig.DODGE_COOLDOWN.get();
        return true;
    }

    @SubscribeEvent
    public static void onDamaged(LivingDamageEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof Warden warden) || source.getDirectEntity() != warden || warden.level().isClientSide()) {
            return;
        }
        if (!source.is(DamageTypes.MOB_ATTACK) || event.getAmount() <= 0) {
            return;
        }
        WardenController c = WardenControllers.peek(warden);
        if (c == null || c.variant != WardenVariant.ENDER || c.busy() || c.hazardsActive() || c.touchCooldown > 0) {
            return;
        }
        LivingEntity victim = event.getEntity();
        if (!victim.isAlive() || victim.isPassenger() || victim.getHealth() <= event.getAmount() || warden.getRandom().nextDouble() >= WpConfig.TOUCH_CHANCE.get()) {
            return;
        }
        double min = WpConfig.TOUCH_MIN_DISTANCE.get();
        double max = Math.max(min, WpConfig.TOUCH_MAX_DISTANCE.get());
        if (EnderOps.randomTeleport(warden, victim, min, max)) {
            c.touchCooldown = WpConfig.TOUCH_COOLDOWN.get();
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getTarget() instanceof Warden warden) || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        WardenVariant variant = WardenVariant.of(warden);
        if (variant == WardenVariant.NONE) {
            return;
        }
        WpNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new VfxPayload(Vfx.VARIANT, warden.getId(), 0, new float[]{variant.ordinal()}));
        WardenController c = WardenControllers.get(warden);
        if (c != null && c.stage() > 1) {
            WpNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new VfxPayload(Vfx.STAGE, warden.getId(), 0, new float[]{c.stage()}));
        }
    }
}
