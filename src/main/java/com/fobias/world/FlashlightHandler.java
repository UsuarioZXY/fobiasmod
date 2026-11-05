package com.fobias.world;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fobias.registry.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Luz dinamica de la linterna: mientras la tienes en la mano se coloca un
 * bloque de luz invisible en el punto al que apuntas (como un foco), y se
 * mueve contigo.
 */
public final class FlashlightHandler {
    /** Alcance del haz, en bloques. */
    private static final int RANGE = 14;
    /** Intensidad de la luz (1-15). */
    private static final int LIGHT_LEVEL = 10;

    private record LightEntry(Level level, BlockPos pos) {
    }

    private static final Map<UUID, LightEntry> LIGHTS = new HashMap<>();

    private FlashlightHandler() {
    }

    public static boolean isHolding(Player player) {
        return player.getMainHandItem().is(ModItems.FLASHLIGHT.get())
                || player.getOffhandItem().is(ModItems.FLASHLIGHT.get());
    }

    /** Se llama cada 2 ticks por jugador (solo servidor). */
    public static void tick(Player player) {
        UUID id = player.getUUID();
        Level level = player.level();

        if (!player.isAlive() || !isHolding(player)) {
            remove(id);
            return;
        }

        LightEntry current = LIGHTS.get(id);
        if (current != null && current.level() != level) {
            remove(id);
            current = null;
        }

        BlockPos target = findTarget(player, level);
        if (target == null) {
            remove(id);
            return;
        }
        if (current != null && current.pos().equals(target)) {
            return;
        }

        remove(id);
        level.setBlock(target, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, LIGHT_LEVEL), 3);
        LIGHTS.put(id, new LightEntry(level, target));
    }

    /** Apaga y elimina la luz de un jugador. */
    public static void remove(UUID id) {
        LightEntry entry = LIGHTS.remove(id);
        if (entry == null) {
            return;
        }
        Level level = entry.level();
        BlockPos pos = entry.pos();
        if (level.isLoaded(pos) && level.getBlockState(pos).is(Blocks.LIGHT)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private static BlockPos findTarget(Player player, Level level) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(RANGE));
        BlockHitResult hit = level.clip(new ClipContext(
                eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

        BlockPos pos;
        if (hit.getType() == HitResult.Type.MISS) {
            pos = BlockPos.containing(end);
        } else {
            pos = hit.getBlockPos().relative(hit.getDirection());
        }

        if (isFree(level, pos)) {
            return pos;
        }
        BlockPos head = BlockPos.containing(eye);
        return isFree(level, head) ? head : null;
    }

    private static boolean isFree(Level level, BlockPos pos) {
        if (!level.isLoaded(pos) || level.isOutsideBuildHeight(pos)) {
            return false;
        }
        return level.getBlockState(pos).isAir() || level.getBlockState(pos).is(Blocks.LIGHT);
    }
}
