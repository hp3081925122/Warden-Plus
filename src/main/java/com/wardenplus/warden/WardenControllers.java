package com.wardenplus.warden;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.entity.monster.warden.Warden;

public final class WardenControllers {
    private static final Map<Warden, WardenController> CONTROLLERS = new WeakHashMap<>();

    private WardenControllers() {
    }

    public static WardenController get(Warden warden) {
        if (warden.level().isClientSide()) {
            return null;
        }
        return CONTROLLERS.computeIfAbsent(warden, WardenController::new);
    }

    public static WardenController peek(Warden warden) {
        return CONTROLLERS.get(warden);
    }

    public static void remove(Warden warden) {
        WardenController c = CONTROLLERS.remove(warden);
        if (c != null) {
            c.abort();
        }
    }

    public static boolean isBrainLocked(Warden warden) {
        WardenController c = CONTROLLERS.get(warden);
        return c != null && c.brainLocked();
    }
}
