package com.wardenplus.client.anim;

import com.wardenplus.client.ClientWardenState;
import com.wardenplus.network.Vfx;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.warden.Warden;

public final class WardenAnimator {
    private float armX;
    private float armZ;
    private float leftArmX;
    private float rightArmX;
    private float leftArmZ;
    private float rightArmZ;
    private float bodyYaw;
    private float bodyX;
    private float bodyY;
    private float headX;
    private float rib;
    private float tendril;
    private float pulse;

    public static final class Parts {
        public ModelPart body;
        public ModelPart head;
        public ModelPart leftArm;
        public ModelPart rightArm;
        public ModelPart leftRib;
        public ModelPart rightRib;
        public ModelPart leftTendril;
        public ModelPart rightTendril;
    }

    private static final WardenAnimator INSTANCE = new WardenAnimator();

    private WardenAnimator() {
    }

    private static float smooth(float x) {
        x = Mth.clamp(x, 0, 1);
        return x * x * (3 - 2 * x);
    }

    private static float seg(float t, float a, float b) {
        return smooth((t - a) / (b - a));
    }

    public static void apply(Warden warden, float ageInTicks, Parts p) {
        ClientWardenState.State s = ClientWardenState.peek(warden.getId());
        if (s == null) {
            return;
        }
        WardenAnimator a = INSTANCE;
        a.reset();
        boolean animating = false;
        if (s.anim != 0) {
            float t = ageInTicks - s.animStart;
            if (t >= 0 && t <= s.animDuration + 1) {
                a.pose(s.anim, t, s.animDuration, s.stage);
                animating = true;
            }
        }
        if (s.stage >= 2 && !animating) {
            a.rib += 0.12F + 0.06F * Mth.sin(ageInTicks * 0.25F);
            a.tendril += 0.15F * Mth.sin(ageInTicks * 0.6F);
        }
        a.write(p);
    }

    private void reset() {
        leftArmZ = rightArmZ = bodyYaw = 0;
        armX = armZ = leftArmX = rightArmX = bodyX = bodyY = headX = rib = tendril = pulse = 0;
    }

    private void pose(int anim, float t, int dur, int stage) {
        switch (anim) {
            case Vfx.ANIM_SLAM -> slam(t);
            case Vfx.ANIM_SNARE -> snare(t);
            case Vfx.ANIM_SINK -> sink(t, dur);
            case Vfx.ANIM_EMERGE -> emerge(t, dur);
            case Vfx.ANIM_DOMAIN -> domain(t, dur, stage);
            case Vfx.ANIM_PRISM -> prism(t, dur);
            case Vfx.ANIM_COLLAPSE -> collapse(t, dur);
            case Vfx.ANIM_AWAKEN, Vfx.ANIM_CONVERT -> awaken(t, dur);
            case Vfx.ANIM_GRAB -> grab(t, dur);
            case Vfx.ANIM_GRAB_HOLD -> hold(1 - seg(t, dur - 6, dur));
            case Vfx.ANIM_LIFT_SLAM -> liftSlam(t, dur);
            case Vfx.ANIM_STUN -> stun(t, dur);
            case Vfx.ANIM_SONIC -> sonic(t, dur);
            case Vfx.ANIM_BLINK -> blink(t, dur);
            case Vfx.ANIM_SWAP -> swap(t, dur);
            default -> {
            }
        }
    }

    private void slam(float t) {
        float raise = seg(t, 0, 20);
        float down = seg(t, 21, 25);
        float rec = seg(t, 28, 40);
        float up = raise * (1 - down);
        float hit = down * (1 - rec);
        armX = -2.9F * up - 1.0F * hit;
        armZ = 0.3F * up + 0.15F * hit;
        bodyX = -0.28F * up + 0.6F * hit;
        bodyY = 3.0F * hit;
        headX = -0.35F * up + 0.2F * hit;
        tendril = 0.5F * up + Mth.sin(t * 2.2F) * 0.3F * hit;
        rib = 0.25F * up;
    }

    private void snare(float t) {
        float wind = seg(t, 0, 9);
        float plunge = seg(t, 9, 13);
        float rec = seg(t, 34, 44);
        float a = wind * (1 - plunge);
        float b = plunge * (1 - rec);
        float shake = Mth.sin(t * 1.4F) * 0.05F * b;
        armX = -1.7F * a - 0.55F * b + shake;
        armZ = 0.2F * a + 0.35F * b;
        bodyX = -0.1F * a + 0.8F * b;
        bodyY = 4.0F * b;
        headX = 0.25F * b;
        tendril = 0.4F * b * Mth.sin(t * 0.9F);
    }

