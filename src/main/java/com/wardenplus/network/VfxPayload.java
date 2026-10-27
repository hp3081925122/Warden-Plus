package com.wardenplus.network;

import com.wardenplus.WardenPlus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record VfxPayload(int kind, int entityId, int duration, float[] data) implements CustomPacketPayload {
    public static final Type<VfxPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(WardenPlus.MOD_ID, "vfx"));
    public static final StreamCodec<FriendlyByteBuf, VfxPayload> STREAM_CODEC = StreamCodec.of(VfxPayload::write, VfxPayload::read);

    private static void write(FriendlyByteBuf buf, VfxPayload p) {
        buf.writeVarInt(p.kind);
        buf.writeVarInt(p.entityId);
        buf.writeVarInt(p.duration);
        buf.writeVarInt(p.data.length);
        for (float f : p.data) {
            buf.writeFloat(f);
        }
    }

    private static VfxPayload read(FriendlyByteBuf buf) {
        int kind = buf.readVarInt();
        int id = buf.readVarInt();
        int duration = buf.readVarInt();
        int n = Math.min(buf.readVarInt(), 256);
        float[] data = new float[n];
        for (int i = 0; i < n; i++) {
            data[i] = buf.readFloat();
        }
        return new VfxPayload(kind, id, duration, data);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
