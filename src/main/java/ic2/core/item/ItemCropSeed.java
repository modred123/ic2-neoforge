/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.core.NonNullList
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.CreativeModeTab
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package ic2.core.item;

import ic2.api.crops.CropCard;
import ic2.api.crops.Crops;
import ic2.api.crops.ICropSeed;
import ic2.core.crop.TileEntityCrop;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;
import java.util.List;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemCropSeed
extends Item
implements ICropSeed {
    public ItemCropSeed(Item.Properties properties) {
        super(properties);
    }

    public String getDescriptionId(ItemStack itemStack) {
        if (itemStack == null) {
            return "ic2.crop.unknown";
        }
        CropCard cropCard = Crops.instance.getCropCard(itemStack);
        int n = this.getScannedFromStack(itemStack);
        if (n == 0) {
            return "ic2.crop.unknown";
        }
        if (n < 0 || cropCard == null) {
            return "ic2.crop.invalid";
        }
        return cropCard.getUnlocalizedName();
    }

    public Component getName(ItemStack itemStack) {
        CropCard cropCard = Crops.instance.getCropCard(itemStack);
        return Component.translatable((String)(cropCard == null ? "ic2.crop.seeds" : cropCard.getSeedType()), (Object[])new Object[]{super.getName(itemStack)});
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        if (this.getScannedFromStack(itemStack) >= 4) {
            list.add((Component)Component.literal((String)("\u00a72Gr\u00a77 " + this.getGrowthFromStack(itemStack))));
            list.add((Component)Component.literal((String)("\u00a76Ga\u00a77 " + this.getGainFromStack(itemStack))));
            list.add((Component)Component.literal((String)("\u00a73Re\u00a77 " + this.getResistanceFromStack(itemStack))));
        }
    }

    public InteractionResult useOn(UseOnContext useOnContext) {
        ItemStack itemStack;
        TileEntityCrop tileEntityCrop;
        BlockEntity blockEntity = useOnContext.getLevel().getBlockEntity(useOnContext.getClickedPos());
        if (blockEntity instanceof TileEntityCrop && (tileEntityCrop = (TileEntityCrop)blockEntity).tryPlantIn(Crops.instance.getCropCard(itemStack = useOnContext.getItemInHand()), 0, this.getGrowthFromStack(itemStack), this.getGainFromStack(itemStack), this.getResistanceFromStack(itemStack), this.getScannedFromStack(itemStack))) {
            Player player = useOnContext.getPlayer();
            if (!player.getAbilities().instabuild) {
                player.getInventory().items.set(player.getInventory().selected, StackUtil.emptyStack);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    // TODO: 1.21.1 CreativeModeTab 重构
    public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> nonNullList) {
        for (CropCard cropCard : Crops.instance.getCrops()) {
            nonNullList.add(ItemCropSeed.generateItemStackFromValues(cropCard, 1, 1, 1, 4));
        }
    }

    public static ItemStack generateItemStackFromValues(CropCard cropCard, int n, int n2, int n3, int n4) {
        ItemStack itemStack = new ItemStack(Ic2Items.CROP_SEED_BACK);
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("owner", cropCard.getOwner());
        compoundTag.putString("id", cropCard.getId());
        compoundTag.putByte("growth", (byte)n);
        compoundTag.putByte("gain", (byte)n2);
        compoundTag.putByte("resistance", (byte)n3);
        compoundTag.putByte("scan", (byte)n4);
        StackUtil.setTag(itemStack, compoundTag);
        return itemStack;
    }

    @Override
    public CropCard getCropFromStack(ItemStack itemStack) {
        CompoundTag compoundTag = StackUtil.getTag(itemStack);
        if (compoundTag == null || !compoundTag.contains("owner", 8) || !compoundTag.contains("id", 8)) {
            return null;
        }
        String string = compoundTag.getString("owner");
        String string2 = compoundTag.getString("id");
        return Crops.instance.getCropCard(string, string2);
    }

    @Override
    public void setCropFromStack(ItemStack itemStack, CropCard cropCard) {
        // 1.21.1 修复（第二十七轮）：写入必须落到【活引用】上。
        // 原实现用 StackUtil.getTag() —— 它内部走 CustomData.copyTag()，返回的是 CompoundTag.copy()
        // 的深拷贝副本，putXxx 后直接丢弃 ⇒ 6 个 setter 全部静默失效。
        // 1.12.2 对照（ic2_src_112/ic2/core/item/ItemCropSeed.java:145-151）用的是
        // `is.getTagCompound().setString(...)`，getTagCompound() 返回内部引用，写入即生效。
        // 守卫语义保持与 1.12.2 一致：无 NBT 则不设置（不主动创建）。
        if (StackUtil.getTag(itemStack) == null) {
            return;
        }
        CompoundTag compoundTag = StackUtil.getOrCreateNbtData(itemStack);
        compoundTag.putString("owner", cropCard.getOwner());
        compoundTag.putString("id", cropCard.getId());
    }

    @Override
    public int getGrowthFromStack(ItemStack itemStack) {
        CompoundTag compoundTag = StackUtil.getTag(itemStack);
        if (compoundTag == null) {
            return -1;
        }
        return compoundTag.getByte("growth");
    }

    @Override
    public void setGrowthFromStack(ItemStack itemStack, int n) {
        // 1.21.1 修复（第二十七轮）：见 setCropFromStack —— 必须写入活引用，getTag 只返回副本。
        if (StackUtil.getTag(itemStack) == null) {
            return;
        }
        StackUtil.getOrCreateNbtData(itemStack).putByte("growth", (byte)n);
    }

    @Override
    public int getGainFromStack(ItemStack itemStack) {
        CompoundTag compoundTag = StackUtil.getTag(itemStack);
        if (compoundTag == null) {
            return -1;
        }
        return compoundTag.getByte("gain");
    }

    @Override
    public void setGainFromStack(ItemStack itemStack, int n) {
        // 1.21.1 修复（第二十七轮）：见 setCropFromStack —— 必须写入活引用，getTag 只返回副本。
        if (StackUtil.getTag(itemStack) == null) {
            return;
        }
        StackUtil.getOrCreateNbtData(itemStack).putByte("gain", (byte)n);
    }

    @Override
    public int getResistanceFromStack(ItemStack itemStack) {
        CompoundTag compoundTag = StackUtil.getTag(itemStack);
        if (compoundTag == null) {
            return -1;
        }
        return compoundTag.getByte("resistance");
    }

    @Override
    public void setResistanceFromStack(ItemStack itemStack, int n) {
        // 1.21.1 修复（第二十七轮）：见 setCropFromStack —— 必须写入活引用，getTag 只返回副本。
        if (StackUtil.getTag(itemStack) == null) {
            return;
        }
        StackUtil.getOrCreateNbtData(itemStack).putByte("resistance", (byte)n);
    }

    @Override
    public int getScannedFromStack(ItemStack itemStack) {
        CompoundTag compoundTag = StackUtil.getTag(itemStack);
        if (compoundTag == null) {
            return -1;
        }
        return compoundTag.getByte("scan");
    }

    @Override
    public void setScannedFromStack(ItemStack itemStack, int n) {
        // 1.21.1 修复（第二十七轮）：见 setCropFromStack —— 必须写入活引用，getTag 只返回副本。
        if (StackUtil.getTag(itemStack) == null) {
            return;
        }
        StackUtil.getOrCreateNbtData(itemStack).putByte("scan", (byte)n);
    }

    @Override
    public void incrementScannedFromStack(ItemStack itemStack) {
        // 1.21.1 修复（第二十七轮）：见 setCropFromStack —— 必须写入活引用，getTag 只返回副本。
        if (StackUtil.getTag(itemStack) == null) {
            return;
        }
        StackUtil.getOrCreateNbtData(itemStack).putByte("scan", (byte)(this.getScannedFromStack(itemStack) + 1));
    }
}

