/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.block.BlockModelShaper
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.client.resources.model.ModelManager
 *  net.minecraft.client.resources.model.ModelResourceLocation
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.block.state.BlockState
 */
package ic2.core.model;

import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class ModelUtil {
    public static ModelResourceLocation getModelLocation(ResourceLocation resourceLocation, BlockState blockState) {
        return new ModelResourceLocation(resourceLocation, ModelUtil.getVariant(blockState));
    }

    public static String getVariant(BlockState blockState) {
        return BlockModelShaper.statePropertiesToString((Map)blockState.getValues());
    }

    public static BakedModel getMissingModel() {
        return ModelUtil.getModelManager().getMissingModel();
    }

    public static BakedModel getModel(ModelResourceLocation modelResourceLocation) {
        return ModelUtil.getModelManager().getModel(modelResourceLocation);
    }

    public static BakedModel getBlockModel(BlockState blockState) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(blockState);
    }

    private static ModelManager getModelManager() {
        return Minecraft.getInstance().getItemRenderer().getItemModelShaper().getModelManager();
    }
}

