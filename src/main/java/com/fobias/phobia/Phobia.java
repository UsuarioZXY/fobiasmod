package com.fobias.phobia;

import java.util.Optional;

import net.minecraft.network.chat.Component;

/**
 * Lista de fobias del mod. Para agregar una nueva: sumar un valor aqui,
 * agregar sus textos en los archivos de idioma y su efecto en PhobiaEffects.
 */
public enum Phobia {
    ARACHNOPHOBIA("aracnofobia"),
    CLAUSTROPHOBIA("claustrofobia"),
    NYCTOPHOBIA("nictofobia");

    private final String id;

    Phobia(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Component getDisplayName() {
        return Component.translatable("phobia.fobias." + id);
    }

    public Component getMessage() {
        return Component.translatable("message.fobias." + id);
    }

    public static Optional<Phobia> byId(String id) {
        for (Phobia phobia : values()) {
            if (phobia.id.equalsIgnoreCase(id)) {
                return Optional.of(phobia);
            }
        }
        return Optional.empty();
    }
}
