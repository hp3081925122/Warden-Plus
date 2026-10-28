package com.wardenplus;

import com.wardenplus.network.WpNetwork;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(WardenPlus.MOD_ID)
public final class WardenPlus {
    public static final String MOD_ID = "wardenplus";

    public WardenPlus() {
        WpNetwork.init();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, WpConfig.SPEC);
    }
}
