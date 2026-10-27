package com.wardenplus.warden.hazard;

import com.wardenplus.warden.WardenController;

public interface Hazard {
    boolean tick(WardenController controller);
}
