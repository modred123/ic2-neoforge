/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 */
package ic2.core.block.wiring.tileentity;

import ic2.core.block.wiring.tileentity.TileEntityChargepadBlock;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityChargepadMFSU
extends TileEntityChargepadBlock {
    /** 储电上限（EU）。创造栏"满电版本"（Ic2CreativeVariants）写 NBT "energy" 时复用此常量。 */
    public static final int CAPACITY = 40000000;

    public TileEntityChargepadMFSU(BlockPos blockPos, BlockState blockState) {
        super((BlockEntityType<? extends TileEntityChargepadBlock>)Ic2BlockEntities.MFSU_CHARGEPAD, blockPos, blockState, 4, 2048, CAPACITY);
    }

    @Override
    protected void getItems(Player player) {
        for (ItemStack itemStack : player.getInventory().armor) {
            if (itemStack == null) continue;
            this.chargeItem(itemStack, 2048);
        }
        for (ItemStack itemStack : player.getInventory().items) {
            if (itemStack == null) continue;
            this.chargeItem(itemStack, 2048);
        }
    }
}

