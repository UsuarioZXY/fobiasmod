package com.fobias.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

/** Datos del jugador que Forge conserva incluso despues de morir. */
public final class PlayerStore {
    private static final String PERSISTED = "PlayerPersisted";

    private PlayerStore() {
    }

    public static CompoundTag persisted(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(PERSISTED, Tag.TAG_COMPOUND)) {
            root.put(PERSISTED, new CompoundTag());
        }
        return root.getCompound(PERSISTED);
    }
}
