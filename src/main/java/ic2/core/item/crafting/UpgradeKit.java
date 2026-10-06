/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.core.Direction
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.Level
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.DistOnly
 */
package ic2.core.item.crafting;
import net.neoforged.api.distmarker.OnlyIn;

import ic2.core.IC2;
import ic2.core.block.wiring.tileentity.TileEntityChargepadMFE;
import ic2.core.block.wiring.tileentity.TileEntityChargepadMFSU;
import ic2.core.block.wiring.tileentity.TileEntityElectricBlock;
import ic2.core.block.wiring.tileentity.TileEntityElectricMFE;
import ic2.core.block.wiring.tileentity.TileEntityElectricMFSU;
import ic2.core.init.Localization;
import ic2.core.item.ItemMulti;
import ic2.core.item.type.UpdateKitType;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;

public class UpgradeKit
extends ItemMulti<UpdateKitType> {
    public UpgradeKit() {
        super(ItemName.upgrade_kit, UpdateKitType.class);
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
        if (!IC2.platform.isSimulating()) {
            return InteractionResult.PASS;
        }
        UpdateKitType type = (UpdateKitType)this.getType(StackUtil.get(player, hand));
        if (type == null) {
            return InteractionResult.PASS;
        }
        boolean ret = false;
        switch (type) {
            case mfsu: {
                ret = UpgradeKit.upgradeToMfsu(world, pos);
            }
        }
        if (!ret) {
            return InteractionResult.PASS;
        }
        StackUtil.consumeOrError(player, hand, 1);
        return InteractionResult.SUCCESS;
    }

    private static boolean upgradeToMfsu(Level world, BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        if (!(te instanceof TileEntityElectricBlock)) {
            return false;
        }
        TileEntityElectricBlock replacement = null;
        if (te instanceof TileEntityElectricMFE) {
            replacement = new TileEntityElectricMFSU(pos, world.getBlockState(pos));
        } else if (te instanceof TileEntityChargepadMFE) {
            replacement = new TileEntityChargepadMFSU(pos, world.getBlockState(pos));
        }
        if (replacement != null) {
            CompoundTag nbt = te.saveWithoutMetadata(world.registryAccess());
            replacement.loadWithComponents(nbt, world.registryAccess());
            world.setBlockEntity(replacement);
            replacement.onUpgraded();
            replacement.setChanged();
            return true;
        }
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        UpdateKitType type = (UpdateKitType)this.getType(stack);
        if (type == null) {
            return;
        }
        switch (type) {
            case mfsu: {
                tooltip.add(Component.literal(Localization.translate("ic2.upgrade_kit.mfsu.info")));
            }
        }
    }
}

