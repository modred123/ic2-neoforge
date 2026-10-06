/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.block.model.ItemOverrides
 *  net.minecraft.client.renderer.block.model.ItemTransforms
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.client.resources.model.ModelResourceLocation
 *  net.minecraft.core.Direction
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.block.state.BlockState
 */
package ic2.core.model;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.21.1 版 MaskOverlayModel（图标遮罩覆盖模型基类）。
 * 说明：ex112 原实现依赖旧 1.12.2 模型框架（AbstractModel/BasicBakedItemModel/MergedItemModel/VdUtil），
 * 该框架尚未迁移到 1.21.1。此处提供自包含的最小实现：所有 BakedModel 行为委托给基础模型
 * （ModelUtil 从模型管理器按 baseModelLocation 加载），遮罩重贴图逻辑留待模型框架迁移时补齐。
 */
public abstract class MaskOverlayModel
implements BakedModel {
    private final ResourceLocation baseModelLocation;
    private final ResourceLocation maskTextureLocation;
    private final boolean scaleOverlay;
    private final float offset;
    private BakedModel baseModel;

    protected MaskOverlayModel(ResourceLocation baseModelLocation, ResourceLocation maskTextureLocation, boolean scaleOverlay, float offset) {
        this.baseModelLocation = baseModelLocation;
        this.maskTextureLocation = maskTextureLocation;
        this.scaleOverlay = scaleOverlay;
        this.offset = offset;
    }

    protected BakedModel getBaseModel() {
        if (this.baseModel == null) {
            this.baseModel = ModelUtil.getModel(new ModelResourceLocation(this.baseModelLocation, "inventory"));
        }
        return this.baseModel;
    }

    protected BakedModel get() {
        return this.getBaseModel();
    }

    protected BakedModel get(TextureAtlasSprite overlay, int colorMultiplier) {
        return this.getBaseModel();
    }

    protected BakedModel get(float[] uvs, int[] colorMultipliers) {
        if (uvs == null) {
            throw new NullPointerException();
        }
        if (uvs.length == 0) {
            return this.get();
        }
        return this.getBaseModel();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, RandomSource randomSource) {
        return this.getBaseModel().getQuads(blockState, direction, randomSource);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.getBaseModel().useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return this.getBaseModel().isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return this.getBaseModel().usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return this.getBaseModel().isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.getBaseModel().getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return this.getBaseModel().getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
