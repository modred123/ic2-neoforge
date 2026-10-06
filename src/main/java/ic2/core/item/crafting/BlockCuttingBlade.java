/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.DistOnly
 */
package ic2.core.item.crafting;
import net.neoforged.api.distmarker.OnlyIn;

import ic2.api.item.IBlockCuttingBlade;
import ic2.core.init.Localization;
import ic2.core.item.ItemMulti;
import ic2.core.item.type.BlockCuttingBladeType;
import ic2.core.ref.ItemName;
import java.util.List;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;

public class BlockCuttingBlade
extends ItemMulti<BlockCuttingBladeType>
implements IBlockCuttingBlade {
    public BlockCuttingBlade() {
        super(ItemName.block_cutting_blade, BlockCuttingBladeType.class);
    }

    @Override
    public int getHardness(ItemStack stack) {
        BlockCuttingBladeType blade = (BlockCuttingBladeType)this.getType(stack);
        if (blade == null) {
            return 0;
        }
        switch (blade) {
            case iron: {
                return 3;
            }
            case steel: {
                return 6;
            }
            case diamond: {
                return 9;
            }
        }
        return 0;
    }

    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, Level world, List<String> tooltip, net.minecraft.world.item.TooltipFlag advanced) {
        BlockCuttingBladeType blade = (BlockCuttingBladeType)this.getType(stack);
        if (blade == null) {
            return;
        }
        switch (blade) {
            case iron: {
                tooltip.add(Localization.translate("ic2.IronBlockCuttingBlade.info"));
                break;
            }
            case steel: {
                tooltip.add(Localization.translate("ic2.AdvIronBlockCuttingBlade.info"));
                break;
            }
            case diamond: {
                tooltip.add(Localization.translate("ic2.DiamondBlockCuttingBlade.info"));
            }
        }
        tooltip.add(Localization.translate("ic2.CuttingBlade.hardness", this.getHardness(stack)));
    }
}

