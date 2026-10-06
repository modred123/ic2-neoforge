/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.client.renderer.block.model.BakedModel
 *  net.minecraft.client.renderer.block.model.ItemOverrideList
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.core.Direction
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.Level
 */
package ic2.core.item.tool;

import ic2.core.item.tool.ItemObscurator;
import ic2.core.model.MaskOverlayModel;
import ic2.core.model.ModelUtil;
import ic2.core.util.StackUtil;
import java.util.Collections;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class RenderObscurator
extends MaskOverlayModel {
    private static final ResourceLocation baseModelLoc = ResourceLocation.fromNamespaceAndPath("ic2", "item/tool/electric/obscurator_raw");
    private static final ResourceLocation maskTextureLoc = ResourceLocation.fromNamespaceAndPath("ic2", "textures/items/tool/electric/obscurator_mask.png");
    private final ItemOverrides overrideHandler = new ItemOverrides(){

        @Override
        public BakedModel resolve(BakedModel originalModel, ItemStack stack, ClientLevel world, LivingEntity entity, int seed) {
            ItemObscurator.ObscuredRenderInfo renderInfo;
            if (StackUtil.isEmpty(stack)) {
                return ModelUtil.getMissingModel();
            }
            CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
            BlockState state = ItemObscurator.getState(nbt);
            Direction side = ItemObscurator.getSide(nbt);
            int[] colorMultipliers = ItemObscurator.getColorMultipliers(nbt);
            if (state == null || side == null || (renderInfo = ItemObscurator.getRenderInfo(state, side)) == null || colorMultipliers == null || colorMultipliers.length * 4 != renderInfo.uvs.length) {
                return RenderObscurator.this.get();
            }
            return RenderObscurator.this.get(renderInfo.uvs, colorMultipliers);
        }
    };

    public RenderObscurator() {
        super(baseModelLoc, maskTextureLoc, true, 0.001f);
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.overrideHandler;
    }
}

