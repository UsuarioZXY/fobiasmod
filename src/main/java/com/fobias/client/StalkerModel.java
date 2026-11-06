package com.fobias.client;

import com.fobias.FobiasMod;
import com.fobias.entity.StalkerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Modelo del Sonriente: 43 px de alto (2,7 bloques), muy flaco, con cabeza ancha
 * de gran sonrisa, brazos que casi tocan el suelo y dedos larguisimos.
 *
 * Jerarquia: root > body > (neck > head, armL/armR > fore > hand > fingers)
 *            root > legL/legR > shin > foot
 * Las animaciones son procedurales y dependen del estado del monstruo.
 */
public class StalkerModel extends EntityModel<StalkerEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(FobiasMod.MODID, "stalker"), "main");

    private static final float DEG = (float) (Math.PI / 180.0D);
    private static final int FINGERS = 3;

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart armL;
    private final ModelPart armR;
    private final ModelPart foreL;
    private final ModelPart foreR;
    private final ModelPart handL;
    private final ModelPart handR;
    private final ModelPart[] fingersL = new ModelPart[FINGERS];
    private final ModelPart[] fingersR = new ModelPart[FINGERS];
    private final ModelPart legL;
    private final ModelPart legR;
    private final ModelPart shinL;
    private final ModelPart shinR;
    private final ModelPart footL;
    private final ModelPart footR;
    private final ModelPart[] all;

    public StalkerModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.neck = body.getChild("neck");
        this.head = neck.getChild("head");

        this.armL = body.getChild("armL");
        this.foreL = armL.getChild("fore");
        this.handL = foreL.getChild("hand");
        this.armR = body.getChild("armR");
        this.foreR = armR.getChild("fore");
        this.handR = foreR.getChild("hand");
        for (int i = 0; i < FINGERS; i++) {
            fingersL[i] = handL.getChild("finger" + i);
            fingersR[i] = handR.getChild("finger" + i);
        }

        this.legL = root.getChild("legL");
        this.shinL = legL.getChild("shin");
        this.footL = shinL.getChild("foot");
        this.legR = root.getChild("legR");
        this.shinR = legR.getChild("shin");
        this.footR = shinR.getChild("foot");

        java.util.List<ModelPart> parts = new java.util.ArrayList<>(java.util.List.of(
                body, neck, head, armL, armR, foreL, foreR, handL, handR,
                legL, legR, shinL, shinR, footL, footR));
        parts.addAll(java.util.List.of(fingersL));
        parts.addAll(java.util.List.of(fingersR));
        this.all = parts.toArray(new ModelPart[0]);
    }

    // ------------------------------------------------------------------
    // Geometria (el suelo esta en y = 24)
    // ------------------------------------------------------------------
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Torso estrecho; el pivote esta en la cadera (y = 6).
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 22).addBox(-3.0F, -12.0F, -1.5F, 6.0F, 12.0F, 3.0F),
                PartPose.offset(0.0F, 6.0F, 0.0F));

        PartDefinition neck = body.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, -12.0F, 0.0F));

        // Cabeza ancha y alta: aqui va la gran sonrisa.
        neck.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -3.0F, 8.0F, 10.0F, 6.0F),
                PartPose.offset(0.0F, -3.0F, 0.0F));

        addArm(body, "armL", 4.0F);
        addArm(body, "armR", -4.0F);
        addLeg(root, "legL", 2.0F);
        addLeg(root, "legR", -2.0F);

        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void addArm(PartDefinition body, String name, float x) {
        PartDefinition arm = body.addOrReplaceChild(name,
                CubeListBuilder.create().texOffs(18, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 10.0F, 2.0F),
                PartPose.offset(x, -11.0F, 0.0F));
        PartDefinition fore = arm.addOrReplaceChild("fore",
                CubeListBuilder.create().texOffs(26, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F),
                PartPose.offset(0.0F, 10.0F, 0.0F));
        PartDefinition hand = fore.addOrReplaceChild("hand",
                CubeListBuilder.create().texOffs(34, 16).addBox(-1.5F, 0.0F, -0.5F, 3.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 11.0F, 0.0F));
        for (int i = 0; i < FINGERS; i++) {
            hand.addOrReplaceChild("finger" + i,
                    CubeListBuilder.create().texOffs(42, 16).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F),
                    PartPose.offset(-1.0F + i, 2.0F, 0.0F));
        }
    }

    private static void addLeg(PartDefinition root, String name, float x) {
        PartDefinition leg = root.addOrReplaceChild(name,
                CubeListBuilder.create().texOffs(46, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 9.0F, 2.0F),
                PartPose.offset(x, 6.0F, 0.0F));
        PartDefinition shin = leg.addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(54, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 9.0F, 2.0F),
                PartPose.offset(0.0F, 9.0F, 0.0F));
        shin.addOrReplaceChild("foot",
                CubeListBuilder.create().texOffs(46, 28).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 1.0F, 4.0F),
                PartPose.offset(0.0F, 9.0F, 0.0F));
    }

    // ------------------------------------------------------------------
    // Animaciones
    // ------------------------------------------------------------------
    private void resetPose() {
        for (ModelPart part : all) {
            part.xRot = 0.0F;
            part.yRot = 0.0F;
            part.zRot = 0.0F;
        }
    }

    @Override
    public void setupAnim(StalkerEntity entity, float limbSwing, float limbSwingAmount,
                          float age, float netHeadYaw, float headPitch) {
        resetPose();

        int state = entity.getState();
        float amt = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
        float swing = limbSwing * 0.5F;
        float t = age * 0.1F;
        // Tics nerviosos: rafagas breves y poco frecuentes.
        float twitch = Math.max(0.0F, Mth.sin(age * 0.11F) * Mth.sin(age * 0.37F) - 0.8F) * 5.0F;
        float attack = entity.swinging ? Mth.clamp(entity.swingTime / 6.0F, 0.0F, 1.0F) : 0.0F;
        float lunge = Mth.sin(attack * (float) Math.PI);

        switch (state) {
            case StalkerEntity.STATE_HUNTING -> animateHunting(swing, amt, age, t, lunge);
            case StalkerEntity.STATE_STUNNED -> animateStunned(swing, amt, age, t);
            default -> animateWatching(age, t, twitch);
        }

        head.yRot += Mth.clamp(netHeadYaw, -70.0F, 70.0F) * DEG;
        head.xRot += Mth.clamp(headPitch, -40.0F, 40.0F) * DEG;
    }

    /** Quieto, con la cabeza ladeada, los dedos moviendose despacio y tics breves. */
    private void animateWatching(float age, float t, float twitch) {
        body.xRot = 0.05F + Mth.sin(t * 0.6F) * 0.015F;
        body.yRot = Mth.sin(t * 0.25F) * 0.06F;

        head.zRot = 0.5F + Mth.sin(t * 0.35F) * 0.06F + Mth.sin(age * 3.1F) * twitch * 0.12F;
        head.yRot = Mth.sin(age * 2.3F) * twitch * 0.3F;

        armL.xRot = 0.1F + Mth.sin(t * 0.5F) * 0.03F;
        armR.xRot = 0.1F + Mth.sin(t * 0.5F + 1.0F) * 0.03F;
        armL.zRot = -0.07F;
        armR.zRot = 0.07F;
        foreL.xRot = -0.35F;
        foreR.xRot = -0.30F;
        handL.xRot = 0.2F;
        handR.xRot = 0.2F;

        for (int i = 0; i < FINGERS; i++) {
            fingersL[i].xRot = -0.2F + Mth.sin(t * 1.3F + i * 1.1F) * 0.2F + twitch * Mth.sin(age * 2.0F + i) * 0.3F;
            fingersR[i].xRot = -0.2F + Mth.sin(t * 1.3F + i * 1.1F + 2.0F) * 0.2F
                    + twitch * Mth.sin(age * 2.2F + i) * 0.3F;
        }

        legL.xRot = 0.04F;
        legR.xRot = -0.04F;
        shinL.xRot = 0.08F;
        shinR.xRot = 0.08F;
    }

    /** Persecucion: inclinado hacia adelante, zancadas enormes, brazos extendidos y dedos abiertos. */
    private void animateHunting(float swing, float amt, float age, float t, float lunge) {
        body.xRot = 0.45F + amt * 0.2F + lunge * 0.35F;
        body.zRot = Mth.cos(swing) * 0.12F * amt;
        body.yRot = Mth.sin(swing) * 0.15F * amt;

        head.xRot = -0.4F - lunge * 0.3F;
        head.zRot = 0.35F + Mth.sin(age * 0.7F) * 0.05F;

        legL.xRot = Mth.cos(swing) * 1.1F * amt;
        legR.xRot = Mth.cos(swing + (float) Math.PI) * 1.1F * amt;
        shinL.xRot = Math.max(0.0F, Mth.sin(swing)) * 1.4F * amt;
        shinR.xRot = Math.max(0.0F, -Mth.sin(swing)) * 1.4F * amt;
        footL.xRot = -shinL.xRot * 0.4F;
        footR.xRot = -shinR.xRot * 0.4F;

        armL.xRot = -1.0F + Mth.cos(swing + (float) Math.PI) * 0.6F * amt - lunge * 0.9F;
        armR.xRot = -1.0F + Mth.cos(swing) * 0.6F * amt - lunge * 0.9F;
        armL.zRot = -0.12F;
        armR.zRot = 0.12F;
        foreL.xRot = -0.5F - lunge * 0.4F;
        foreR.xRot = -0.5F - lunge * 0.4F;

        for (int i = 0; i < FINGERS; i++) {
            float spread = (i - 1) * (0.18F + lunge * 0.3F);
            fingersL[i].zRot = spread + Mth.sin(age * 0.9F + i) * 0.12F;
            fingersR[i].zRot = spread + Mth.sin(age * 0.9F + i + 1.5F) * 0.12F;
            fingersL[i].xRot = -0.1F;
            fingersR[i].xRot = -0.1F;
        }
    }

    /** Iluminado por la linterna: se tapa la cara, tiembla y retrocede a trompicones. */
    private void animateStunned(float swing, float amt, float age, float t) {
        body.xRot = -0.2F;
        body.yRot = Mth.sin(age * 2.5F) * 0.05F;
        body.zRot = Mth.sin(age * 1.9F) * 0.04F;

        head.xRot = 0.35F;
        head.yRot = Mth.sin(age * 1.7F) * 0.3F;
        head.zRot = 0.5F + Mth.sin(age * 2.9F) * 0.08F;

        // Brazo izquierdo sobre la cara; el derecho agarra al aire.
        armL.xRot = -1.9F;
        armL.zRot = -0.2F;
        foreL.xRot = -1.3F;
        armR.xRot = -0.7F + Mth.sin(t * 2.0F) * 0.1F;
        armR.zRot = 0.35F;
        foreR.xRot = -0.4F;

        for (int i = 0; i < FINGERS; i++) {
            fingersL[i].xRot = -0.8F + Mth.sin(age * 1.4F + i) * 0.1F;
            fingersR[i].xRot = -0.6F + Mth.sin(age * 1.1F + i) * 0.2F;
            fingersR[i].zRot = (i - 1) * 0.3F;
        }

        legL.xRot = Mth.cos(swing) * 0.5F * amt - 0.1F;
        legR.xRot = Mth.cos(swing + (float) Math.PI) * 0.5F * amt + 0.1F;
        shinL.xRot = 0.3F;
        shinR.xRot = 0.15F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
