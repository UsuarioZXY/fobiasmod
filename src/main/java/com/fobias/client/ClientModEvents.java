package com.fobias.client;

import com.fobias.FobiasMod;
import com.fobias.item.FlashlightItem;
import com.fobias.registry.ModEntities;
import com.fobias.registry.ModItems;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = FobiasMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Cambia la textura de la linterna segun este encendida o apagada.
        event.enqueueWork(() -> ItemProperties.register(
                ModItems.FLASHLIGHT.get(),
                new ResourceLocation(FobiasMod.MODID, "on"),
                (stack, level, entity, seed) -> FlashlightItem.isOn(stack) ? 1.0F : 0.0F));
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(StalkerModel.LAYER, StalkerModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.STALKER.get(), StalkerRenderer::new);
    }
}