    private void sink(float t, int dur) {
        float in = seg(t, 0, 6);
        armX = -0.6F * in;
        armZ = 0.7F * in;
        bodyX = 0.35F * in;
        headX = 0.3F * in;
        tendril = 0.3F * Mth.sin(t * 1.1F) * in;
    }

    private void emerge(float t, int dur) {
        float burst = seg(t, 0, 4);
        float rec = seg(t, 8, dur);
        float e = burst * (1 - rec);
        armX = -3.0F * e;
        armZ = 0.55F * e;
        bodyX = -0.35F * e;
        headX = -0.5F * e;
        rib = 0.6F * e;
        tendril = 0.7F * e;
    }

    private void domain(float t, int dur, int stage) {
        float in = seg(t, 0, 12) * (1 - seg(t, dur - 12, dur));
        int interval = stage >= 3 ? 16 : 20;
        float beatPhase = ((t - 14 + 1) % interval + interval) % interval;
        float beat = t > 14 ? (float) Math.pow(Math.max(0, 1 - beatPhase / 6.0F), 2) : 0;
        armX = -0.35F * in;
        armZ = (1.35F + 0.12F * beat) * in;
        bodyX = -0.18F * in;
        headX = -0.45F * in;
        rib = (0.9F + 0.35F * beat) * in;
        tendril = (0.4F + 0.5F * beat) * in;
        pulse = 0.05F * beat * in;
    }

    private void prism(float t, int dur) {
        float in = seg(t, 0, 12) * (1 - seg(t, dur - 10, dur));
        rightArmX = -2.7F * in;
        leftArmX = -1.4F * in;
        float since = t - 28;
        float thrust = since > 0 ? (float) Math.pow(Math.max(0, 1 - (since % 7) / 4.0F), 2) : 0;
        leftArmX -= 0.4F * thrust * in;
        armZ = 0.25F * in;
        headX = -0.3F * in;
        bodyX = -0.08F * in + 0.08F * thrust * in;
        tendril = 0.3F * in;
    }

    private void collapse(float t, int dur) {
        float gather = seg(t, 0, 10);
        float hunch = seg(t, 52, 63);
        float release = seg(t, 63, 67);
        float rec = seg(t, 74, dur);
        float g = gather * (1 - release);
        float r = release * (1 - rec);
        float tremble = Mth.sin(t * 2.3F) * 0.04F * gather * (t / 64F) * (1 - release);
        armX = -1.25F * g - 0.35F * hunch * (1 - release) - 0.6F * r + tremble;
        armZ = -0.45F * g + 1.5F * r;
        bodyX = -0.1F * g + 0.45F * hunch * (1 - release) - 0.45F * r;
        headX = 0.3F * hunch * (1 - release) - 0.6F * r;
        rib = 1.2F * g + 0.9F * r;
        tendril = 0.6F * g * Mth.sin(t * 1.3F) + 0.9F * r;
    }

    private void awaken(float t, int dur) {
        float in = seg(t, 6, 22) * (1 - seg(t, dur - 14, dur));
        armZ = 0.45F * in;
        rib = 1.0F * in;
        tendril = 0.4F * in * Mth.sin(t * 1.5F);
    }

    private void hold(float w) {
        bodyYaw += -0.3F * w;
        rightArmX += -1.45F * w;
        rightArmZ += -0.15F * w;
        leftArmX += -0.3F * w;
        bodyX += 0.15F * w;
        headX += 0.15F * w;
    }

    private void grab(float t, int dur) {
        float wind = seg(t, 0, 9);
        float swing = seg(t, 9, 13);
        float rec = seg(t, 15, dur);
        float w = wind * (1 - swing);
        float g = swing * (1 - rec);
        bodyYaw = 0.55F * w;
        rightArmX = -1.9F * w;
        rightArmZ = 1.1F * w;
        leftArmX = -0.25F * w;
        bodyX = -0.1F * w;
        headX = -0.1F * w;
        rib = 0.25F * w;
        tendril = 0.3F * w;
        hold(g);
    }

