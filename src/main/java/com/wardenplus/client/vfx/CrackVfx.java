package com.wardenplus.client.vfx;

import com.wardenplus.client.Draw;
import com.wardenplus.client.WpRenderTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class CrackVfx extends VfxBase {
    private final Vec3 c;
    private final float radius;
    private final float rot;
    private final float[][] pal;

    public CrackVfx(int id, Vec3 center, float radius, int life, int palette) {
        super(id, life);
        this.c = center;
        this.radius = radius;
        this.rot = RANDOM.nextFloat() * Mth.TWO_PI;
        this.pal = palette(palette);
    }

    @Override
    public void render(float partial) {
        float t = time(partial);
        float in = Math.min(1, t / 3.0F);
        float out = (float) Math.pow(Math.max(0, 1 - t / life), 0.8);
        float a = in * out;
        float grow = 0.6F + 0.4F * ease(t / 5.0F);
        Draw.flat(Draw.shade(WpRenderTypes.CRACK), c.x, c.y + 0.02, c.z, radius * grow, rot, pal[3][0], pal[3][1], pal[3][2], 0.85F * a);
        float pulse = 0.6F + 0.4F * Mth.sin(t * 0.35F);
        Draw.flat(Draw.add(WpRenderTypes.CRACK), c.x, c.y + 0.035, c.z, radius * grow, rot, pal[0][0], pal[0][1], pal[0][2], a * pulse);
        Draw.flat(Draw.add(WpRenderTypes.GLOW), c.x, c.y + 0.03, c.z, radius * 1.25F * grow, 0, pal[2][0], pal[2][1], pal[2][2], 0.45F * a * (1 - Math.min(1, t / 20F) * 0.6F));
    }
}
