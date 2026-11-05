package com.fobias.client;

import com.fobias.FobiasMod;
import com.fobias.entity.StalkerEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Silueta negra y alta con ojos que brillan en la oscuridad. */
public class StalkerRenderer extends HumanoidMobRenderer<StalkerEntity, HumanoidModel<StalkerEntity>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(FobiasMod.MODID, "textures/entity/stalker.png");

    public StalkerRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        this.addLayer(new StalkerEyesLayer(this));
    }

    @Override
    protected void scale(StalkerEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(1.0F, 1.25F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(StalkerEntity entity) {
        return TEXTURE;
    }
}
