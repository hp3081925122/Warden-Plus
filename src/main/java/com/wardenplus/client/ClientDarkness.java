package com.wardenplus.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

public final class ClientDarkness {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int BLEND = 22;
    private static int remaining;
    private static int total;
    private static float factor;
    private static float factorO;
    private static float visibility = 15.0F;

    private ClientDarkness() {
    }

    public static void apply(int ticks, float distance) {
        visibility = Math.max(0.0F, distance);
        if (remaining <= 0) {
            LOGGER.debug("[WardenPlus] Client darkness started, hold {} ticks, darknessEffectScale {}", ticks, Minecraft.getInstance().options.darknessEffectScale().get());
        }
        if (ticks > remaining) {
            int elapsed = factor > 0 ? Math.max(0, total - remaining) : 0;
            remaining = ticks;
            total = ticks + Math.min(elapsed, BLEND);
        }
    }

    public static void tick() {
        factorO = factor;
        if (remaining > 0) {
            remaining--;
            float in = Math.min(1.0F, (float) (total - remaining) / BLEND);
            float out = Math.min(1.0F, (float) remaining / BLEND);
            factor = Math.min(in, out);
        } else {
            factor = 0;
        }
    }

    public static float factor(float partial) {
        if (Minecraft.getInstance().player == null) {
            return 0;
        }
        return Mth.lerp(partial, factorO, factor);
    }

    public static float pulse(float partial) {
        if (Minecraft.getInstance().player == null) {
            return 0;
        }
        return Math.max(0.0F, Mth.cos((Minecraft.getInstance().player.tickCount - partial) * Mth.PI * 0.025F));
    }

    public static float visibility() {
        return visibility;
    }

    public static void clear() {
        remaining = 0;
        total = 0;
        factor = 0;
        factorO = 0;
    }
}
