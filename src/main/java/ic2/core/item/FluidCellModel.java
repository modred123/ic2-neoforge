/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.block.model.BakedModel
 *  net.minecraft.client.renderer.block.model.ItemOverrideList
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.fluids.FluidStack
 *  net.minecraftforge.fluids.FluidUtil
 */
package ic2.core.item;

import ic2.core.fluid.FluidHandler;
import ic2.core.model.MaskOverlayModel;
import ic2.core.model.ModelUtil;
import ic2.core.util.StackUtil;
import java.util.Collections;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

public class FluidCellModel
extends MaskOverlayModel {
    private static final ResourceLocation baseModelLoc = ResourceLocation.fromNamespaceAndPath("ic2", "item/cell/fluid_cell_case");
    private static final ResourceLocation maskTextureLoc = ResourceLocation.fromNamespaceAndPath("ic2", "textures/items/cell/fluid_cell_window.png");
    private final ItemOverrides overrideHandler = new ItemOverrides(){

        @Override
        public BakedModel resolve(BakedModel originalModel, ItemStack stack, ClientLevel world, LivingEntity entity, int seed) {
            ResourceLocation spriteLoc;
            if (StackUtil.isEmpty(stack)) {
                return ModelUtil.getMissingModel();
            }
            FluidStack fs = FluidUtil.getFluidContained(stack).orElse(null);
            if (fs == null || (spriteLoc = FluidHandler.getStillSpriteId(fs.getFluid())) == null) {
                return FluidCellModel.this.get();
            }
            return FluidCellModel.this.get(((TextureAtlas)Minecraft.getInstance().getTextureManager().getTexture(InventoryMenu.BLOCK_ATLAS)).getSprite(spriteLoc), FluidHandler.getColor(fs.getFluid()));
        }
    };

    public FluidCellModel() {
        super(baseModelLoc, maskTextureLoc, false, -0.1f);
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.overrideHandler;
    }
}

