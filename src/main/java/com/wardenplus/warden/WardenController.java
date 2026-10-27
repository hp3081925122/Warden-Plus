package com.wardenplus.warden;

import com.wardenplus.WpConfig;
import com.wardenplus.network.Vfx;
import com.wardenplus.warden.hazard.Hazard;
import com.wardenplus.warden.skill.WardenSkill;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.warden.SonicBoom;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.Vec3;

public final class WardenController {
    public final Warden warden;
    public final WardenVariant variant;
    private final Map<SkillType, Integer> cooldowns = new EnumMap<>(SkillType.class);
    private final List<Hazard> hazards = new ArrayList<>();
    private final List<Hazard> pendingHazards = new ArrayList<>();
    private int stage = 1;
    private SkillType pendingForced;
    private WardenSkill pendingCustom;
    private WardenSkill active;
    private SkillType activeType;
    private int activeTick;
    private int globalCooldown = 40;
    private int combatTimer;
    private ServerBossEvent bossBar;
    public boolean invulnerable;
    private int guardTicks;
    public float damageTakenScale = 1.0F;
    public Vec3 lastTargetPos;
    public int touchCooldown;
    public int dodgeCooldown;

    public WardenController(Warden warden) {
        this.warden = warden;
        this.variant = WardenVariant.of(warden);
        this.stage = computeStage();
        if (stage >= 2) {
            cooldowns.put(variant == WardenVariant.ENDER ? SkillType.RIFT_SONIC : SkillType.SILENT_COLLAPSE, stage >= 3 ? 200 : 0);
        }
    }

    public boolean busy() {
        return active != null;
    }

    public boolean hazardsActive() {
        return !hazards.isEmpty() || !pendingHazards.isEmpty();
    }

    public boolean isInvulnerable() {
        return invulnerable || guardTicks > 0 || pendingCustom != null || pendingForced != null && WpConfig.TRANSITION_INVULNERABLE_TICKS.get() > 0;
    }

    private void guard(int ticks) {
        guardTicks = Math.max(guardTicks, ticks);
    }

    public void queue(WardenSkill skill) {
        pendingCustom = skill;
    }

    public int stage() {
        return stage;
    }

    public boolean brainLocked() {
        return active != null && active.locksBrain();
    }

    public void addHazard(Hazard hazard) {
        pendingHazards.add(hazard);
    }

    private int computeStage() {
        float ratio = warden.getHealth() / Math.max(1.0F, warden.getMaxHealth());
        if (ratio <= WpConfig.STAGE_THREE_THRESHOLD.get()) {
            return 3;
        }
        if (ratio <= WpConfig.STAGE_TWO_THRESHOLD.get()) {
            return 2;
        }
        return 1;
    }

    public void tick() {
        if (!warden.isAlive()) {
            return;
        }
        updateStage();
        LivingEntity target = currentTarget();
        if (target != null) {
            lastTargetPos = target.position();
            combatTimer = 200;
        } else if (combatTimer > 0) {
            combatTimer--;
        }
        cooldowns.replaceAll((k, v) -> Math.max(0, v - 1));
        if (touchCooldown > 0) {
            touchCooldown--;
        }
        if (dodgeCooldown > 0) {
            dodgeCooldown--;
        }
        if (guardTicks > 0) {
            guardTicks--;
        }
        if (globalCooldown > 0 && active == null) {
            globalCooldown--;
        }
        if (active != null) {
            tickActive(target);
        } else {
            trySelect(target);
        }
        if (!pendingHazards.isEmpty()) {
            hazards.addAll(pendingHazards);
            pendingHazards.clear();
        }
        hazards.removeIf(h -> !h.tick(this));
        updateBossBar();
    }

