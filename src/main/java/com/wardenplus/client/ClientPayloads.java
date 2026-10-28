package com.wardenplus.client;

import com.wardenplus.client.vfx.AwakenVfx;
import com.wardenplus.client.vfx.BeamVfx;
import com.wardenplus.client.vfx.BlastVfx;
import com.wardenplus.client.vfx.BurrowVfx;
import com.wardenplus.client.vfx.CollapseVfx;
import com.wardenplus.client.vfx.CrackVfx;
import com.wardenplus.client.vfx.DomainVfx;
import com.wardenplus.client.vfx.EruptVfx;
import com.wardenplus.client.vfx.GatherVfx;
import com.wardenplus.client.vfx.HeardVfx;
import com.wardenplus.client.vfx.PrismVfx;
import com.wardenplus.client.vfx.ShockRingVfx;
import com.wardenplus.client.vfx.SnareVfx;
import com.wardenplus.client.vfx.VfxManager;
import com.wardenplus.client.vfx.ConvertVfx;
import com.wardenplus.client.vfx.EnderTrailVfx;
import com.wardenplus.client.vfx.MarkVfx;
import com.wardenplus.client.vfx.RiftVfx;
import com.wardenplus.client.vfx.RiftZoneVfx;
import com.wardenplus.client.vfx.SonicLockVfx;
import com.wardenplus.client.vfx.SonicTrackVfx;
import com.wardenplus.client.vfx.SoulGatherVfx;
import com.wardenplus.client.vfx.StunVfx;
import com.wardenplus.client.vfx.VoidImpactVfx;
import com.wardenplus.network.Vfx;
import com.wardenplus.network.VfxPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class ClientPayloads {
    private ClientPayloads() {
    }

    private static Vec3 pos(float[] d, int i) {
        return new Vec3(d[i], d[i + 1], d[i + 2]);
    }

    private static int opt(float[] d, int i) {
        return d.length > i ? (int) d[i] : 0;
    }

    public static void handle(VfxPayload p) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        float[] d = p.data();
        int id = p.entityId();
        int life = p.duration();
        switch (p.kind()) {
            case Vfx.STAGE -> ClientWardenState.get(id).stage = (int) d[0];
            case Vfx.ANIM -> {
                Entity e = Minecraft.getInstance().level.getEntity(id);
                ClientWardenState.State s = ClientWardenState.get(id);
                s.anim = (int) d[0];
                s.animStart = e == null ? 0 : e.tickCount;
                s.animDuration = life;
            }
            case Vfx.SHOCK_RING -> VfxManager.add(new ShockRingVfx(id, pos(d, 0), d[3], d[4], (int) d[5], d[6], opt(d, 7)));
            case Vfx.CRACK -> VfxManager.add(new CrackVfx(id, pos(d, 0), d[3], life, opt(d, 4)));
            case Vfx.GATHER -> VfxManager.add(new GatherVfx(id, life));
            case Vfx.SNARE -> VfxManager.add(new SnareVfx(id, pos(d, 0), (int) d[3], life));
            case Vfx.BURROW -> VfxManager.add(new BurrowVfx(id, pos(d, 0), life));
            case Vfx.ERUPT -> {
                VfxManager.each(v -> {
                    if (v instanceof BurrowVfx b && b.entityId() == id) {
                        b.end();
                    }
                });
                VfxManager.add(new EruptVfx(id, pos(d, 0), d[3], (int) d[4], life));
            }
            case Vfx.DOMAIN -> VfxManager.add(new DomainVfx(id, pos(d, 0), d[3], (int) d[4], (int) d[5], (int) d[6], life));
            case Vfx.HEARD -> VfxManager.add(new HeardVfx(id, (int) d[0], life));
            case Vfx.PRISMS -> VfxManager.add(new PrismVfx(id, d, life));
            case Vfx.BEAM -> VfxManager.add(new BeamVfx(id, pos(d, 0), pos(d, 3), (int) d[6], life));
            case Vfx.COLLAPSE -> VfxManager.add(new CollapseVfx(id, d[0], life));
            case Vfx.BLAST -> VfxManager.add(new BlastVfx(id, pos(d, 0), d[3], life));
            case Vfx.AWAKEN -> VfxManager.add(new AwakenVfx(id, life));
            case Vfx.VARIANT -> ClientWardenState.get(id).variant = (int) d[0];
            case Vfx.DARKNESS -> ClientDarkness.apply(life);
            case Vfx.ENDER_TRAIL -> VfxManager.add(new EnderTrailVfx(id, pos(d, 0), pos(d, 3), d[6], (int) d[7], life));
            case Vfx.SOUL_GATHER -> VfxManager.add(new SoulGatherVfx(id, life));
            case Vfx.SONIC_TRACK -> VfxManager.add(new SonicTrackVfx(id, (int) d[0], life));
            case Vfx.SONIC_LOCK -> VfxManager.add(new SonicLockVfx(id, pos(d, 0), pos(d, 3), (int) d[6], life));
            case Vfx.RIFT -> VfxManager.add(new RiftVfx(id, pos(d, 0), pos(d, 3), d[6], (int) d[7], (int) d[8], life));
            case Vfx.RIFT_END -> VfxManager.each(v -> {
                if (v instanceof RiftVfx r && r.entityId() == id && r.tag() == (int) d[0]) {
                    r.close();
                }
            });
            case Vfx.CONVERT -> VfxManager.add(new ConvertVfx(id, (int) d[0], (int) d[1], life));
            case Vfx.STUN -> VfxManager.add(new StunVfx(id, life));
            case Vfx.ENTITY_MARK -> VfxManager.add(new MarkVfx(id, (int) d[0], d[1], life));
            case Vfx.RIFT_ZONE -> VfxManager.add(new RiftZoneVfx(id, pos(d, 0), d[3], (int) d[4], (int) d[5], life));
            case Vfx.VOID_IMPACT -> VfxManager.add(new VoidImpactVfx(id, pos(d, 0), d[3], life));
            default -> {
            }
        }
    }
}
