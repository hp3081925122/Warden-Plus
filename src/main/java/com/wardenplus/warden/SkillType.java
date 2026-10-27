package com.wardenplus.warden;

import com.wardenplus.warden.skill.AbyssalBurrowSkill;
import com.wardenplus.warden.skill.AwakenSkill;
import com.wardenplus.warden.skill.EchoPrismSkill;
import com.wardenplus.warden.skill.HeartbeatDomainSkill;
import com.wardenplus.warden.skill.SculkSnareSkill;
import com.wardenplus.warden.skill.SeismicPulseSkill;
import com.wardenplus.warden.skill.SilentCollapseSkill;
import com.wardenplus.warden.skill.WardenSkill;
import com.wardenplus.warden.skill.ender.BlinkSlamSkill;
import com.wardenplus.warden.skill.ender.RiftHuntSkill;
import com.wardenplus.warden.skill.ender.RiftSonicSkill;
import com.wardenplus.warden.skill.ender.VoidLoopSkill;
import com.wardenplus.warden.skill.ender.EnderSwapSkill;
import com.wardenplus.warden.skill.ender.GrappleSlamSkill;
import java.util.function.Supplier;
import net.minecraft.world.entity.LivingEntity;

public enum SkillType {
    SEISMIC_PULSE(WardenVariant.ECHO, "seismic_pulse", "震脉践踏", "Seismic Pulse", 0.4, 160, 1, 0.0, 9.0, 4, false, SeismicPulseSkill::new),
    SCULK_SNARE(WardenVariant.ECHO, "sculk_snare", "幽匿缠须", "Sculk Snare", 0.3, 200, 1, 4.0, 24.0, 3, false, SculkSnareSkill::new),
    ABYSSAL_BURROW(WardenVariant.ECHO, "abyssal_burrow", "潜渊突袭", "Abyssal Burrow", 0.6, 360, 1, 10.0, 40.0, 3, false, AbyssalBurrowSkill::new),
    HEARTBEAT_DOMAIN(WardenVariant.ECHO, "heartbeat_domain", "心跳领域", "Heartbeat Domain", 0.28, 520, 2, 0.0, 14.0, 2, false, HeartbeatDomainSkill::new),
    ECHO_PRISM(WardenVariant.ECHO, "echo_prism", "回响棱晶", "Echo Prism", 0.25, 280, 2, 6.0, 30.0, 3, false, EchoPrismSkill::new),
    SILENT_COLLAPSE(WardenVariant.ECHO, "silent_collapse", "寂灭回响", "Silent Collapse", 1.1, 900, 3, 0.0, 22.0, 1, false, SilentCollapseSkill::new),
    AWAKEN(WardenVariant.ECHO, "awaken", "共鸣苏醒", "Resonant Awakening", 0.2, 0, 2, 0.0, 64.0, 0, true, AwakenSkill::new),
    BLINK_SLAM(WardenVariant.ENDER, "blink_slam", "末影闪击", "Ender Blink Slam", 0.7, 180, 1, 4.0, 24.0, 4, false, BlinkSlamSkill::new, BlinkSlamSkill::canStart),
    GRAPPLE_SLAM(WardenVariant.ENDER, "grapple_slam", "擒摔", "Grapple Slam", 0.5, 260, 1, 0.0, 4.5, 3, false, GrappleSlamSkill::new, GrappleSlamSkill::canStart),
    VOID_LOOP(WardenVariant.ENDER, "void_loop", "虚空回环摔", "Void Loop Slam", 1.0, 600, 1, 0.0, 4.5, 1, false, VoidLoopSkill::new, VoidLoopSkill::canStart),
    ENDER_SWAP(WardenVariant.ENDER, "ender_swap", "末影置换", "Ender Swap", 0.4, 420, 2, 6.0, 26.0, 2, false, EnderSwapSkill::new, EnderSwapSkill::canStart),
    RIFT_HUNT(WardenVariant.ENDER, "rift_hunt", "裂隙追猎", "Rift Hunt", 0.5, 200, 1, 18.0, 48.0, 3, false, RiftHuntSkill::new, RiftHuntSkill::canStart),
    RIFT_SONIC(WardenVariant.ENDER, "rift_sonic", "裂隙千音", "Rift Sonic Barrage", 0.4, 1200, 2, 0.0, 40.0, 1, false, RiftSonicSkill::new);

    public interface Check {
        boolean test(WardenController c, LivingEntity target, double dist);
    }

    public final WardenVariant variant;
    public final String key;
    public final String zhName;
    public final String enName;
    public final double defaultDamage;
    public final int defaultCooldown;
    public final int minStage;
    public final double minRange;
    public final double maxRange;
    public final int weight;
    public final boolean forcedOnly;
    private final Supplier<WardenSkill> factory;
    private final Check check;

    SkillType(WardenVariant variant, String key, String zhName, String enName, double defaultDamage, int defaultCooldown, int minStage, double minRange, double maxRange, int weight, boolean forcedOnly, Supplier<WardenSkill> factory) {
        this(variant, key, zhName, enName, defaultDamage, defaultCooldown, minStage, minRange, maxRange, weight, forcedOnly, factory, null);
    }

    SkillType(WardenVariant variant, String key, String zhName, String enName, double defaultDamage, int defaultCooldown, int minStage, double minRange, double maxRange, int weight, boolean forcedOnly, Supplier<WardenSkill> factory, Check check) {
        this.variant = variant;
        this.key = key;
        this.zhName = zhName;
        this.enName = enName;
        this.defaultDamage = defaultDamage;
        this.defaultCooldown = defaultCooldown;
        this.minStage = minStage;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.weight = weight;
        this.forcedOnly = forcedOnly;
        this.factory = factory;
        this.check = check;
    }

    public WardenSkill create() {
        return factory.get();
    }

    public boolean extraCheck(WardenController c, LivingEntity target, double dist) {
        return check == null || check.test(c, target, dist);
    }
}
