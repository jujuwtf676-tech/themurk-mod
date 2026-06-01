package com.themurk.mod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.themurk.mod.entity.MurkEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

import static com.themurk.mod.TheMurkMod.MODID;

/**
 * The Murk is rendered as an amorphous blob of overlapping, slightly-offset spheroid cubes.
 * The whole thing pulses and wavers via the render tick — never quite a fixed shape.
 */
public class MurkModel<T extends MurkEntity> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
        new ModelLayerLocation(new ResourceLocation(MODID, "murk"), "main");

    private final ModelPart core;
    private final ModelPart blobA;
    private final ModelPart blobB;
    private final ModelPart blobC;

    public MurkModel(ModelPart root) {
        this.core  = root.getChild("core");
        this.blobA = root.getChild("blobA");
        this.blobB = root.getChild("blobB");
        this.blobC = root.getChild("blobC");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Central mass
        root.addOrReplaceChild("core",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-6, -12, -6, 12, 14, 12),
            PartPose.offset(0, 12, 0));

        // Side blob A — slightly raised and rotated
        root.addOrReplaceChild("blobA",
            CubeListBuilder.create()
                .texOffs(0, 26)
                .addBox(-5, -10, -5, 10, 10, 10),
            PartPose.offsetAndRotation(-3, 10, 2, 0.1f, 0.3f, -0.1f));

        // Side blob B
        root.addOrReplaceChild("blobB",
            CubeListBuilder.create()
                .texOffs(40, 0)
                .addBox(-4, -8, -4, 9, 10, 9),
            PartPose.offsetAndRotation(2, 9, -2, -0.1f, -0.2f, 0.15f));

        // Top tendril blob
        root.addOrReplaceChild("blobC",
            CubeListBuilder.create()
                .texOffs(40, 19)
                .addBox(-3, -6, -3, 7, 7, 7),
            PartPose.offsetAndRotation(0, 4, 0, 0.2f, 0.5f, 0.1f));

        return LayerDefinition.create(mesh, 64, 48);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        // Slow, unsettling pulsing
        float pulse = (float) Math.sin(ageInTicks * 0.08f) * 0.06f;
        float wobble = (float) Math.cos(ageInTicks * 0.11f) * 0.05f;

        this.core.yRot  = (float) Math.sin(ageInTicks * 0.04f) * 0.08f;
        this.blobA.xRot = 0.1f + wobble;
        this.blobA.yRot = 0.3f + pulse;
        this.blobB.xRot = -0.1f - wobble * 0.7f;
        this.blobB.yRot = -0.2f - pulse * 1.2f;
        this.blobC.yRot = 0.5f + (float) Math.sin(ageInTicks * 0.07f) * 0.15f;

        // Subtle vertical bob
        float bob = (float) Math.sin(ageInTicks * 0.06f) * 0.3f;
        this.core.y  = 12 + bob;
        this.blobA.y = 10 + bob * 0.8f;
        this.blobB.y = 9  + bob * 0.6f;
        this.blobC.y = 4  + bob * 1.2f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float r, float g, float b, float a) {
        core.render(poseStack,  buffer, packedLight, packedOverlay, r, g, b, a);
        blobA.render(poseStack, buffer, packedLight, packedOverlay, r, g, b, a);
        blobB.render(poseStack, buffer, packedLight, packedOverlay, r, g, b, a);
        blobC.render(poseStack, buffer, packedLight, packedOverlay, r, g, b, a);
    }
}
