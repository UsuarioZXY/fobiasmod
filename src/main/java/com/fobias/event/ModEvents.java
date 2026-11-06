package com.fobias.event;

import com.fobias.FobiasMod;
import com.fobias.entity.StalkerEntity;
import com.fobias.registry.ModEntities;
import com.fobias.registry.ModItems;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Eventos del bus del mod (atributos y pestaña creativa). */
@Mod.EventBusSubscriber(modid = FobiasMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {

    @SubscribeEvent
    public static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.STALKER.get(), StalkerEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.PURGATORY_ORB.get());
            event.accept(ModItems.FLASHLIGHT.get());
        }
    }
}
