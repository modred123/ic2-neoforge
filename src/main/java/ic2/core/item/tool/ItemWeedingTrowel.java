/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.core.Direction
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.Level
 */
package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.crop.Ic2Crops;
import ic2.core.crop.TileEntityCrop;
import ic2.core.item.ItemIC2;
import ic2.core.item.type.CropResItemType;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

@NotClassic
public class ItemWeedingTrowel
extends ItemIC2 {
    public ItemWeedingTrowel() {
        super(ItemName.weeding_trowel);
        this.setMaxStackSize(1);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext useOnContext) {
        Player player = useOnContext.getPlayer();
        Level world = useOnContext.getLevel();
        BlockPos pos = useOnContext.getClickedPos();
        Direction side = useOnContext.getClickedFace();
        InteractionHand hand = useOnContext.getHand();
        if (player == null) {
            return InteractionResult.PASS;
        }
        TileEntityCrop tileEntityCrop;
        if (!IC2.platform.isSimulating()) {
            return InteractionResult.PASS;
        }
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof TileEntityCrop && (tileEntityCrop = (TileEntityCrop)tileEntity).getCrop() == Ic2Crops.weed) {
            StackUtil.dropAsEntity(world, pos, StackUtil.copyWithSize(ItemName.crop_res.getItemStack(CropResItemType.weed), tileEntityCrop.getCurrentAge()));
            tileEntityCrop.reset();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}

