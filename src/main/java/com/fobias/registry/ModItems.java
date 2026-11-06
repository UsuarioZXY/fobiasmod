package com.fobias.registry;

import com.fobias.FobiasMod;
import com.fobias.item.FlashlightItem;
import com.fobias.item.PurgatoryOrbItem;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, FobiasMod.MODID);

    /** Orbe que lleva al Nivel 0. Los orbes de los otros niveles se agregan aqui. */
    public static final RegistryObject<Item> PURGATORY_ORB = ITEMS.register("purgatory_orb",
            () -> new PurgatoryOrbItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    /** Linterna: clic derecho para encender/apagar. */
    public static final RegistryObject<Item> FLASHLIGHT = ITEMS.register("flashlight",
            () -> new FlashlightItem(new Item.Properties().stacksTo(1)));

    private ModItems() {
    }
}
