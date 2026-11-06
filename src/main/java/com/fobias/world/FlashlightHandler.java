package com.fobias.world;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.joml.Vector3f;

import com.fobias.item.FlashlightItem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Luz dinamica de la linterna.
 *
 * Mientras esta encendida pone bloques de luz invisibles a lo largo del haz
 * (cuatro puntos). Cuanto mas lejos apuntas, mas fuerte es la luz del punto
 * final, asi que el terreno lejano se ve mejor y mas grande. Ademas dibuja
 * particulas de polvo que forman un cono visible en la oscuridad.
 */
public final class FlashlightHandler {
    /** Alcance del haz, en bloques. */
    public static final int RANGE = 24;

    private static final float[] FRACTIONS = {0.30F, 0.55F, 0.80F, 1.0F};
    private static final int[] LIGHT_DROP = {5, 3, 1, 0};

    private static final DustParticleOptions BEAM_DUST =
            new DustParticleOptions(new Vector3f(1.0F, 0.95F, 0.72F), 0.45F);

    private record LightState(Level level, Map<BlockPos, Integer> blocks) {
    }

    private static final Map<UUID, LightState> LIGHTS = new HashMap<>();

    private FlashlightHandler() {
    }

    /** True si el jugador sostiene una linterna encendida. */
    public static boolean isOn(Player player) {
        return FlashlightItem.isOn(player.getMainHandItem()) || FlashlightItem.isOn(player.getOffhandItem());
    }

    /** Se llama cada 2 ticks por jugador (solo servidor). */
    public static void tick(ServerPlayer player) {
        UUID id = player.getUUID();
        ServerLevel level = player.serverLevel();

        if (!player.isAlive() || !isOn(player)) {
            remove(id);
            return;
        }

        LightState current = LIGHTS.get(id);
        if (current != null && current.level() != level) {
            remove(id);
            current = null;
        }

        Vec3 eye = player.getEyePosition();
        Vec3 dir = player.getViewVector(1.0F);
        Vec3 end = eye.add(dir.scale(RANGE));
        BlockHitResult hit = level.clip(new ClipContext(
                eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

        boolean hitSomething = hit.getType() != HitResult.Type.MISS;
        double distance = hitSomething ? eye.distanceTo(hit.getLocation()) : RANGE;

        // Cuanto mas lejos apuntas, mas intensa es la luz del final del haz.
        int base = Mth.clamp(5 + (int) (distance * 0.45D), 5, 14);

        Map<BlockPos, Integer> wanted = new LinkedHashMap<>();
        for (int i = 0; i < FRACTIONS.length; i++) {
            double d = distance * FRACTIONS[i];
            if (i == FRACTIONS.length - 1 && hitSomething) {
                d -= 0.5D;
            }
            BlockPos pos = BlockPos.containing(eye.add(dir.scale(Math.max(1.0D, d))));
            if (!isFree(level, pos)) {
                continue;
            }
            int lightLevel = Math.max(1, base - LIGHT_DROP[i]);
            wanted.merge(pos, lightLevel, Math::max);
        }

        if (current == null || !current.blocks().equals(wanted)) {
            if (current != null) {
                for (BlockPos old : current.blocks().keySet()) {
                    if (!wanted.containsKey(old)) {
                        clearLight(level, old);
                    }
                }
            }
            for (Map.Entry<BlockPos, Integer> entry : wanted.entrySet()) {
                Integer previous = current == null ? null : current.blocks().get(entry.getKey());
                if (previous == null || !previous.equals(entry.getValue())) {
                    level.setBlock(entry.getKey(),
                            Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, entry.getValue()), 3);
                }
            }
            LIGHTS.put(id, new LightState(level, wanted));
        }

        spawnBeam(level, eye, dir, distance);
    }

    /** Cono de polvo: se ensancha con la distancia, para que el haz se vea a lo lejos. */
    private static void spawnBeam(ServerLevel level, Vec3 eye, Vec3 dir, double distance) {
        Vec3 origin = eye.add(0.0D, -0.25D, 0.0D);
        int points = 5;
        for (int k = 1; k <= points; k++) {
            double d = 1.5D + (distance - 1.5D) * k / points;
            if (d <= 1.5D) {
                continue;
            }
            Vec3 p = origin.add(dir.scale(d));
            double spread = 0.04D + d * 0.035D;
            level.sendParticles(BEAM_DUST, p.x, p.y, p.z, 2, spread, spread, spread, 0.0D);
        }
    }

    /** Apaga y elimina la luz de un jugador. */
    public static void remove(UUID id) {
        LightState state = LIGHTS.remove(id);
        if (state == null) {
            return;
        }
        for (BlockPos pos : state.blocks().keySet()) {
            clearLight(state.level(), pos);
        }
    }

    private static void clearLight(Level level, BlockPos pos) {
        if (level.isLoaded(pos) && level.getBlockState(pos).is(Blocks.LIGHT)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private static boolean isFree(Level level, BlockPos pos) {
        if (!level.isLoaded(pos) || level.isOutsideBuildHeight(pos)) {
            return false;
        }
        return level.getBlockState(pos).isAir() || level.getBlockState(pos).is(Blocks.LIGHT);
    }
}
