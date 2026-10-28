package com.wardenplus.client.vfx;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class VfxManager {
    private static final List<VfxBase> ACTIVE = new ArrayList<>();
    private static final List<VfxBase> PENDING = new ArrayList<>();

    private VfxManager() {
    }

    public static void add(VfxBase vfx) {
        PENDING.add(vfx);
    }

    public static void removeIf(Predicate<VfxBase> filter) {
        ACTIVE.removeIf(filter);
        PENDING.removeIf(filter);
    }

    public static void each(Consumer<VfxBase> action) {
        ACTIVE.forEach(action);
        PENDING.forEach(action);
    }

    public static void tick() {
        if (!PENDING.isEmpty()) {
            ACTIVE.addAll(PENDING);
            PENDING.clear();
        }
        ACTIVE.removeIf(v -> !v.tick());
    }

    public static void render(float partial) {
        for (VfxBase vfx : ACTIVE) {
            vfx.render(partial);
        }
    }

    public static boolean isEmpty() {
        return ACTIVE.isEmpty();
    }

    public static void clear() {
        ACTIVE.clear();
        PENDING.clear();
    }
}




