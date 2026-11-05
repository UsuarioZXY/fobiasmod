package com.fobias.registry;

import com.fobias.FobiasMod;
import com.fobias.item.DarkOrbItem;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, FobiasMod.MODID);

    /** Orbe que lleva al bosque oscuro. Los orbes de los otros mundos se agregan aqui. */
    public static final RegistryObject<Item> DARK_ORB = ITEMS.register("dark_orb",
            () -> new DarkOrbItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    /** Linterna: lo unico que tienes dentro del bosque oscuro. */
    public static final RegistryObject<Item> FLASHLIGHT = ITEMS.register("flashlight",
            () -> new Item(new Item.Properties().stacksTo(1)));

    private ModItems() {
    }
}
