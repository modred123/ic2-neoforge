/*
 * 第四十轮新增：方块物品的「能量等级 / 输出 / 最大电量」tooltip 桥接。
 *
 * 背景（用户实测需求）：把"各电器及物品能承受的最大能量等级"写进物品标签，以便接线前判断会不会过压爆炸。
 *
 * 1.12.2 的实现：方块物品是 `ItemBlockTe`，它的 `addInformation` 会把 tooltip 转发给该方块的
 * **dummy TE**（`TeBlock.getDummyTe()`），于是 `Ic2TileEntity.addInformation` 的
 * `ic2.item.tooltip.PowerTier`（能量等级）、`TileEntityElectricBlock.addInformation` 的
 * Output/Capacity/Store、`TileEntityTransformer` 的低压/高压行都会显示。
 *
 * 迁移版：1.21 的方块物品是普通 `BlockItem`，**没有任何人调用 TE 的 addInformation** ——
 * 全工程 7 个实现了 addInformation 的 TE（Ic2TileEntity / TileEntityElectricBlock / TileEntityTransformer /
 * TileEntityTank / TileEntityStorageBox / TileEntityAssemblyBench / TileEntityMassFabricator）的 tooltip
 * 因此全部失效。本类在客户端 tooltip 事件里补上这座桥。
 *
 * 实现要点：
 *  - `Ic2TileEntityBlock.createBlockEntity(BlockPos, BlockState)` 是**纯反射构造**（不访问 Level），
 *    因此可以在客户端为每个方块缓存一个"哑 TE"实例（等价 1.12.2 的 dummy TE）；失败则用哨兵记住，不重试。
 *  - 传给 addInformation 的是 `itemStack.copy()`：`TileEntityElectricBlock.addInformation` 会调用
 *    `StackUtil.getOrCreateNbtData`（会写组件），绝不能作用在真实物品栈上。
 *  - 全程 try/catch：任何一个方块的 tooltip 出错都不应影响游戏。
 */
package ic2.core.item;

import ic2.core.IC2;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.util.LogCategory;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public final class EnergyTierTooltipHandler {
    /** 构造失败过的方块（避免每次 tooltip 都重试并刷日志）。 */
    private static final Object FAILED = new Object();
    /** 方块 → 哑 TE（或 {@link #FAILED}）。只在客户端 tooltip 线程访问。 */
    private static final Map<Block, Object> DUMMY_TE = new IdentityHashMap<Block, Object>();

    private EnergyTierTooltipHandler() {
    }

    public static void addTooltip(ItemStack itemStack, List<Component> list) {
        if (itemStack == null || itemStack.isEmpty()) {
            return;
        }
        if (!(itemStack.getItem() instanceof BlockItem)) {
            return;
        }
        Block block = ((BlockItem)itemStack.getItem()).getBlock();
        if (!(block instanceof Ic2TileEntityBlock)) {
            return;
        }
        Ic2TileEntity tileEntity = EnergyTierTooltipHandler.dummy(block);
        if (tileEntity == null) {
            return;
        }
        ArrayList<String> arrayList = new ArrayList<String>(4);
        try {
            tileEntity.addInformation(itemStack.copy(), arrayList, TooltipFlag.Default.NORMAL);
        }
        catch (Throwable throwable) {
            IC2.log.warn(LogCategory.General, "Block tooltip failed for %s: %s", block, throwable.toString());
            return;
        }
        for (String string : arrayList) {
            if (string == null || string.isEmpty()) continue;
            list.add(Component.literal(string));
        }
    }

    private static Ic2TileEntity dummy(Block block) {
        Ic2TileEntity tileEntity;
        Object object = DUMMY_TE.get(block);
        if (object == FAILED) {
            return null;
        }
        if (object != null) {
            return (Ic2TileEntity)object;
        }
        Ic2TileEntity ic2TileEntity = null;
        try {
            ic2TileEntity = ((Ic2TileEntityBlock)block).createBlockEntity(BlockPos.ZERO, block.defaultBlockState());
        }
        catch (Throwable throwable) {
            IC2.log.warn(LogCategory.General, "Cannot build dummy tile entity for %s: %s", block, throwable.toString());
        }
        DUMMY_TE.put(block, ic2TileEntity == null ? FAILED : ic2TileEntity);
        return ic2TileEntity;
    }
}
