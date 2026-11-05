package com.fobias.client;

import com.fobias.FobiasMod;
import com.fobias.entity.StalkerEntity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/** Capa de ojos que se dibuja a brillo maximo, visible incluso a oscuras. */
public class StalkerEyesLayer extends EyesLayer<StalkerEntity, HumanoidModel<StalkerEntity>> {
    private static final RenderType EYES = RenderType.eyes(
            new ResourceLocation(FobiasMod.MODID, "textures/entity/stalker_eyes.png"));

    public StalkerEyesLayer(RenderLayerParent<StalkerEntity, HumanoidModel<StalkerEntity>> parent) {
        super(parent);
    }

    @Override
    public RenderType renderType() {
        return EYES;
    }
}