    private void updateStage() {
        int now = computeStage();
        if (now <= stage) {
            return;
        }
        int old = stage;
        stage = now;
        Vfx.send(warden, Vfx.STAGE, 0, stage);
        guard(WpConfig.TRANSITION_INVULNERABLE_TICKS.get());
        if (variant == WardenVariant.ENDER) {
            if (WpConfig.skill(SkillType.RIFT_SONIC).enabled().get()) {
                cooldowns.put(SkillType.RIFT_SONIC, 0);
                if (pendingForced == null) {
                    pendingForced = SkillType.RIFT_SONIC;
                }
            }
            return;
        }
        if (old < 2 && WpConfig.skill(SkillType.AWAKEN).enabled().get()) {
            pendingForced = SkillType.AWAKEN;
        }
        if (stage >= 3 && WpConfig.skill(SkillType.SILENT_COLLAPSE).enabled().get()) {
            cooldowns.put(SkillType.SILENT_COLLAPSE, 0);
            if (pendingForced == null) {
                pendingForced = SkillType.SILENT_COLLAPSE;
            }
        }
    }

    public LivingEntity currentTarget() {
        LivingEntity target = warden.getTarget();
        if (target == null || !target.isAlive() || !warden.canTargetEntity(target)) {
            return null;
        }
        return target;
    }

    private void tickActive(LivingEntity target) {
        if (active.needsTarget() && target == null && lastTargetPos == null) {
            finish(true);
            return;
        }
        active.tick(this, target, activeTick);
        activeTick++;
        if (activeTick >= active.duration()) {
            finish(false);
        }
    }

    private void trySelect(LivingEntity target) {
        if (!WpConfig.ENABLED.get() || !warden.onGround() && !warden.isInWater()) {
            return;
        }
        if (warden.getPose() != Pose.STANDING || sonicBoomRunning()) {
            return;
        }
        if (pendingCustom != null) {
            WardenSkill custom = pendingCustom;
            pendingCustom = null;
            begin(custom, null, target);
            return;
        }
        if (pendingForced != null) {
            SkillType forced = pendingForced;
            pendingForced = null;
            if ((forced == SkillType.SILENT_COLLAPSE || forced == SkillType.RIFT_SONIC) && pendingForcedBlocked()) {
                return;
            }
            start(forced, target);
            int transition = WpConfig.TRANSITION_INVULNERABLE_TICKS.get();
            if (transition > 0) {
                guard(forced == SkillType.AWAKEN ? active.duration() : transition);
            }
            if (forced == SkillType.AWAKEN && stage >= 3 && WpConfig.skill(SkillType.SILENT_COLLAPSE).enabled().get()) {
                pendingForced = SkillType.SILENT_COLLAPSE;
            }
            return;
        }
        if (target == null || globalCooldown > 0) {
            return;
        }
        double dist = warden.distanceTo(target);
        List<SkillType> options = new ArrayList<>();
        int total = 0;
        for (SkillType type : SkillType.values()) {
            if (type.variant != variant || type.forcedOnly || stage < type.minStage || cooldowns.getOrDefault(type, 0) > 0) {
                continue;
            }
            if (!WpConfig.skill(type).enabled().get()) {
                continue;
            }
            boolean inRange = dist >= type.minRange && dist <= type.maxRange;
            if ((type == SkillType.ABYSSAL_BURROW || type == SkillType.RIFT_HUNT) && warden.getBrain().hasMemoryValue(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE) && dist <= type.maxRange) {
                inRange = true;
            }
            if (!inRange || !type.extraCheck(this, target, dist)) {
                continue;
            }
            options.add(type);
            total += type.weight;
        }
        if (options.isEmpty() || total <= 0) {
            return;
        }
        int roll = warden.getRandom().nextInt(total);
        for (SkillType type : options) {
            roll -= type.weight;
            if (roll < 0) {
                start(type, target);
                return;
            }
        }
    }

    private boolean pendingForcedBlocked() {
        return currentTarget() == null && lastTargetPos == null;
    }

    private boolean sonicBoomRunning() {
        for (BehaviorControl<? super Warden> behavior : warden.getBrain().getRunningBehaviors()) {
            if (behavior instanceof SonicBoom) {
                return true;
            }
        }
        return warden.getBrain().hasMemoryValue(MemoryModuleType.SONIC_BOOM_SOUND_DELAY);
    }

    private void start(SkillType type, LivingEntity target) {
        begin(type.create(), type, target);
    }

