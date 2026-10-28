package com.wardenplus.network;

import com.wardenplus.WardenPlus;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class WpNetwork {
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(WardenPlus.MOD_ID, "main"), () -> "1", version -> true, version -> true);

    private WpNetwork() {
    }

    public static void init() {
        CHANNEL.registerMessage(0, VfxPayload.class, VfxPayload::encode, VfxPayload::decode, (payload, context) -> {
            context.get().enqueueWork(() -> com.wardenplus.client.ClientPayloads.handle(payload));
            context.get().setPacketHandled(true);
        });
    }
}
