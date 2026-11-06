package com.fobias.client;

import com.fobias.FobiasMod;
import com.fobias.entity.StalkerEntity;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/** Ojos y dientes que brillan a maximo brillo, visibles incluso en la oscuridad total. */
public class StalkerEyesLayer extends EyesLayer<StalkerEntity, StalkerModel> {
    private static final RenderType EYES = RenderType.eyes(
            new ResourceLocation(FobiasMod.MODID, "textures/entity/stalker_eyes.png"));

    public StalkerEyesLayer(RenderLayerParent<StalkerEntity, StalkerModel> parent) {
        super(parent);
    }

    @Override
    public RenderType renderType() {
        return EYES;
    }
}