    private void begin(WardenSkill skill, SkillType type, LivingEntity target) {
        active = skill;
        activeType = type;
        activeTick = 0;
        warden.getNavigation().stop();
        warden.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        active.start(this, target);
    }

    private void finish(boolean interrupted) {
        WardenSkill skill = active;
        SkillType type = activeType;
        active = null;
        activeType = null;
        skill.stop(this, interrupted);
        if (type == null) {
            globalCooldown = Math.max(globalCooldown, 30);
            return;
        }
        int cd = WpConfig.skill(type).cooldown().get();
        if (stage >= 2) {
            cd = Mth.floor(cd * WpConfig.LATE_STAGE_COOLDOWN_SCALE.get());
        }
        cooldowns.put(type, cd);
        int gcd = WpConfig.GLOBAL_COOLDOWN.get();
        if (stage >= 2) {
            gcd = Mth.floor(gcd * WpConfig.LATE_STAGE_COOLDOWN_SCALE.get());
        }
        globalCooldown = gcd + warden.getRandom().nextInt(20);
        if (!warden.getBrain().hasMemoryValue(MemoryModuleType.SONIC_BOOM_COOLDOWN)) {
            SonicBoom.setCooldown(warden, 30);
        }
    }

    public void abort() {
        if (active != null) {
            finish(true);
        }
        hazards.clear();
        pendingHazards.clear();
        removeBossBar();
    }

    public void stopMoving() {
        warden.getNavigation().stop();
        warden.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        Vec3 v = warden.getDeltaMovement();
        warden.setDeltaMovement(0.0, v.y, 0.0);
    }

    public void face(Vec3 pos) {
        double dx = pos.x - warden.getX();
        double dz = pos.z - warden.getZ();
        if (dx * dx + dz * dz < 1.0E-4) {
            return;
        }
        float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        warden.setYRot(yaw);
        warden.setYBodyRot(yaw);
        warden.setYHeadRot(yaw);
        warden.getLookControl().setLookAt(pos.x, pos.y + 1.0, pos.z);
    }

    public Vec3 aimPos(LivingEntity target) {
        if (target != null) {
            return target.position();
        }
        return lastTargetPos != null ? lastTargetPos : warden.position();
    }

    private void updateBossBar() {
        if (!WpConfig.BOSS_BAR.get() || !(warden.level() instanceof ServerLevel level)) {
            removeBossBar();
            return;
        }
        if (combatTimer <= 0) {
            removeBossBar();
            return;
        }
        if (bossBar == null) {
            bossBar = new ServerBossEvent(Component.empty(), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10);
            bossBar.setDarkenScreen(true);
        }
        bossBar.setProgress(Mth.clamp(warden.getHealth() / warden.getMaxHealth(), 0.0F, 1.0F));
        if (variant == WardenVariant.ENDER) {
            bossBar.setName(Component.translatable("bossbar.wardenplus.ender.stage" + stage, warden.getDisplayName()));
            bossBar.setColor(stage == 1 ? BossEvent.BossBarColor.PURPLE : stage == 2 ? BossEvent.BossBarColor.PINK : BossEvent.BossBarColor.WHITE);
        } else {
            bossBar.setName(Component.translatable("bossbar.wardenplus.stage" + stage, warden.getDisplayName()));
            bossBar.setColor(stage == 1 ? BossEvent.BossBarColor.BLUE : stage == 2 ? BossEvent.BossBarColor.PURPLE : BossEvent.BossBarColor.WHITE);
        }
        if (warden.tickCount % 10 == 0) {
            double range = WpConfig.BOSS_BAR_RANGE.get();
            for (ServerPlayer player : level.players()) {
                boolean near = player.distanceToSqr(warden) <= range * range;
                if (near && !bossBar.getPlayers().contains(player)) {
                    bossBar.addPlayer(player);
                } else if (!near && bossBar.getPlayers().contains(player)) {
                    bossBar.removePlayer(player);
                }
            }
        }
    }

    private void removeBossBar() {
        if (bossBar != null) {
            bossBar.removeAllPlayers();
            bossBar = null;
        }
    }
}
