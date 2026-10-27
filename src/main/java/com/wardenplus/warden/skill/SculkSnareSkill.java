package com.wardenplus.warden.skill;

import com.wardenplus.network.Vfx;
import com.wardenplus.warden.Combat;
import com.wardenplus.warden.SkillType;
import com.wardenplus.warden.WardenController;
import com.wardenplus.warden.hazard.SnareHazard;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class SculkSnareSkill extends WardenSkill {
    private static final int CAST = 10;
    private static final int TELEGRAPH = 22;

    @Override
    public int duration() {
        return 44;
    }

    @Override
    public void start(WardenController c, LivingEntity target) {
        c.stopMoving();
        Vfx.anim(c.warden, Vfx.ANIM_SNARE, duration());
        sound(c.warden, SoundEvents.WARDEN_DIG, 2.0F, 1.3F);
    }

    @Override
    public void tick(WardenController c, LivingEntity target, int t) {
        Warden w = c.warden;
        c.stopMoving();
        if (t < CAST) {
            c.face(c.aimPos(target));
        }
        if (t != CAST) {
            return;
        }
        RandomSource random = w.getRandom();
        float damage = Combat.damage(w, SkillType.SCULK_SNARE);
        List<Vec3> spots = new ArrayList<>();
        Vec3 base = c.aimPos(target);
        if (target != null) {
            Vec3 v = target.getDeltaMovement();
            base = base.add(Mth.clamp(v.x * 10, -4, 4), 0, Mth.clamp(v.z * 10, -4, 4));
        }
        spots.add(base);
        int count = c.stage() == 1 ? 3 : c.stage() == 2 ? 5 : 7;
        for (int i = 1; i < count; i++) {
            double a = random.nextDouble() * Math.PI * 2;
            double r = 1.8 + random.nextDouble() * 2.6;
            spots.add(base.add(Math.cos(a) * r, 0, Math.sin(a) * r));
        }
        int extra = 0;
        for (Player p : w.level().getEntitiesOfClass(Player.class, w.getBoundingBox().inflate(22))) {
            if (p != target && w.canTargetEntity(p) && extra < 3) {
                spots.add(p.position());
                extra++;
            }
        }
        for (int i = 0; i < spots.size(); i++) {
            Vec3 s = spots.get(i);
            double gy = Combat.groundY(w.level(), s.x, s.y, s.z);
            Vec3 pos = new Vec3(s.x, gy, s.z);
            int delay = TELEGRAPH + i * 2;
            c.addHazard(new SnareHazard(pos, delay, 1.5, damage));
            Vfx.send(w, Vfx.SNARE, delay + 34, Vfx.pos(pos, delay));
        }
        sound(w, base, SoundEvents.SCULK_CATALYST_BLOOM, 3.0F, 0.6F);
    }
}
