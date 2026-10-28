package com.wardenplus.network;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

public final class Vfx {
    public static final int STAGE = 0;
    public static final int ANIM = 1;
    public static final int SHOCK_RING = 2;
    public static final int CRACK = 3;
    public static final int GATHER = 4;
    public static final int SNARE = 5;
    public static final int BURROW = 6;
    public static final int ERUPT = 7;
    public static final int DOMAIN = 8;
    public static final int HEARD = 9;
    public static final int PRISMS = 10;
    public static final int BEAM = 11;
    public static final int COLLAPSE = 12;
    public static final int BLAST = 13;
    public static final int AWAKEN = 14;
    public static final int DARKNESS = 16;
    public static final int ENDER_TRAIL = 17;
    public static final int SOUL_GATHER = 18;
    public static final int SONIC_TRACK = 19;
    public static final int SONIC_LOCK = 20;
    public static final int RIFT = 21;
    public static final int RIFT_END = 22;
    public static final int VARIANT = 26;
    public static final int CONVERT = 27;
    public static final int STUN = 28;
    public static final int ENTITY_MARK = 29;
    public static final int VOID_IMPACT = 30;
    public static final int RIFT_ZONE = 31;

    public static final int PALETTE_ECHO = 0;
    public static final int PALETTE_ENDER = 1;

    public static final int ANIM_SLAM = 1;
    public static final int ANIM_SNARE = 2;
    public static final int ANIM_SINK = 3;
    public static final int ANIM_EMERGE = 4;
    public static final int ANIM_DOMAIN = 5;
    public static final int ANIM_PRISM = 6;
    public static final int ANIM_COLLAPSE = 7;
    public static final int ANIM_AWAKEN = 8;
    public static final int ANIM_GRAB = 9;
    public static final int ANIM_STUN = 10;
    public static final int ANIM_SONIC = 11;
    public static final int ANIM_LIFT_SLAM = 17;
    public static final int ANIM_GRAB_HOLD = 12;
    public static final int ANIM_BLINK = 13;
    public static final int ANIM_CONVERT = 14;
    public static final int ANIM_SWAP = 15;
    public static final int ANIM_NONE = 0;

    private Vfx() {
    }

    public static void send(Entity source, int kind, int duration, float... data) {
        WpNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> source), new VfxPayload(kind, source.getId(), duration, data));
    }

    public static void anim(Entity source, int anim, int duration) {
        send(source, ANIM, duration, anim);
    }

    public static float[] pos(Vec3 v, float... extra) {
        float[] out = new float[3 + extra.length];
        out[0] = (float) v.x;
        out[1] = (float) v.y;
        out[2] = (float) v.z;
        System.arraycopy(extra, 0, out, 3, extra.length);
        return out;
    }
}