    private void liftSlam(float t, int dur) {
        float lift = seg(t, 0, 14);
        float slam = seg(t, 18, 21);
        float ret = seg(t, 23, 28);
        float keep = dur > 30 ? 1 - seg(t, 28, dur) : 1;
        float up = lift * (1 - slam) * keep;
        float dn = slam * (1 - ret) * keep;
        hold(keep - up - dn);
        bodyYaw += 0.15F * up - 0.45F * dn;
        rightArmX += -2.95F * up - 0.75F * dn;
        rightArmZ += 0.25F * up - 0.25F * dn;
        leftArmX += -0.2F * up + 0.2F * dn;
        bodyX += -0.3F * up + 0.6F * dn;
        bodyY += 3.0F * dn;
        headX += -0.45F * up + 0.3F * dn;
        rib += 0.4F * up;
        tendril += 0.4F * up + 0.5F * dn * Mth.sin(t * 2.0F);
    }

    private void stun(float t, int dur) {
        float in = seg(t, 0, 6) * (1 - seg(t, dur - 8, dur));
        float sway = Mth.sin(t * 0.25F);
        armX = 0.35F * in;
        armZ = (0.1F + 0.08F * sway) * in;
        bodyX = 0.38F * in;
        bodyY = 2.2F * in;
        headX = (0.65F + 0.1F * sway) * in;
        rib = -0.1F * in;
        tendril = -0.35F * in;
    }

    private void sonic(float t, int dur) {
        float in = seg(t, 0, 6) * (1 - seg(t, dur - 5, dur));
        float shake = Mth.sin(t * 2.6F) * 0.05F * in * Math.min(1, t / Math.max(1, dur - 6));
        armX = 0.45F * in + shake;
        armZ = 1.0F * in;
        bodyX = -0.22F * in;
        headX = -0.55F * in;
        rib = 1.4F * in;
        tendril = (0.6F + 0.3F * Mth.sin(t * 1.8F)) * in;
    }

    private void blink(float t, int dur) {
        float slamAt = dur - 14;
        float prep = slamAt - 14;
        float crouch = seg(t, 0, 6) * (1 - seg(t, prep, prep + 2));
        float raise = seg(t, prep + 1, slamAt - 2);
        float down = seg(t, slamAt - 2, slamAt + 1);
        float rec = seg(t, slamAt + 4, dur);
        float up = raise * (1 - down);
        float hit = down * (1 - rec);
        armX = 0.7F * crouch - 2.9F * up - 1.0F * hit;
        armZ = 0.5F * crouch + 0.3F * up + 0.15F * hit;
        bodyX = 0.45F * crouch - 0.28F * up + 0.6F * hit;
        bodyY = 2.0F * crouch + 3.0F * hit;
        headX = 0.3F * crouch - 0.35F * up + 0.2F * hit;
        tendril = 0.4F * crouch * Mth.sin(t * 1.4F) + 0.5F * up;
        rib = 0.3F * crouch + 0.25F * up;
    }

    private void swap(float t, int dur) {
        float point = seg(t, 0, 6) * (1 - seg(t, 18, 21));
        float s = seg(t, 21, 26) * (1 - seg(t, dur - 5, dur));
        rightArmX = -2.4F * point;
        leftArmX = 0.3F * point;
        armZ = 0.15F * point + 1.0F * s;
        armX = 0.45F * s;
        headX = -0.15F * point - 0.55F * s;
        bodyX = -0.22F * s;
        rib = 0.3F * point + 1.4F * s;
        tendril = 0.5F * point * Mth.sin(t * 1.2F) + 0.6F * s;
    }

    private void write(Parts p) {
        p.rightArm.xRot += armX + rightArmX;
        p.leftArm.xRot += armX + leftArmX;
        p.rightArm.zRot += armZ + rightArmZ;
        p.leftArm.zRot -= armZ + leftArmZ;
        p.body.yRot += bodyYaw;
        p.body.xRot += bodyX;
        p.body.y += bodyY;
        p.head.xRot += headX;
        p.rightRib.yRot += rib;
        p.leftRib.yRot -= rib;
        p.rightTendril.xRot -= tendril;
        p.leftTendril.xRot += tendril;
        if (pulse > 0) {
            p.body.xScale += pulse;
            p.body.zScale += pulse;
        }
    }
}
