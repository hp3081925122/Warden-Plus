package com.wardenplus.client;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.LinkedHashMap;
import java.util.SequencedMap;
import net.minecraft.client.Camera;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class Draw {
    private static final SequencedMap<RenderType, ByteBufferBuilder> FIXED = new LinkedHashMap<>();
    private static final MultiBufferSource.BufferSource BUFFERS = MultiBufferSource.immediateWithBuffers(FIXED, new ByteBufferBuilder(256));
    private static Matrix4f matrix;
    private static double cx;
    private static double cy;
    private static double cz;
    private static Vector3fc left;
    private static Vector3fc up;
    public static float partial;

    private Draw() {
    }

    public static void begin(PoseStack.Pose p, Camera camera, float pt) {
        matrix = p.pose();
        Vec3 c = camera.position();
        cx = c.x;
        cy = c.y;
        cz = c.z;
        left = camera.leftVector();
        up = camera.upVector();
        partial = pt;
    }

    public static void end() {
        BUFFERS.endBatch();
    }

    public static Vec3 camera() {
        return new Vec3(cx, cy, cz);
    }

    public static VertexConsumer add(Identifier tex) {
        return buffer(WpRenderTypes.additive(tex));
    }

    public static VertexConsumer shade(Identifier tex) {
        return buffer(WpRenderTypes.translucent(tex));
    }

    public static VertexConsumer solid() {
        return buffer(WpRenderTypes.SOLID);
    }

    public static VertexConsumer buffer(RenderType type) {
        if (FIXED.isEmpty()) {
            FIXED.put(WpRenderTypes.SOLID, new ByteBufferBuilder(WpRenderTypes.SOLID.bufferSize()));
        }
        FIXED.computeIfAbsent(type, t -> new ByteBufferBuilder(t.bufferSize()));
        return BUFFERS.getBuffer(type);
    }

    public static void v(VertexConsumer vc, double x, double y, double z, float u, float v, float r, float g, float b, float a) {
        vc.addVertex(matrix, (float) (x - cx), (float) (y - cy), (float) (z - cz)).setColor(r, g, b, Mth.clamp(a, 0.0F, 1.0F)).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
    }

    public static void billboard(VertexConsumer vc, double x, double y, double z, float size, float rot, float r, float g, float b, float a) {
        float s = Mth.sin(rot) * size;
        float c = Mth.cos(rot) * size;
        double lx = left.x() * c + up.x() * s;
        double ly = left.y() * c + up.y() * s;
        double lz = left.z() * c + up.z() * s;
        double ux = -left.x() * s + up.x() * c;
        double uy = -left.y() * s + up.y() * c;
        double uz = -left.z() * s + up.z() * c;
        v(vc, x - lx - ux, y - ly - uy, z - lz - uz, 0, 1, r, g, b, a);
        v(vc, x - lx + ux, y - ly + uy, z - lz + uz, 0, 0, r, g, b, a);
        v(vc, x + lx + ux, y + ly + uy, z + lz + uz, 1, 0, r, g, b, a);
        v(vc, x + lx - ux, y + ly - uy, z + lz - uz, 1, 1, r, g, b, a);
    }

    public static void flat(VertexConsumer vc, double x, double y, double z, float radius, float rot, float r, float g, float b, float a) {
        float s = Mth.sin(rot) * radius;
        float c = Mth.cos(rot) * radius;
        v(vc, x - c + s, y, z - s - c, 0, 0, r, g, b, a);
        v(vc, x - c - s, y, z - s + c, 0, 1, r, g, b, a);
        v(vc, x + c - s, y, z + s + c, 1, 1, r, g, b, a);
        v(vc, x + c + s, y, z + s - c, 1, 0, r, g, b, a);
    }

    public static void groundRing(VertexConsumer vc, double x, double y, double z, double inner, double outer, int segs, float r, float g, float b, float a) {
        inner = Math.max(0, inner);
        for (int i = 0; i < segs; i++) {
            double a0 = i * Math.PI * 2 / segs;
            double a1 = (i + 1) * Math.PI * 2 / segs;
            double c0 = Math.cos(a0), s0 = Math.sin(a0), c1 = Math.cos(a1), s1 = Math.sin(a1);
            float v0 = (float) i / segs;
            float v1 = (float) (i + 1) / segs;
            v(vc, x + c0 * inner, y, z + s0 * inner, 0, v0, r, g, b, a);
            v(vc, x + c0 * outer, y, z + s0 * outer, 1, v0, r, g, b, a);
            v(vc, x + c1 * outer, y, z + s1 * outer, 1, v1, r, g, b, a);
            v(vc, x + c1 * inner, y, z + s1 * inner, 0, v1, r, g, b, a);
        }
    }

    public static void wall(VertexConsumer vc, double x, double y, double z, double radius, double height, int segs, float uRepeat, float uOffset, float r, float g, float b, float aBottom, float aTop) {
        for (int i = 0; i < segs; i++) {
            double a0 = i * Math.PI * 2 / segs;
            double a1 = (i + 1) * Math.PI * 2 / segs;
            float u0 = uOffset + uRepeat * i / segs;
            float u1 = uOffset + uRepeat * (i + 1) / segs;
            double x0 = x + Math.cos(a0) * radius, z0 = z + Math.sin(a0) * radius;
            double x1 = x + Math.cos(a1) * radius, z1 = z + Math.sin(a1) * radius;
            v(vc, x0, y, z0, u0, 1, r, g, b, aBottom);
            v(vc, x0, y + height, z0, u0, 0, r, g, b, aTop);
            v(vc, x1, y + height, z1, u1, 0, r, g, b, aTop);
            v(vc, x1, y, z1, u1, 1, r, g, b, aBottom);
        }
    }

    public static void beam(VertexConsumer vc, Vec3 a, Vec3 b, float width, float vOffset, float vRepeat, float r, float g, float bl, float alpha) {
        beam(vc, a, b, width, width, vOffset, vRepeat, r, g, bl, alpha, alpha);
    }

    public static void beam(VertexConsumer vc, Vec3 a, Vec3 b, float wa, float wb, float vOffset, float vRepeat, float r, float g, float bl, float alphaA, float alphaB) {
        Vec3 dir = b.subtract(a);
        Vec3 mid = a.add(b).scale(0.5);
        Vec3 toCam = new Vec3(cx, cy, cz).subtract(mid);
        Vec3 side = dir.cross(toCam);
        if (side.lengthSqr() < 1.0E-8) {
            side = dir.cross(new Vec3(0, 1, 0));
            if (side.lengthSqr() < 1.0E-8) {
                side = new Vec3(1, 0, 0);
            }
        }
        side = side.normalize();
        Vec3 sa = side.scale(wa * 0.5);
        Vec3 sb = side.scale(wb * 0.5);
        v(vc, a.x - sa.x, a.y - sa.y, a.z - sa.z, 0, vOffset, r, g, bl, alphaA);
        v(vc, a.x + sa.x, a.y + sa.y, a.z + sa.z, 1, vOffset, r, g, bl, alphaA);
        v(vc, b.x + sb.x, b.y + sb.y, b.z + sb.z, 1, vOffset + vRepeat, r, g, bl, alphaB);
        v(vc, b.x - sb.x, b.y - sb.y, b.z - sb.z, 0, vOffset + vRepeat, r, g, bl, alphaB);
    }

    public static void sphere(VertexConsumer vc, Vec3 c, double radius, int lat, int lon, float uOffset, float vOffset, float r, float g, float b, float base, float rim, double thetaMax) {
        Vec3 cam = new Vec3(cx, cy, cz);
        for (int i = 0; i < lat; i++) {
            double t0 = thetaMax * i / lat;
            double t1 = thetaMax * (i + 1) / lat;
            for (int j = 0; j < lon; j++) {
                double p0 = Math.PI * 2 * j / lon;
                double p1 = Math.PI * 2 * (j + 1) / lon;
                sphereVertex(vc, c, radius, t0, p0, cam, (float) j / lon, (float) i / lat, uOffset, vOffset, r, g, b, base, rim);
                sphereVertex(vc, c, radius, t1, p0, cam, (float) j / lon, (float) (i + 1) / lat, uOffset, vOffset, r, g, b, base, rim);
                sphereVertex(vc, c, radius, t1, p1, cam, (float) (j + 1) / lon, (float) (i + 1) / lat, uOffset, vOffset, r, g, b, base, rim);
                sphereVertex(vc, c, radius, t0, p1, cam, (float) (j + 1) / lon, (float) i / lat, uOffset, vOffset, r, g, b, base, rim);
            }
        }
    }

    private static void sphereVertex(VertexConsumer vc, Vec3 c, double radius, double theta, double phi, Vec3 cam, float u, float v, float uo, float vo, float r, float g, float b, float base, float rim) {
        double nx = Math.sin(theta) * Math.cos(phi);
        double ny = Math.cos(theta);
        double nz = Math.sin(theta) * Math.sin(phi);
        double x = c.x + nx * radius;
        double y = c.y + ny * radius;
        double z = c.z + nz * radius;
        Vec3 view = new Vec3(x - cam.x, y - cam.y, z - cam.z);
        double len = view.length();
        double dot = len < 1.0E-5 ? 0 : Math.abs((nx * view.x + ny * view.y + nz * view.z) / len);
        float fres = (float) Math.pow(1.0 - dot, 2.0);
        v(vc, x, y, z, u * 4 + uo, v * 2 + vo, r, g, b, base + rim * fres);
    }

    public static void tube(VertexConsumer vc, Vec3[] pts, float[] radii, float r, float g, float b, float tipGlow) {
        int sides = 6;
        Vec3[] prevRing = null;
        for (int i = 0; i < pts.length; i++) {
            Vec3 tangent = i < pts.length - 1 ? pts[i + 1].subtract(pts[i]) : pts[i].subtract(pts[i - 1]);
            tangent = tangent.normalize();
            Vec3 ref = Math.abs(tangent.y) > 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
            Vec3 n1 = tangent.cross(ref).normalize();
            Vec3 n2 = tangent.cross(n1).normalize();
            Vec3[] ring = new Vec3[sides];
            for (int s = 0; s < sides; s++) {
                double a = s * Math.PI * 2 / sides;
                ring[s] = pts[i].add(n1.scale(Math.cos(a) * radii[i])).add(n2.scale(Math.sin(a) * radii[i]));
            }
            if (prevRing != null) {
                float f0 = (float) (i - 1) / (pts.length - 1);
                float f1 = (float) i / (pts.length - 1);
                for (int s = 0; s < sides; s++) {
                    int n = (s + 1) % sides;
                    float shade = 0.55F + 0.45F * (float) Math.max(0, Math.cos(s * Math.PI * 2 / sides));
                    float g0 = tipGlow * f0 * f0;
                    float g1 = tipGlow * f1 * f1;
                    quadSolid(vc, prevRing[s], prevRing[n], ring[n], ring[s], r * shade + g0 * 0.2F, g * shade + g0 * 0.9F, b * shade + g0, r * shade + g1 * 0.2F, g * shade + g1 * 0.9F, b * shade + g1);
                }
            }
            prevRing = ring;
        }
    }

    private static void quadSolid(VertexConsumer vc, Vec3 a, Vec3 b, Vec3 c, Vec3 d, float r0, float g0, float b0, float r1, float g1, float b1) {
        v(vc, a.x, a.y, a.z, 0.5F, 0.5F, r0, g0, b0, 1);
        v(vc, b.x, b.y, b.z, 0.5F, 0.5F, r0, g0, b0, 1);
        v(vc, c.x, c.y, c.z, 0.5F, 0.5F, r1, g1, b1, 1);
        v(vc, d.x, d.y, d.z, 0.5F, 0.5F, r1, g1, b1, 1);
    }

    private static Vec3[] basis(Vec3 normal) {
        Vec3 n = normal.lengthSqr() < 1.0E-6 ? new Vec3(0, 1, 0) : normal.normalize();
        Vec3 ref = Math.abs(n.y) > 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 u = n.cross(ref).normalize();
        Vec3 w = n.cross(u).normalize();
        return new Vec3[]{u, w};
    }

    public static void disc(VertexConsumer vc, Vec3 c, Vec3 normal, float radius, float rot, float r, float g, float b, float a) {
        Vec3[] uv = basis(normal);
        float s = Mth.sin(rot) * radius;
        float co = Mth.cos(rot) * radius;
        Vec3 x = uv[0].scale(co).add(uv[1].scale(s));
        Vec3 y = uv[0].scale(-s).add(uv[1].scale(co));
        v(vc, c.x - x.x - y.x, c.y - x.y - y.y, c.z - x.z - y.z, 0, 0, r, g, b, a);
        v(vc, c.x - x.x + y.x, c.y - x.y + y.y, c.z - x.z + y.z, 0, 1, r, g, b, a);
        v(vc, c.x + x.x + y.x, c.y + x.y + y.y, c.z + x.z + y.z, 1, 1, r, g, b, a);
        v(vc, c.x + x.x - y.x, c.y + x.y - y.y, c.z + x.z - y.z, 1, 0, r, g, b, a);
    }

    public static void ring(VertexConsumer vc, Vec3 c, Vec3 normal, double inner, double outer, int segs, float uOffset, float r, float g, float b, float aInner, float aOuter) {
        Vec3[] uv = basis(normal);
        inner = Math.max(0, inner);
        for (int i = 0; i < segs; i++) {
            double a0 = i * Math.PI * 2 / segs;
            double a1 = (i + 1) * Math.PI * 2 / segs;
            Vec3 d0 = uv[0].scale(Math.cos(a0)).add(uv[1].scale(Math.sin(a0)));
            Vec3 d1 = uv[0].scale(Math.cos(a1)).add(uv[1].scale(Math.sin(a1)));
            float v0 = uOffset + (float) i / segs;
            float v1 = uOffset + (float) (i + 1) / segs;
            v(vc, c.x + d0.x * inner, c.y + d0.y * inner, c.z + d0.z * inner, 0, v0, r, g, b, aInner);
            v(vc, c.x + d0.x * outer, c.y + d0.y * outer, c.z + d0.z * outer, 1, v0, r, g, b, aOuter);
            v(vc, c.x + d1.x * outer, c.y + d1.y * outer, c.z + d1.z * outer, 1, v1, r, g, b, aOuter);
            v(vc, c.x + d1.x * inner, c.y + d1.y * inner, c.z + d1.z * inner, 0, v1, r, g, b, aInner);
        }
    }

    public static void crystal(VertexConsumer vc, Vec3 c, float radius, float height, float spin, float r, float g, float b, float a, boolean solidShade) {
        Vec3 top = c.add(0, height, 0);
        Vec3 bottom = c.add(0, -height, 0);
        Vec3[] ring = new Vec3[4];
        for (int i = 0; i < 4; i++) {
            double ang = spin + i * Math.PI / 2;
            ring[i] = c.add(Math.cos(ang) * radius, 0, Math.sin(ang) * radius);
        }
        for (int i = 0; i < 4; i++) {
            Vec3 p = ring[i];
            Vec3 q = ring[(i + 1) % 4];
            float s = solidShade ? 0.6F + 0.4F * (i % 2) : 1.0F;
            v(vc, top.x, top.y, top.z, 0.5F, 0.0F, r * s, g * s, b * s, a);
            v(vc, p.x, p.y, p.z, 0.0F, 0.5F, r * s, g * s, b * s, a);
            v(vc, q.x, q.y, q.z, 1.0F, 0.5F, r * s, g * s, b * s, a);
            v(vc, top.x, top.y, top.z, 0.5F, 0.0F, r * s, g * s, b * s, a);
            float s2 = s * 0.7F;
            v(vc, bottom.x, bottom.y, bottom.z, 0.5F, 1.0F, r * s2, g * s2, b * s2, a);
            v(vc, q.x, q.y, q.z, 1.0F, 0.5F, r * s2, g * s2, b * s2, a);
            v(vc, p.x, p.y, p.z, 0.0F, 0.5F, r * s2, g * s2, b * s2, a);
            v(vc, bottom.x, bottom.y, bottom.z, 0.5F, 1.0F, r * s2, g * s2, b * s2, a);
        }
    }
}





