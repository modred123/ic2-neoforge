/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.PathNavigationRegion
 *  net.minecraft.world.level.block.state.BlockState
 */
package ic2.core.item.tool;

import ic2.api.item.ElectricItem;
import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.Ic2Player;
import ic2.core.util.LogCategory;
import ic2.core.init.OreValues;
import ic2.core.item.BaseElectricItem;
import ic2.core.item.IHandHeldInventory;
import ic2.core.item.tool.ContainerToolScanner;
import ic2.core.item.tool.HandHeldScanner;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.ItemComparableItemStack;
import ic2.core.util.StackUtil;
import ic2.core.util.Tuple;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.state.BlockState;

public class ItemScanner
extends BaseElectricItem
implements IBoxable,
IHandHeldInventory {
    public ItemScanner(Item.Properties properties, double d, double d2, int n) {
        super(properties, d, d2, n);
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
        list.add((Component)Component.translatable((String)"ic2.scanner.range", (Object[])new Object[]{"" + this.getScanRange()}).withStyle(ChatFormatting.GRAY));
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = StackUtil.get(player, interactionHand);
        if (this.tier == 1 && !ElectricItem.manager.use(itemStack, 50.0, (LivingEntity)player) || this.tier == 2 && !ElectricItem.manager.use(itemStack, 250.0, (LivingEntity)player)) {
            return new InteractionResultHolder(InteractionResult.FAIL, (Object)itemStack);
        }
        if (!level.isClientSide) {
            if (this.getInventory(player, interactionHand, itemStack).openManagedItem(player, interactionHand, null) && player.containerMenu instanceof ContainerToolScanner) {
                ContainerToolScanner containerToolScanner = (ContainerToolScanner)player.containerMenu;
                Map<ItemComparableItemStack, Integer> map = this.scan(player.getCommandSenderWorld(), player.blockPosition(), this.getScanRange());
                List<Tuple.T2<ItemStack, Integer>> list2 = this.scanMapToSortedList(map);
                // 第四十轮诊断（扫描器"结果恒为空"排查）：把"服务端扫到几条"与"能发给几个 listener"都写进日志。
                // 若 results>0 而客户端仍空 → 是同步问题；若 results=0 → 看同一次 scan 里打印的 nonAir/oreHits 计数。
                IC2.log.info(LogCategory.General, "[ScannerDiag] player=%s results=%d listeners=%d containerId=%d range=%d",
                        player.getName().getString(), list2.size(), containerToolScanner.getListeners().size(), containerToolScanner.containerId, this.getScanRange());
                containerToolScanner.setResults(list2);
            }
        } else {
            player.playSound(Ic2SoundEvents.ITEM_SCANNER_USE, 1.0f, 1.0f);
        }
        return new InteractionResultHolder(InteractionResult.SUCCESS, (Object)itemStack);
    }

    public boolean onDroppedByPlayer(ItemStack itemStack, Player player) {
        HandHeldScanner handHeldScanner;
        if (!player.getCommandSenderWorld().isClientSide && !StackUtil.isEmpty(itemStack) && player.containerMenu instanceof ContainerToolScanner && (handHeldScanner = (HandHeldScanner)((ContainerToolScanner)player.containerMenu).base).isThisContainer(itemStack)) {
            handHeldScanner.saveAsThrown(itemStack);
            ((ServerPlayer)player).closeContainer();
        }
        return true;
    }

    public int startLayerScan(ItemStack itemStack) {
        return ElectricItem.manager.use(itemStack, 50.0, null) ? this.getScanRange() / 2 : 0;
    }

    public int getScanRange() {
        return 6;
    }

    @Override
    public boolean canBeStoredInToolbox(ItemStack itemStack) {
        return true;
    }

    @Override
    public IHasGui getInventory(Player player, InteractionHand interactionHand, ItemStack itemStack) {
        return new HandHeldScanner(player, interactionHand, itemStack);
    }

    private Map<ItemComparableItemStack, Integer> scan(Level level, BlockPos blockPos, int n) {
        HashMap<ItemComparableItemStack, Integer> hashMap = new HashMap<ItemComparableItemStack, Integer>();
        PathNavigationRegion pathNavigationRegion = new PathNavigationRegion(level, blockPos.offset(-n, -n, -n), blockPos.offset(n, n, n));
        Player player = Ic2Player.get(level);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        // 第四十轮诊断计数器
        int diagAir = 0;
        int diagNoChunk = 0;
        int diagNonAir = 0;
        int diagOre = 0;
        int diagDrops = 0;
        for (int i = blockPos.getY() - n; i <= blockPos.getY() + n; ++i) {
            for (int j = blockPos.getZ() - n; j <= blockPos.getZ() + n; ++j) {
                for (int k = blockPos.getX() - n; k <= blockPos.getX() + n; ++k) {
                    List<ItemStack> list;
                    mutableBlockPos.set(k, i, j);
                    // 第三十六轮：PathNavigationRegion 只缓存中心区块，越界位置在区块未加载时会取到 null 区块
                    // 而 NPE；先确认所在区块已加载（扫描不应触发区块加载）。
                    if (!level.hasChunkAt((BlockPos)mutableBlockPos)) {
                        ++diagNoChunk;
                        continue;
                    }
                    BlockState blockState = pathNavigationRegion.getBlockState((BlockPos)mutableBlockPos);
                    if (blockState.isAir()) {
                        ++diagAir;
                        continue;
                    }
                    ++diagNonAir;
                    ItemStack itemStack = StackUtil.getPickStack(level, (BlockPos)mutableBlockPos, blockState, player);
                    // 第三十六轮修复：
                    // 1) 先判"这个方块本身是不是矿石"——OreValues 已改为按 1.21 的方块标签判定
                    //    （1.12.2 靠 OreDictionary.OreRegisterEvent 填表，1.21 无此机制，迁移时整条填充链丢失）；
                    // 2) 下面的 getDrops 必须传 level（服务端就是 ServerLevel）。旧代码传 pathNavigationRegion，
                    //    而 StackUtil.getDrops 内部会强转 (ServerLevel) —— 实测每次右键扫描器都在这里抛
                    //    ClassCastException（logs/2026-10-02-1.log:1085，服务端 suppress 掉异常），
                    //    于是 setResults 从未执行、扫描结果恒为空，这就是"扫不出视线内矿物"的直接原因。
                    if (OreValues.isOre(blockState) || itemStack != null && OreValues.get(itemStack) > 0) {
                        ++diagOre;
                        list = Arrays.asList(StackUtil.isEmpty(itemStack) ? new ItemStack(blockState.getBlock()) : itemStack);
                        if (StackUtil.isEmpty((ItemStack)list.get(0))) continue;
                    } else {
                        list = StackUtil.getDrops((BlockGetter)level, (BlockPos)mutableBlockPos, blockState, 0);
                        if (list.isEmpty() || OreValues.get(list) <= 0) continue;
                        ++diagDrops;
                    }
                    for (ItemStack itemStack2 : list) {
                        ItemComparableItemStack itemComparableItemStack = new ItemComparableItemStack(itemStack2, true);
                        Integer n2 = (Integer)hashMap.get(itemComparableItemStack);
                        if (n2 == null) {
                            n2 = 0;
                        }
                        n2 = n2 + StackUtil.getSize(itemStack2);
                        hashMap.put(itemComparableItemStack, n2);
                    }
                }
            }
        }
        // 第四十轮诊断：一次扫描的完整计数（真实游戏里跑一次就能定位是"没扫到"还是"没发出去"）
        IC2.log.info(LogCategory.General, "[ScannerDiag] range=%d nonAir=%d air=%d noChunk=%d oreHits=%d dropHits=%d entries=%d",
                n, diagNonAir, diagAir, diagNoChunk, diagOre, diagDrops, hashMap.size());
        return hashMap;
    }

    private List<Tuple.T2<ItemStack, Integer>> scanMapToSortedList(Map<ItemComparableItemStack, Integer> map) {
        ArrayList<Tuple.T2<ItemStack, Integer>> arrayList = new ArrayList<Tuple.T2<ItemStack, Integer>>(map.size());
        for (Map.Entry<ItemComparableItemStack, Integer> entry : map.entrySet()) {
            arrayList.add(new Tuple.T2<ItemStack, Integer>(entry.getKey().toStack(), entry.getValue()));
        }
        Collections.sort(arrayList, new Comparator<Tuple.T2<ItemStack, Integer>>(){

            @Override
            public int compare(Tuple.T2<ItemStack, Integer> t2, Tuple.T2<ItemStack, Integer> t22) {
                return (Integer)t22.b - (Integer)t2.b;
            }
        });
        return arrayList;
    }
}

