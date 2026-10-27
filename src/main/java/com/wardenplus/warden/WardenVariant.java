package com.wardenplus.warden;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.monster.warden.Warden;

public enum WardenVariant {
    NONE("none"),
    ECHO("echo"),
    ENDER("ender");

    public static final String KEY = "wardenplus_variant";
    public static final String ENHANCED_KEY = "wardenplus_enhanced";
    public static final String ATTR_KEY = "wardenplus_attr_applied";

    public final String key;

    WardenVariant(String key) {
        this.key = key;
    }

    public static WardenVariant byKey(String key) {
        for (WardenVariant v : values()) {
            if (v.key.equals(key)) {
                return v;
            }
        }
        return NONE;
    }

    public static WardenVariant byId(int id) {
        WardenVariant[] all = values();
        return id >= 0 && id < all.length ? all[id] : NONE;
    }

    public static WardenVariant of(Warden warden) {
        return byKey(warden.getPersistentData().getString(KEY));
    }

    public static void set(Warden warden, WardenVariant variant) {
        CompoundTag tag = warden.getPersistentData();
        tag.putString(KEY, variant.key);
        tag.putBoolean(ENHANCED_KEY, variant != NONE);
    }
}
