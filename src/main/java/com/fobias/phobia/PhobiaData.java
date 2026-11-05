package com.fobias.phobia;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

/**
 * Guarda las fobias de cada jugador en sus datos persistentes
 * (se conservan al morir y al reiniciar el mundo).
 */
public final class PhobiaData {
    // Etiqueta que Forge conserva cuando el jugador muere y reaparece.
    private static final String PERSISTED = "PlayerPersisted";
    private static final String KEY = "fobias_phobias";

    private PhobiaData() {
    }

    private static CompoundTag storage(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(PERSISTED, Tag.TAG_COMPOUND)) {
            root.put(PERSISTED, new CompoundTag());
        }
        CompoundTag persisted = root.getCompound(PERSISTED);
        if (!persisted.contains(KEY, Tag.TAG_COMPOUND)) {
            persisted.put(KEY, new CompoundTag());
        }
        return persisted.getCompound(KEY);
    }

    public static boolean has(Player player, Phobia phobia) {
        return storage(player).getBoolean(phobia.getId());
    }

    public static void add(Player player, Phobia phobia) {
        storage(player).putBoolean(phobia.getId(), true);
    }

    public static void remove(Player player, Phobia phobia) {
        storage(player).remove(phobia.getId());
    }

    public static void clear(Player player) {
        for (Phobia phobia : Phobia.values()) {
            remove(player, phobia);
        }
    }

    public static List<Phobia> getAll(Player player) {
        List<Phobia> result = new ArrayList<>();
        for (Phobia phobia : Phobia.values()) {
            if (has(player, phobia)) {
                result.add(phobia);
            }
        }
        return result;
    }
}
