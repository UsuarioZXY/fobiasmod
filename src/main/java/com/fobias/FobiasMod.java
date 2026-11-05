package com.fobias;

import com.fobias.registry.ModEntities;
import com.fobias.registry.ModItems;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Clase principal del mod. Los eventos y comandos se registran solos
 * mediante @Mod.EventBusSubscriber en sus propias clases.
 */
@Mod(FobiasMod.MODID)
public class FobiasMod {
    public static final String MODID = "fobias";

    public FobiasMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
    }
}
