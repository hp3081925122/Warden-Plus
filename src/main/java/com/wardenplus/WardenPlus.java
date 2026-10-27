package com.wardenplus;

import com.wardenplus.network.WpNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(WardenPlus.MOD_ID)
public final class WardenPlus {
    public static final String MOD_ID = "wardenplus";

    public WardenPlus(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(WpNetwork::register);
        modContainer.registerConfig(ModConfig.Type.COMMON, WpConfig.SPEC);
    }
}
