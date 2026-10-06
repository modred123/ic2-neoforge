/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 */
package ic2.core.block.beam;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import ic2.core.block.beam.ParticleEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class BeamRenderer
extends EntityRenderer<ParticleEntity> {
    private static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("ic2", "textures/models/beam.png");

    public BeamRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(ParticleEntity particleEntity, float f, float f2, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        float f3 = 0.0f;
        float f4 = 1.0f;
        float f5 = 0.0f;
        float f6 = 1.0f;
        float f7 = 0.1f;
        int n2 = 255;
        int n3 = 255;
        int n4 = 255;
        int n5 = 255;
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.itemEntityTranslucentCull((ResourceLocation)texture));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();
        for (int i = 0; i < 4; ++i) {
            float f8;
            float f9;
            float f10;
            float f11;
            if (i < 2) {
                f11 = -f7;
                f10 = f3;
            } else {
                f11 = f7;
                f10 = f4;
            }
            if (i == 0 || i == 3) {
                f9 = -f7;
                f8 = f5;
            } else {
                f9 = f7;
                f8 = f6;
            }
            vertexConsumer.addVertex(matrix4f, f11, f9, 0.0f).setColor(n2, n3, n4, n5).setUv(f10, f8).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(n & 0xFFFF, n >> 16 & 0xFFFF).setNormal(0.0f, 1.0f, 0.0f);
        }
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ParticleEntity particleEntity) {
        return texture;
    }
}

