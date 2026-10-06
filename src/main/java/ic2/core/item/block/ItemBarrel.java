/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.ItemMeshDefinition
 *  net.minecraft.client.renderer.block.model.ModelBakery
 *  net.minecraft.client.renderer.block.model.ModelResourceLocation
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.core.Direction
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.client.model.ModelLoader
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.DistOnly
 */
package ic2.core.item.block;
import net.neoforged.api.distmarker.OnlyIn;

import ic2.core.init.Localization;
import ic2.core.item.ItemBooze;
import ic2.core.item.ItemIC2;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;


import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import net.neoforged.api.distmarker.Dist;

public class ItemBarrel
extends ItemIC2 {
    public ItemBarrel() {
        super(ItemName.barrel);
        this.setMaxStackSize(1);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerModels(final ItemName name) {
        
        
    }

    @Override
    public String getItemStackDisplayName(ItemStack itemstack) {
        int v = ItemBooze.getAmountOfValue(itemstack.getDamageValue());
        if (v > 0) {
            return "" + v + Localization.translate("ic2.item.LBoozeBarrel");
        }
        return Localization.translate("ic2.item.EmptyBoozeBarrel");
    }

    @Override
    public InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
        // 1.19.2 无木桶方块（ex112 TeBlock 体系已移除）：保留物品（酒桶数据），放置功能暂缺
        return InteractionResult.PASS;
    }
}

