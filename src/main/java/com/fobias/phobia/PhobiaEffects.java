package com.fobias.phobia;

import java.util.List;

import com.fobias.world.DarkForestGame;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Efectos pasivos de cada fobia sobre el jugador.
 * Se llama una vez por segundo desde PhobiaEvents.
 */
public final class PhobiaEffects {
    private static final double SPIDER_RADIUS = 8.0D;
    private static final int DARK_LIGHT_LEVEL = 3;

    private PhobiaEffects() {
    }

    public static void apply(Player player, Phobia phobia) {
        switch (phobia) {
            case ARACHNOPHOBIA -> arachnophobia(player);
            case CLAUSTROPHOBIA -> claustrophobia(player);
            case NYCTOPHOBIA -> nyctophobia(player);
        }
    }

    private static void give(Player player, MobEffect effect, int duration, int amplifier) {
        player.addEffect(new MobEffectInstance(effect, duration, amplifier, true, false, true));
    }

    // ---------------------------------------------------------------
    // Aracnofobia: arañas (y arañas de cueva) cerca
    // ---------------------------------------------------------------
    private static void arachnophobia(Player player) {
        Level level = player.level();
        AABB area = player.getBoundingBox().inflate(SPIDER_RADIUS);
        List<Spider> spiders = level.getEntitiesOfClass(Spider.class, area);
        if (spiders.isEmpty()) {
            return;
        }

        double nearest = Double.MAX_VALUE;
        for (Spider spider : spiders) {
            nearest = Math.min(nearest, spider.distanceTo(player));
        }

        if (!player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
            player.displayClientMessage(Phobia.ARACHNOPHOBIA.getMessage(), true);
        }

        give(player, MobEffects.MOVEMENT_SLOWDOWN, 60, 0);
        if (nearest <= 5.0D) {
            give(player, MobEffects.WEAKNESS, 60, 0);
        }
        if (nearest <= 3.0D) {
            give(player, MobEffects.CONFUSION, 120, 0);
        }
    }

    // ---------------------------------------------------------------
    // Claustrofobia: techo cerca y paredes por casi todos lados
    // ---------------------------------------------------------------
    private static void claustrophobia(Player player) {
        Level level = player.level();
        BlockPos feet = player.blockPosition();
        BlockPos head = feet.above();

        if (!solidWithin(level, head, Direction.UP, 2)) {
            return;
        }

        int walls = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (solidWithin(level, feet, direction, 2) || solidWithin(level, head, direction, 2)) {
                walls++;
            }
        }
        if (walls < 3) {
            return;
        }

        if (!player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            player.displayClientMessage(Phobia.CLAUSTROPHOBIA.getMessage(), true);
        }

        give(player, MobEffects.DIG_SLOWDOWN, 60, 0);
        give(player, MobEffects.DARKNESS, 100, 0);
    }

    private static boolean solidWithin(Level level, BlockPos origin, Direction direction, int distance) {
        for (int i = 1; i <= distance; i++) {
            if (level.getBlockState(origin.relative(direction, i)).blocksMotion()) {
                return true;
            }
        }
        return false;
    }

    // ---------------------------------------------------------------
    // Nictofobia: lugares con muy poca luz (fuera del bosque oscuro)
    // ---------------------------------------------------------------
    private static void nyctophobia(Player player) {
        Level level = player.level();
        if (level.dimension().equals(DarkForestGame.DIMENSION)) {
            return;
        }
        if (level.getMaxLocalRawBrightness(player.blockPosition()) > DARK_LIGHT_LEVEL) {
            return;
        }

        if (!player.hasEffect(MobEffects.WEAKNESS)) {
            player.displayClientMessage(Phobia.NYCTOPHOBIA.getMessage(), true);
        }

        give(player, MobEffects.WEAKNESS, 60, 0);
        give(player, MobEffects.MOVEMENT_SLOWDOWN, 60, 0);
    }
}
