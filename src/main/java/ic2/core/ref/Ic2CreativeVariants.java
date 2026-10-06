/*
 * 第三十三轮新增：创造模式物品栏的"满电版本"条目。
 *
 * 1.12.2 的 IC2 创造栏里，电池类物品与储电类机器**各有两格**：空格 + 满电格。
 *   - 电池：`BaseElectricItem.getSubItems` → `ElectricItemManager.addChargeVariants(item, list)`
 *     （1.12.2 `ic2/core/item/BaseElectricItem.java:75`）。
 *   - 储电箱：`TeBlock.addSubBlocks` 给所有 dummy TE 实现 `IEnergyStorage` 的方块再补一格，
 *     内容为 NBT `energy = capacity`（1.12.2 `ic2/core/ref/TeBlock.java:405`）。
 *
 * 1.21.1 的 `Item.fillItemCategory` 已被移除，创造栏内容改由 `CreativeModeTab.Builder.displayItems`
 * 生成，于是这两类"满电格"整体丢失。本类给出补齐所需的 ItemStack，
 * 由 `ic2.forge.EnvProxyForge#createItemGroup` 在遍历 ic2 物品时追加。
 */
package ic2.core.ref;

import ic2.core.block.wiring.tileentity.TileEntityChargepadBatBox;
import ic2.core.block.wiring.tileentity.TileEntityChargepadCESU;
import ic2.core.block.wiring.tileentity.TileEntityChargepadMFE;
import ic2.core.block.wiring.tileentity.TileEntityChargepadMFSU;
import ic2.core.block.wiring.tileentity.TileEntityElectricBatBox;
import ic2.core.block.wiring.tileentity.TileEntityElectricCESU;
import ic2.core.block.wiring.tileentity.TileEntityElectricMFE;
import ic2.core.block.wiring.tileentity.TileEntityElectricMFSU;
import ic2.core.item.ElectricItemManager;
import ic2.core.util.StackUtil;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class Ic2CreativeVariants {
    /**
     * 四个电池类物品（充电电池 / 高级充电电池 / 能量水晶 / 兰波顿水晶）。
     * 1.12.2 里这四件都走 `BaseElectricItem.getSubItems`，满电版本由
     * `ElectricItemManager.addChargeVariants` 生成。
     */
    private static final Set<Item> FULL_CHARGE_BATTERIES = Set.of(
            Ic2Items.RE_BATTERY,
            Ic2Items.ADVANCED_RE_BATTERY,
            Ic2Items.ENERGY_CRYSTAL,
            Ic2Items.LAPOTRON_CRYSTAL);

    /**
     * 八台"储电类机器"：4 台储电箱 + 4 台充电座。容量取自各自 TileEntity 的 CAPACITY 常量
     * （与实体构造参数同一来源，避免在两处写死后失配）。满电格 = 物品 NBT `energy = capacity`；
     * 放置时由 `TileEntityElectricBlock.onPlaced` 读回（`...TileEntityElectricBlock.java:173-179`）。
     */
    private static final Map<Item, Double> ENERGY_STORAGE_CAPACITIES = Map.of(
            Ic2Items.BATBOX, (double)TileEntityElectricBatBox.CAPACITY,
            Ic2Items.CESU, (double)TileEntityElectricCESU.CAPACITY,
            Ic2Items.MFE, (double)TileEntityElectricMFE.CAPACITY,
            Ic2Items.MFSU, (double)TileEntityElectricMFSU.CAPACITY,
            Ic2Items.BATBOX_CHARGEPAD, (double)TileEntityChargepadBatBox.CAPACITY,
            Ic2Items.CESU_CHARGEPAD, (double)TileEntityChargepadCESU.CAPACITY,
            Ic2Items.MFE_CHARGEPAD, (double)TileEntityChargepadMFE.CAPACITY,
            Ic2Items.MFSU_CHARGEPAD, (double)TileEntityChargepadMFSU.CAPACITY);

    /**
     * 第三十七轮：创造栏**隐藏**的"经典版（classic_*）"方块物品。
     *
     * 1.12.2 里这些并不是独立物品 —— 它们是 `@TeBlock.Delegated(current=…, old=…)` 的**旧 TE 类**
     * （`ic2_src_112/ic2/core/block/wiring/TileEntityElectricMFSU.java:10/21`），只在 IC2 "classic 模式"下
     * 替换同一个方块的 TE；1.12.2 的创造栏由 `TeBlock.addSubBlocks` 遍历 TeBlock 条目生成，**从来不出现它们**。
     *
     * 上游 1.19.2 把它们做成了独立方块/物品，却**没有给独立的名称与图标**：
     *   - `block.ic2.classic_mfsu` = "MFSU储电箱"（与 `block.ic2.mfsu` 逐字相同，en_us 同为 "MFSU"）；
     *   - `block.ic2.classic_mfe`  = "MFE储电箱"（同上）；
     *   - `models/item/classic_mfsu.json` 的 parent 也是 `ic2:block/wiring/storage/mfsu`（图标一模一样）。
     * 于是创造栏里出现两个**名称、图标完全相同**的 "MFSU储电箱"（用户实测报的"物品重复"；他看到的第三个
     * 是本类第三十三轮补的满电格）。
     *
     * 处理：创造栏跳过这些物品（对齐 1.12.2 的创造栏），**保留注册** —— 存档里已放置的 classic_* 方块、
     * `/give ic2:classic_mfsu`、配方与 JEI 的物品索引都不受影响。
     */
    private static final Set<Item> HIDDEN_IN_CREATIVE_TAB = Set.of(
            Ic2Items.CLASSIC_NUKE,
            Ic2Items.CLASSIC_CANNER,
            Ic2Items.CLASSIC_CROPMATRON,
            Ic2Items.CLASSIC_MASS_FABRICATOR,
            Ic2Items.CLASSIC_MFE,
            Ic2Items.CLASSIC_MFSU,
            Ic2Items.CLASSIC_ELECTROLYZER);

    /** 该物品是否应从创造模式物品栏中隐藏（原因见 {@link #HIDDEN_IN_CREATIVE_TAB}）。 */
    public static boolean isHiddenInCreativeTab(Item item) {
        return HIDDEN_IN_CREATIVE_TAB.contains(item);
    }

    private Ic2CreativeVariants() {
    }

    /**
     * @return 该物品的"满电版本" ItemStack；没有对应变体时返回 {@link ItemStack#EMPTY}。
     *         每次调用都返回新对象，可安全交给创造栏输出。
     */
    public static ItemStack getFullVariant(Item item) {
        if (FULL_CHARGE_BATTERIES.contains(item)) {
            return ElectricItemManager.getCharged(item, Double.POSITIVE_INFINITY);
        }
        Double capacity = ENERGY_STORAGE_CAPACITIES.get(item);
        if (capacity == null) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack = new ItemStack(item);
        StackUtil.getOrCreateNbtData(itemStack).putDouble("energy", capacity);
        return itemStack;
    }
}
