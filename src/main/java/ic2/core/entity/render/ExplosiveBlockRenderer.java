/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  org.joml.Vector3f
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.block.BlockRenderDispatcher
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.TntMinecartRenderer
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.block.state.BlockState
 */
package ic2.core.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector3f;
import com.mojang.math.Axis;
import ic2.api.entity.block.ExplosiveEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

public class ExplosiveBlockRenderer
extends EntityRenderer<ExplosiveEntity> {
    private final BlockRenderDispatcher blockRenderManager;

    public ExplosiveBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5f;
        this.blockRenderManager = context.getBlockRenderDispatcher();
    }

    public void render(ExplosiveEntity explosiveEntity, float f, float f2, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        poseStack.pushPose();
        poseStack.translate(0.0, 0.5, 0.0);
        int n2 = explosiveEntity.getFuse();
        if ((float)n2 - f2 + 1.0f < 10.0f) {
            float f3 = 1.0f - ((float)n2 - f2 + 1.0f) / 10.0f;
            f3 = Mth.clamp((float)f3, (float)0.0f, (float)1.0f);
            f3 *= f3;
            f3 *= f3;
            float f4 = 1.0f + f3 * 0.3f;
            poseStack.scale(f4, f4, f4);
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0f));
        poseStack.translate(-0.5, -0.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
        TntMinecartRenderer.renderWhiteSolidBlock((BlockRenderDispatcher)this.blockRenderManager, (BlockState)explosiveEntity.renderBlockState, (PoseStack)poseStack, (MultiBufferSource)multiBufferSource, (int)n, (n2 / 5 % 2 == 0 ? 1 : 0) != 0);
        poseStack.popPose();
        super.render(explosiveEntity, f, f2, poseStack, multiBufferSource, n);
    }

    public ResourceLocation getTextureLocation(ExplosiveEntity explosiveEntity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

