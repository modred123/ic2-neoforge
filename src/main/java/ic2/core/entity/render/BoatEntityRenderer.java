/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.datafixers.util.Pair
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 *  net.minecraft.client.model.BoatModel
 *  net.minecraft.client.model.geom.ModelLayerLocation
 *  net.minecraft.client.model.geom.ModelLayers
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.vehicle.Boat
 *  net.minecraft.world.entity.vehicle.Boat$Type
 */
package ic2.core.entity.render;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import com.mojang.math.Axis;
import ic2.api.entity.boat.AbstractBoatEntity;
import ic2.api.entity.boat.BoatType;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;

public class BoatEntityRenderer
extends EntityRenderer<AbstractBoatEntity> {
    private final Map<BoatType, Pair<ResourceLocation, BoatModel>> texturesAndModels;

    public BoatEntityRenderer(EntityRendererProvider.Context context, boolean bl, String string) {
        super(context);
        this.shadowRadius = 0.8f;
        this.texturesAndModels = BoatType.stream().collect(ImmutableMap.toImmutableMap(boatType -> boatType, boatType -> this.createModel(string, bl, context, boatType)));
    }

    private BoatModel createModel(EntityRendererProvider.Context context, boolean bl) {
        ModelLayerLocation modelLayerLocation = bl ? ModelLayers.createChestBoatModelName((Boat.Type)Boat.Type.OAK) : ModelLayers.createBoatModelName((Boat.Type)Boat.Type.OAK);
        return new BoatModel(context.bakeLayer(modelLayerLocation));
    }

    public void render(AbstractBoatEntity abstractBoatEntity, float f, float f2, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        poseStack.pushPose();
        poseStack.translate(0.0, 0.375, 0.0);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - f));
        float f3 = (float)abstractBoatEntity.getHurtTime() - f2;
        float f4 = abstractBoatEntity.getDamage() - f2;
        if (f4 < 0.0f) {
            f4 = 0.0f;
        }
        if (f3 > 0.0f) {
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin((float)f3) * f3 * f4 / 10.0f * (float)abstractBoatEntity.getHurtDir()));
        }
        if (!Mth.equal((float)abstractBoatEntity.getBubbleAngle(f2), (float)0.0f)) {
            poseStack.mulPose(new Quaternionf().rotateAxis(abstractBoatEntity.getBubbleAngle(f2), 1.0f, 0.0f, 1.0f));
        }
        Pair<ResourceLocation, BoatModel> pair = this.texturesAndModels.get(abstractBoatEntity.getOverrideBoatType());
        ResourceLocation resourceLocation = (ResourceLocation)pair.getFirst();
        BoatModel boatModel = (BoatModel)pair.getSecond();
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
        boatModel.setupAnim((Boat)abstractBoatEntity, f2, 0.0f, -0.1f, 0.0f, 0.0f);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(boatModel.renderType(resourceLocation));
        boatModel.renderToBuffer(poseStack, vertexConsumer, n, OverlayTexture.NO_OVERLAY, -1);
        if (!abstractBoatEntity.isUnderWater()) {
            VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RenderType.waterMask());
            boatModel.waterPatch().render(poseStack, vertexConsumer2, n, OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
        super.render(abstractBoatEntity, f, f2, poseStack, multiBufferSource, n);
    }

    protected String getTexture(BoatType boatType, boolean bl) {
        if (bl) {
            return "textures/entity/chest_boat/" + boatType.getName() + ".png";
        }
        return "textures/entity/boat/" + boatType.getName() + ".png";
    }

    public ResourceLocation getTextureLocation(AbstractBoatEntity abstractBoatEntity) {
        return (ResourceLocation)this.texturesAndModels.get(abstractBoatEntity.getOverrideBoatType()).getFirst();
    }

    private Pair createModel(String string, boolean bl, EntityRendererProvider.Context context, BoatType boatType) {
        return Pair.of((Object)ResourceLocation.fromNamespaceAndPath(string, this.getTexture(boatType, bl)), (Object)this.createModel(context, bl));
    }

}

