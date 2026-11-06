package com.fobias.client;

import com.fobias.FobiasMod;
import com.fobias.entity.StalkerEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class StalkerRenderer extends MobRenderer<StalkerEntity, StalkerModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(FobiasMod.MODID, "textures/entity/stalker.png");

    public StalkerRenderer(EntityRendererProvider.Context context) {
        super(context, new StalkerModel(context.bakeLayer(StalkerModel.LAYER)), 0.35F);
        this.addLayer(new StalkerEyesLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(StalkerEntity entity) {
        return TEXTURE;
    }
}
