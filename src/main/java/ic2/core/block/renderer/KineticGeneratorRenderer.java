/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  org.joml.Vector3f
 *  it.unimi.dsi.fastutil.ints.Int2ReferenceMap
 *  it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.model.geom.PartPose
 *  net.minecraft.client.model.geom.builders.CubeListBuilder
 *  net.minecraft.client.model.geom.builders.LayerDefinition
 *  net.minecraft.client.model.geom.builders.MeshDefinition
 *  net.minecraft.client.model.geom.builders.PartDefinition
 *  net.minecraft.client.renderer.LevelRenderer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderer
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.BlockAndTintGetter
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package ic2.core.block.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector3f;
import com.mojang.math.Axis;
import ic2.api.tile.IRotorProvider;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;

public class KineticGeneratorRenderer<T extends BlockEntity>
implements BlockEntityRenderer<T> {
    private static final Int2ReferenceMap<ModelPart> rotorModels = new Int2ReferenceOpenHashMap();

    public KineticGeneratorRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void render(T t, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2) {
        MeshDefinition meshDefinition;
        int n3 = ((IRotorProvider)t).getRotorDiameter();
        if (n3 == 0) {
            return;
        }
        float f2 = ((IRotorProvider)t).getAngle();
        ResourceLocation resourceLocation = ((IRotorProvider)t).getRotorRenderTexture();
        ModelPart modelPart = rotorModels.get(n3);
        if (modelPart == null) {
            meshDefinition = new MeshDefinition();
            PartDefinition partDefinition = meshDefinition.getRoot();
            partDefinition.addOrReplaceChild("1", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, -4.0f, 1.0f, (float)(n3 * 8), 8.0f), PartPose.offsetAndRotation((float)-8.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.5f, (float)0.0f));
            partDefinition.addOrReplaceChild("2", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, -4.0f, 1.0f, (float)(n3 * 8), 8.0f), PartPose.offsetAndRotation((float)-8.0f, (float)0.0f, (float)0.0f, (float)3.1f, (float)0.5f, (float)0.0f));
            partDefinition.addOrReplaceChild("3", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, -4.0f, 1.0f, (float)(n3 * 8), 8.0f), PartPose.offsetAndRotation((float)-8.0f, (float)0.0f, (float)0.0f, (float)4.7f, (float)0.0f, (float)0.5f));
            partDefinition.addOrReplaceChild("4", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, -4.0f, 1.0f, (float)(n3 * 8), 8.0f), PartPose.offsetAndRotation((float)-8.0f, (float)0.0f, (float)0.0f, (float)1.5f, (float)0.0f, (float)-0.5f));
            modelPart = LayerDefinition.create(meshDefinition, 32, 256).bakeRoot();
            rotorModels.put(n3, modelPart);
        }
        Direction direction = ((IRotorProvider)t).getFacing();
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        switch (direction) {
            case NORTH: {
                poseStack.mulPose(Axis.YP.rotationDegrees(-90.0f));
                break;
            }
            case EAST: {
                poseStack.mulPose(Axis.YP.rotationDegrees(-180.0f));
                break;
            }
            case SOUTH: {
                poseStack.mulPose(Axis.YP.rotationDegrees(-270.0f));
                break;
            }
            case UP: {
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0f));
                break;
            }
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(f2));
        poseStack.translate(-0.2, 0.0, 0.0);
        n = LevelRenderer.getLightColor(t.getLevel(), t.getBlockPos().relative(direction));
        modelPart.render(poseStack, multiBufferSource.getBuffer(RenderType.entitySolid((ResourceLocation)resourceLocation)), n, n2);
        poseStack.popPose();
    }
}

