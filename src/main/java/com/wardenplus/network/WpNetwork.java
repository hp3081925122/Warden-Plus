package com.wardenplus.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class WpNetwork {
    private WpNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(VfxPayload.TYPE, VfxPayload.STREAM_CODEC, (payload, ctx) -> ctx.enqueueWork(() -> com.wardenplus.client.ClientPayloads.handle(payload)));
    }
}




