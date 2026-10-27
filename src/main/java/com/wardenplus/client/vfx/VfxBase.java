package com.wardenplus.client.vfx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class VfxBase {
    public static final float[] CYAN = {0.22F, 0.92F, 1.0F};
    public static final float[] TEAL = {0.04F, 0.5F, 0.62F};
    public static final float[] PALE = {0.78F, 1.0F, 1.0F};
    public static final float[] DEEP = {0.02F, 0.12F, 0.16F};
    public static final float[] VOID = {0.72F, 0.3F, 1.0F};
    public static final float[] VOID_PALE = {0.97F, 0.82F, 1.0F};
    public static final float[] VOID_DIM = {0.38F, 0.08F, 0.6F};
    public static final float[] VOID_DEEP = {0.1F, 0.02F, 0.16F};
    private static final float[][] ECHO_PALETTE = {CYAN, PALE, TEAL, DEEP};
    private static final float[][] ENDER_PALETTE = {VOID, VOID_PALE, VOID_DIM, VOID_DEEP};
    protected static final RandomSource RANDOM = RandomSource.create();
    protected final int entityId;
    protected final int life;
    protected int age;

    protected VfxBase(int entityId, int life) {
        this.entityId = entityId;
        this.life = Math.max(1, life);
    }

    public boolean tick() {
        onTick();
        age++;
        return age < life;
    }

    protected void onTick() {
    }

    public abstract void render(float partial);

    public int entityId() {
        return entityId;
    }

    protected float time(float partial) {
        return age + partial;
    }

    protected static ClientLevel level() {
        return Minecraft.getInstance().level;
    }

    protected Entity entity() {
        ClientLevel level = level();
        return level == null ? null : level.getEntity(entityId);
    }

    protected static Entity entityById(int id) {
        ClientLevel level = level();
        return level == null || id < 0 ? null : level.getEntity(id);
    }

    protected static Vec3 chest(Entity e, float partial) {
        float yaw = (e instanceof net.minecraft.world.entity.LivingEntity le ? Mth.rotLerp(partial, le.yBodyRotO, le.yBodyRot) : e.getYRot()) * Mth.DEG_TO_RAD;
        return e.getPosition(partial).add(-Math.sin(yaw) * 0.6, 1.75, Math.cos(yaw) * 0.6);
    }

    protected static Player localPlayer() {
        return Minecraft.getInstance().player;
    }

    protected static void particle(ParticleOptions options, double x, double y, double z, double vx, double vy, double vz) {
        ClientLevel level = level();
        if (level != null) {
            level.addParticle(options, x, y, z, vx, vy, vz);
        }
    }

    protected static void debris(double x, double y, double z, double vx, double vy, double vz) {
        ClientLevel level = level();
        if (level == null) {
            return;
        }
        BlockState state = level.getBlockState(BlockPos.containing(x, y - 0.5, z));
        if (state.isAir() || state.getRenderShape() == RenderShape.INVISIBLE) {
            state = net.minecraft.world.level.block.Blocks.SCULK.defaultBlockState();
        }
        level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), x, y + 0.1, z, vx, vy, vz);
    }

    protected static float ease(float x) {
        x = Mth.clamp(x, 0, 1);
        return 1 - (1 - x) * (1 - x) * (1 - x);
    }

    protected static float smooth(float x) {
        x = Mth.clamp(x, 0, 1);
        return x * x * (3 - 2 * x);
    }

    public static float[][] palette(int id) {
        return id == 1 ? ENDER_PALETTE : ECHO_PALETTE;
    }

    protected static Vec3 v(float[] d, int i) {
        return new Vec3(d[i], d[i + 1], d[i + 2]);
    }
}
