/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererManager
 *  net.minecraft.resources.ResourceLocation
 */
package ic2.core.item.tool;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.item.tool.EntityParticle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RenderBillboardEntity
extends EntityRenderer<EntityParticle> {
    private final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("ic2", "textures/models/beam.png");

    public RenderBillboardEntity(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EntityParticle entity, float yaw, float partialTickTime, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
    }

    @Override
    public ResourceLocation getTextureLocation(EntityParticle entity) {
        return this.texture;
    }
}

