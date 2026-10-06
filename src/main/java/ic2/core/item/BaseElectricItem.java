/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.NonNullList
 *  net.minecraft.util.Mth
 *  net.minecraft.world.item.CreativeModeTab
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 */
package ic2.core.item;

import ic2.core.init.MainConfig;
import ic2.core.ref.ItemName;
import ic2.core.util.ConfigUtil;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IItemHudInfo;
import ic2.core.item.ElectricItemManager;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public abstract class BaseElectricItem
extends Item
implements IElectricItem,
IItemHudInfo {
    public static final boolean logIncorrectItemDamaging = ConfigUtil.getBool(MainConfig.get(), "debug/logIncorrectItemDamaging");
    protected final double maxCharge;
    protected final double transferLimit;
    protected final int tier;

    public BaseElectricItem(ItemName name, double d, double d2, int n) {
        this(new Item.Properties(), d, d2, n);
        ic2.core.init.BlocksItems.registerItem(this, ic2.core.IC2.getIdentifier(name.name()));
        name.setInstance(this);
    }

    public BaseElectricItem(Item.Properties properties, double d, double d2, int n) {
        // 1.21.1 修复（第二十四轮）：强制电动物品不可堆叠（maxStackSize = 1）。
        //
        // 1.12.2（权威实现）BaseElectricItem 构造器内调用了 this.setMaxStackSize(1)，
        // 所有电动物品（电池/电芯/电钻/装甲/喷气背包……）一律不可堆叠。
        // 1.19.2 重写时漏掉了这一句，导致：
        //   · RE 电池（Ic2Items 用裸 new Item.Properties()）→ 默认堆叠上限 64
        //   · 高级电池/能量水晶/兰波顿水晶 → 显式 stacksTo(16)
        // 而 ElectricItemManager.charge()/discharge() 至今仍保留原版守卫：
        //   if (d < 0.0 || StackUtil.getSize(itemStack) > 1 || tier > n) return 0.0;
        // 于是「可堆叠」与「拒绝 count > 1」直接冲突，产生两个连锁症状：
        //   1) 一叠电池放不进任何充电槽 —— InvSlotCharge.accepts() 调 charge(...,true,true)
        //      恒返回 0.0，槽位判定为拒绝；
        //   2) 一叠电池不显示电量 tooltip —— ElectricItemTooltipHandler.addTooltip() 的
        //      getMaxCharge(itemStack) > 0.0 守卫同样因 count > 1 得到 0.0 而失败。
        // 且堆叠电池共享同一份 charge 数据，本身在物理上就不成立。
        // 此处恢复 1.12.2 行为：无条件 stacksTo(1)，覆盖 Ic2Items 里的 stacksTo(16)。
        super(properties.stacksTo(1));
        this.maxCharge = d;
        this.transferLimit = d2;
        this.tier = n;
    }

    @Override
    public boolean canProvideEnergy(ItemStack itemStack) {
        return false;
    }

    @Override
    public double getMaxCharge(ItemStack itemStack) {
        return this.maxCharge;
    }

    @Override
    public int getTier(ItemStack itemStack) {
        return this.tier;
    }

    @Override
    public double getTransferLimit(ItemStack itemStack) {
        return this.transferLimit;
    }

    @Override
    public List<String> getHudInfo(ItemStack itemStack, boolean bl) {
        LinkedList<String> linkedList = new LinkedList<String>();
        linkedList.add(ElectricItem.manager.getToolTip(itemStack));
        return linkedList;
    }

    public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> nonNullList) {
        
        ElectricItemManager.addChargeVariants(this, nonNullList);
    }

    /**
     * 第二十八轮修复：1.12.2 的 BaseElectricItem 是 IPseudoDamageItem，电量被映射成"伪耐久"，
     * 原版只在 damage > 0（即未满电）时才画电量条。迁移版写成 charge <= maxCharge —— 恒为 true，
     * 满电也会多画一条满格绿条，与 1.12.2 不符。此处按电量比例恢复"满电不画条"。
     */
    public boolean isBarVisible(ItemStack itemStack) {
        return ElectricItem.manager.getStackChargeLevel(itemStack) < 1.0;
    }

    public int getBarWidth(ItemStack itemStack) {
        return (int)Math.round(ElectricItem.manager.getStackChargeLevel(itemStack) * 13.0);
    }

    public int getBarColor(ItemStack itemStack) {
        return Mth.hsvToRgb((float)((float)(ElectricItem.manager.getStackChargeLevel(itemStack) / 3.0)), (float)1.0f, (float)1.0f);
    }
}

