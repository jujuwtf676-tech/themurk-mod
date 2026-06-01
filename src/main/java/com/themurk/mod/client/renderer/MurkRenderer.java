package com.themurk.mod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.themurk.mod.TheMurkMod;
import com.themurk.mod.entity.MurkEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class MurkRenderer extends MobRenderer<MurkEntity, MurkModel<MurkEntity>> {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation(TheMurkMod.MODID, "textures/entity/murk.png");

    public MurkRenderer(EntityRendererProvider.Context context) {
        super(context,
            new MurkModel<>(context.bakeLayer(MurkModel.LAYER_LOCATION)),
            0.5f); // Shadow radius
    }

    @Override
    public ResourceLocation getTextureLocation(MurkEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(MurkEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        // Slight scale pulsing on render
        float pulse = 1.0f + (float) Math.sin(entity.tickCount * 0.08f + partialTick * 0.08f) * 0.03f;
        poseStack.pushPose();
        poseStack.scale(pulse, pulse, pulse);

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        poseStack.popPose();
    }

    /**
     * Use TRANSLUCENT render type so the Murk appears semi-transparent and misty.
     */
    @Override
    protected @Nullable RenderType getRenderType(MurkEntity entity, boolean bodyVisible,
                                                  boolean translucent, boolean glowing) {
        return RenderType.entityTranslucent(TEXTURE);
    }

    @Override
    protected boolean shouldShowName(MurkEntity entity) {
        return false; // No name tag — it's a mystery
    }
}
