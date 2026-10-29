package com.wardenplus;

import com.wardenplus.warden.SkillType;
import com.wardenplus.warden.WardenVariant;
import java.util.EnumMap;
import java.util.Map;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class WpConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.DoubleValue HEALTH_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue SKILL_DAMAGE_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue KNOCKBACK_SCALE;
    public static final ModConfigSpec.DoubleValue STAGE_TWO_THRESHOLD;
    public static final ModConfigSpec.DoubleValue STAGE_THREE_THRESHOLD;
    public static final ModConfigSpec.IntValue TRANSITION_INVULNERABLE_TICKS;
    public static final ModConfigSpec.DoubleValue LATE_STAGE_COOLDOWN_SCALE;
    public static final ModConfigSpec.IntValue GLOBAL_COOLDOWN;
    public static final ModConfigSpec.DoubleValue AWAKEN_DAMAGE_TAKEN;
    public static final ModConfigSpec.BooleanValue BOSS_BAR;
    public static final ModConfigSpec.IntValue BOSS_BAR_RANGE;
    public static final ModConfigSpec.BooleanValue CONVERSION_ENABLED;
    public static final ModConfigSpec.ConfigValue<String> ECHO_ITEM;
    public static final ModConfigSpec.BooleanValue ECHO_ENABLED;
    public static final ModConfigSpec.ConfigValue<String> ENDER_ITEM;
    public static final ModConfigSpec.ConfigValue<String> ENDER_LOOT_TABLE;
    public static final ModConfigSpec.BooleanValue CONSUME_ITEM;
    public static final ModConfigSpec.DoubleValue ENDER_HEALTH_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue ENDER_ATTACK_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue TOUCH_CHANCE;
    public static final ModConfigSpec.IntValue TOUCH_COOLDOWN;
    public static final ModConfigSpec.DoubleValue TOUCH_MIN_DISTANCE;
    public static final ModConfigSpec.DoubleValue TOUCH_MAX_DISTANCE;
    public static final ModConfigSpec.DoubleValue DODGE_CHANCE;
    public static final ModConfigSpec.IntValue DODGE_COOLDOWN;
    public static final ModConfigSpec.IntValue SONIC_MIN_JUMPS;
    public static final ModConfigSpec.IntValue SONIC_MAX_JUMPS;
    public static final ModConfigSpec.IntValue SONIC_STUN_TICKS;
    public static final ModConfigSpec.DoubleValue SONIC_STUN_DAMAGE_TAKEN;
    public static final ModConfigSpec.DoubleValue DARKNESS_RADIUS;
    public static final ModConfigSpec.DoubleValue DARKNESS_VISIBILITY;
    public static final Map<SkillType, Skill> SKILLS = new EnumMap<>(SkillType.class);

    public record Skill(ModConfigSpec.BooleanValue enabled, ModConfigSpec.DoubleValue damage, ModConfigSpec.IntValue cooldown) {
    }

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.comment("监守者增强总设置 / General settings for the enhanced Warden").push("general");
        ENABLED = b.comment("是否启用重做后的监守者技能 / Enable reworked Warden skills").define("enabled", true);
        HEALTH_MULTIPLIER = b.comment("回响监守者最大生命倍率，只在转化时应用一次 / Echo Warden max health multiplier, applied once on conversion").defineInRange("healthMultiplier", 1.0, 0.1, 100.0);
        SKILL_DAMAGE_MULTIPLIER = b.comment("所有技能伤害的全局倍率，技能伤害 = 攻击力 × 技能倍率 × 本值 / Global multiplier for skill damage, damage = attack × skill ratio × this").defineInRange("skillDamageMultiplier", 1.0, 0.0, 100.0);
        KNOCKBACK_SCALE = b.comment("技能击退倍率，仍会受击退抗性影响 / Skill knockback scale, still reduced by knockback resistance").defineInRange("knockbackScale", 1.0, 0.0, 5.0);
        STAGE_TWO_THRESHOLD = b.comment("进入二阶段的生命百分比 / Health ratio that starts stage two").defineInRange("stageTwoThreshold", 0.5, 0.0, 1.0);
        STAGE_THREE_THRESHOLD = b.comment("进入三阶段的生命百分比 / Health ratio that starts stage three").defineInRange("stageThreeThreshold", 0.25, 0.0, 1.0);
        TRANSITION_INVULNERABLE_TICKS = b.comment("转阶段时的无敌时长（tick），0 为关闭；共鸣苏醒期间全程无敌 / Invulnerable ticks on stage transition, 0 disables; awakening is fully invulnerable").defineInRange("transitionInvulnerableTicks", 40, 0, 400);
        LATE_STAGE_COOLDOWN_SCALE = b.comment("二、三阶段技能冷却倍率 / Skill cooldown scale in stage two and three").defineInRange("lateStageCooldownScale", 0.75, 0.1, 2.0);
        GLOBAL_COOLDOWN = b.comment("两次技能之间的最短间隔（刻） / Minimum ticks between two skills").defineInRange("globalCooldown", 50, 0, 1200);
        AWAKEN_DAMAGE_TAKEN = b.comment("共鸣苏醒期间受到伤害的倍率 / Damage taken multiplier while awakening").defineInRange("awakenDamageTaken", 0.3, 0.0, 1.0);
        BOSS_BAR = b.comment("战斗时是否显示 Boss 血条 / Show a boss bar while fighting").define("bossBar", true);
        BOSS_BAR_RANGE = b.comment("Boss 血条显示范围（格） / Boss bar range in blocks").defineInRange("bossBarRange", 48, 8, 256);
        b.pop();
        b.comment("转化设置：自然生成的都是普通监守者，用物品右键转化 / Conversion: natural Wardens are vanilla, right click with an item to convert").push("conversion");
        CONVERSION_ENABLED = b.comment("是否允许右键转化 / Allow right click conversion").define("enabled", true);
        ECHO_ENABLED = b.comment("是否允许转化为回响监守者（暂未开放） / Allow converting into the Echo Warden (not released yet)").define("echoEnabled", false);
        ECHO_ITEM = b.comment("转化为回响监守者的物品 ID / Item ID that converts into the Echo Warden").define("echoItem", "minecraft:echo_shard");
        ENDER_ITEM = b.comment("转化为末影监守者的物品 ID / Item ID that converts into the Ender Warden").define("enderItem", "minecraft:ender_pearl");
        CONSUME_ITEM = b.comment("生存模式下是否消耗物品 / Consume the item in survival").define("consumeItem", true);
        b.pop();
        b.comment("末影监守者设置 / Ender Warden settings").push("ender");
        ENDER_LOOT_TABLE = b.comment("末影监守者使用的战利品表 ID / Loot table ID used by the Ender Warden").define("lootTable", "minecraft:entities/warden");
        ENDER_HEALTH_MULTIPLIER = b.comment("最大生命倍率，只在转化时应用一次 / Max health multiplier, applied once on conversion").defineInRange("healthMultiplier", 1.0, 0.1, 100.0);
        ENDER_ATTACK_MULTIPLIER = b.comment("攻击力倍率，只在转化时应用一次 / Attack damage multiplier, applied once on conversion").defineInRange("attackMultiplier", 1.0, 0.1, 100.0);
        TOUCH_CHANCE = b.comment("普攻命中后把目标随机传送走的概率 / Chance that a melee hit teleports the target").defineInRange("touchChance", 0.35, 0.0, 1.0);
        TOUCH_COOLDOWN = b.comment("普攻传送的冷却（刻） / Melee teleport cooldown in ticks").defineInRange("touchCooldown", 60, 0, 72000);
        TOUCH_MIN_DISTANCE = b.comment("普攻传送最近距离（格） / Melee teleport min distance").defineInRange("touchMinDistance", 5.0, 1.0, 64.0);
        TOUCH_MAX_DISTANCE = b.comment("普攻传送最远距离（格） / Melee teleport max distance").defineInRange("touchMaxDistance", 10.0, 1.0, 64.0);
        DODGE_CHANCE = b.comment("相位闪避：被弹射物击中时传送躲开的概率 / Phase Dodge: chance to teleport away from projectiles").defineInRange("dodgeChance", 0.6, 0.0, 1.0);
        DODGE_COOLDOWN = b.comment("相位闪避冷却（刻） / Phase Dodge cooldown in ticks").defineInRange("dodgeCooldown", 40, 0, 72000);
        SONIC_MIN_JUMPS = b.comment("裂隙千音最少传送次数 / Rift Sonic Barrage min teleports").defineInRange("sonicMinJumps", 3, 1, 10);
        SONIC_MAX_JUMPS = b.comment("裂隙千音最多传送次数，三阶段固定取最大值 / Rift Sonic Barrage max teleports, stage three always uses max").defineInRange("sonicMaxJumps", 5, 1, 10);
        SONIC_STUN_TICKS = b.comment("裂隙千音结束后的宕机时间（刻） / Stun after Rift Sonic Barrage in ticks").defineInRange("sonicStunTicks", 60, 0, 600);
        SONIC_STUN_DAMAGE_TAKEN = b.comment("宕机期间受到伤害的倍率 / Damage taken multiplier while stunned").defineInRange("sonicStunDamageTaken", 1.25, 0.0, 10.0);
        DARKNESS_RADIUS = b.comment("裂隙千音致盲范围（格） / Rift Sonic Barrage darkness radius").defineInRange("darknessRadius", 40.0, 0.0, 256.0);
        DARKNESS_VISIBILITY = b.comment("裂隙千音黑暗中的可见距离（格），会随黑暗脉动缩短到一半；0 为全黑，原版黑暗约为 15；技能特效与预警不受影响 / Visible distance inside the Rift Sonic Barrage darkness, shrinks to half with the darkness pulse; 0 is pitch black, vanilla Darkness is about 15; skill effects and warnings stay visible").defineInRange("darknessVisibility", 7.0, 0.0, 64.0);
        b.pop();
        defineSkills(b, WardenVariant.ECHO, "skills", "回响监守者各技能设置，伤害为攻击力倍率 / Echo Warden skills, damage is a ratio of attack damage");
        defineSkills(b, WardenVariant.ENDER, "ender_skills", "末影监守者各技能设置，伤害为攻击力倍率 / Ender Warden skills, damage is a ratio of attack damage");
        SPEC = b.build();
    }

    private static void defineSkills(ModConfigSpec.Builder b, WardenVariant variant, String path, String comment) {
        b.comment(comment).push(path);
        for (SkillType type : SkillType.values()) {
            if (type.variant != variant) {
                continue;
            }
            b.comment(type.zhName + " / " + type.enName).push(type.key);
            ModConfigSpec.BooleanValue enabled = b.comment("是否启用 / Enabled").define("enabled", true);
            ModConfigSpec.DoubleValue damage = b.comment("伤害倍率 / Damage ratio").defineInRange("damageRatio", type.defaultDamage, 0.0, 20.0);
            ModConfigSpec.IntValue cooldown = b.comment("冷却（刻） / Cooldown in ticks").defineInRange("cooldown", type.defaultCooldown, 0, 72000);
            SKILLS.put(type, new Skill(enabled, damage, cooldown));
            b.pop();
        }
        b.pop();
    }

    private WpConfig() {
    }

    public static Skill skill(SkillType type) {
        return SKILLS.get(type);
    }
}




