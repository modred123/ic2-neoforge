/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.client.renderer.block.model.BakedQuad
 *  net.minecraft.client.renderer.block.model.ItemOverrides
 *  net.minecraft.client.renderer.block.model.ItemTransforms
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.client.resources.model.Material
 *  net.minecraft.client.resources.model.ModelBakery
 *  net.minecraft.client.resources.model.ModelState
 *  net.minecraft.client.resources.model.UnbakedModel
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.core.Registry
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  org.jetbrains.annotations.Nullable
 */
package ic2.core.block;

import com.mojang.datafixers.util.Pair;
import ic2.core.IC2;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.util.LogCategory;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class DynamicBeModel<T>
implements UnbakedModel,
BakedModel {
    protected final Ic2TileEntityBlock block;
    private final ResourceLocation backingModelId;
    private BakedModel baseModel;
    private BakedModel activeBaseModel;
    private final T[] cache;
    private final StampedLock cacheLock = new StampedLock();

    protected DynamicBeModel(ResourceLocation resourceLocation) {
        ResourceLocation resourceLocation2 = ResourceLocation.fromNamespaceAndPath(resourceLocation.getNamespace(), resourceLocation.getPath().substring(resourceLocation.getPath().lastIndexOf(47) + 1));
        Block block = (Block)BuiltInRegistries.BLOCK.get(resourceLocation2);
        if (!(block instanceof Ic2TileEntityBlock)) {
            throw new IllegalArgumentException("invalid id: " + resourceLocation);
        }
        this.block = (Ic2TileEntityBlock)block;
        this.backingModelId = ResourceLocation.fromNamespaceAndPath(resourceLocation.getNamespace(), resourceLocation.getPath().replace("block/be/", "block/"));
        this.cache = (T[])new Object[(this.block.facingProperty != null ? 6 : 1) * (this.block.canActive() ? 2 : 1)];
    }

    public Collection<ResourceLocation> getDependencies() {
        if (this.block.canActive()) {
            return Arrays.asList(this.backingModelId, this.getActiveModelId());
        }
        return Collections.singletonList(this.backingModelId);
    }

    private ResourceLocation getActiveModelId() {
        return ResourceLocation.fromNamespaceAndPath(this.backingModelId.getNamespace(), this.backingModelId.getPath().concat("_active"));
    }


    public BakedModel bake(ModelBaker modelBaker, Function<Material, TextureAtlasSprite> function, ModelState modelState) {
        this.baseModel = modelBaker.bake(this.backingModelId, modelState);
        if (this.baseModel == null) {
            throw new IllegalStateException("missing model " + this.backingModelId);
        }
        if (this.block.canActive()) {
            this.activeBaseModel = modelBaker.bake(this.getActiveModelId(), modelState);
            if (this.activeBaseModel == null) {
                throw new IllegalStateException("missing model " + this.getActiveModelId());
            }
        }
        return this;
    }

    public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, RandomSource randomSource) {
        return Collections.emptyList();
    }

    public boolean useAmbientOcclusion() {
        return true;
    }

    public boolean isGui3d() {
        return true;
    }

    public boolean usesBlockLight() {
        return true;
    }

    public boolean isCustomRenderer() {
        return false;
    }

    public TextureAtlasSprite getParticleIcon() {
        return this.baseModel.getParticleIcon();
    }

    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected T getMesh(BlockState blockState, boolean bl) {
        int n;
        Direction direction;
        if (this.block.facingProperty != null) {
            direction = (Direction)blockState.getValue(this.block.facingProperty);
            n = direction.ordinal();
        } else {
            direction = Direction.NORTH;
            n = 0;
        }
        if (bl) {
            n += this.cache.length >>> 1;
        }
        long l = this.cacheLock.readLock();
        try {
            T t = this.cache[n];
            if (t != null) {
                T t2 = t;
                return t2;
            }
        }
        finally {
            this.cacheLock.unlock(l);
        }
        T t = this.generateMesh(bl ? this.activeBaseModel : this.baseModel, switch (direction) {
            case Direction.DOWN -> 1;
            case Direction.UP -> 3;
            case Direction.NORTH -> 0;
            case Direction.SOUTH -> 2;
            case Direction.WEST -> 3;
            case Direction.EAST -> 1;
            default -> throw new IllegalStateException();
        }, direction.getAxis() == Direction.Axis.Y);
        l = this.cacheLock.readLock();
        try {
            T t3 = this.cache[n];
            if (t3 != null) {
                T t4 = t3;
                return t4;
            }
            long l2 = this.cacheLock.tryConvertToWriteLock(l);
            if (l2 != 0L) {
                l = l2;
            } else {
                this.cacheLock.unlock(l);
                l = this.cacheLock.writeLock();
            }
            t3 = this.cache[n];
            if (t3 != null) {
                T t5 = t3;
                return t5;
            }
            this.cache[n] = t;
            T t6 = t;
            return t6;
        }
        finally {
            this.cacheLock.unlock(l);
        }
    }

    protected abstract T generateMesh(BakedModel var1, int var2, boolean var3);

    protected static BakedQuad rotateQuad(BakedQuad bakedQuad, int n, boolean bl) {
        int n2;
        int n3;
        if ((n &= 3) == 0) {
            return bakedQuad;
        }
        int[] nArray = bakedQuad.getVertices();
        int[] nArray2 = Arrays.copyOf(nArray, nArray.length);
        int n4 = nArray.length >>> 2;
        if (bl) {
            n3 = 2;
            n2 = 1;
        } else {
            n3 = 0;
            n2 = 2;
        }
        for (int i = 0; i < 4; ++i) {
            float f;
            int n5 = i * n4;
            float f2 = Float.intBitsToFloat(nArray[n5 + n3]);
            float f3 = Float.intBitsToFloat(nArray[n5 + n2]);
            switch (n) {
                case 1: {
                    f = 1.0f - f3;
                    f3 = f2;
                    break;
                }
                case 2: {
                    f = 1.0f - f2;
                    f3 = 1.0f - f3;
                    break;
                }
                case 3: {
                    f = f3;
                    f3 = 1.0f - f2;
                    break;
                }
                default: {
                    throw new IllegalStateException();
                }
            }
            nArray2[n5 + n3] = Float.floatToRawIntBits(f);
            nArray2[n5 + n2] = Float.floatToRawIntBits(f3);
        }
        return new BakedQuad(nArray2, bakedQuad.getTintIndex(), DynamicBeModel.rotateFace(bakedQuad.getDirection(), n, bl), bakedQuad.getSprite(), bakedQuad.isShade());
    }

    protected static Direction rotateFace(Direction direction, int n, boolean bl) {
        block3: {
            block2: {
                n &= 3;
                if (!bl || direction.getAxis() == Direction.Axis.X) break block2;
                for (int i = 0; i < n; ++i) {
                    direction = direction.getClockWise(Direction.Axis.X);
                }
                break block3;
            }
            if (bl || direction.getAxis() == Direction.Axis.Y) break block3;
            for (int i = 0; i < n; ++i) {
                direction = direction.getClockWise();
            }
        }
        return direction;
    }
}

