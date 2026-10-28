package com.wardenplus.network;

import com.wardenplus.WardenPlus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class VfxPayload {
    public static final ResourceLocation ID = new ResourceLocation(WardenPlus.MOD_ID, "vfx");
    private final int kind;
    private final int entityId;
    private final int duration;
    private final float[] data;

    public VfxPayload(int kind, int entityId, int duration, float[] data) {
        this.kind = kind;
        this.entityId = entityId;
        this.duration = duration;
        this.data = data;
    }

    public int kind() {
        return kind;
    }

    public int entityId() {
        return entityId;
    }

    public int duration() {
        return duration;
    }

    public float[] data() {
        return data;
    }

    public static void encode(VfxPayload payload, FriendlyByteBuf buffer) {
        buffer.writeVarInt(payload.kind);
        buffer.writeVarInt(payload.entityId);
        buffer.writeVarInt(payload.duration);
        buffer.writeVarInt(payload.data.length);
        for (float value : payload.data) {
            buffer.writeFloat(value);
        }
    }

    public static VfxPayload decode(FriendlyByteBuf buffer) {
        int kind = buffer.readVarInt();
        int entityId = buffer.readVarInt();
        int duration = buffer.readVarInt();
        int length = Math.min(buffer.readVarInt(), 256);
        float[] data = new float[length];
        for (int index = 0; index < length; index++) {
            data[index] = buffer.readFloat();
        }
        return new VfxPayload(kind, entityId, duration, data);
    }
}
