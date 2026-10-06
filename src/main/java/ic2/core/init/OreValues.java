/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package ic2.core.init;

import ic2.core.IC2;
import ic2.core.ref.Ic2BlockTags;
import ic2.core.util.ItemComparableItemStack;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class OreValues {
    private static final Map<ItemComparableItemStack, Integer> stackValues = new HashMap<ItemComparableItemStack, Integer>();

    /*
     * 第三十六轮修复（用户实测：OD/OV 扫描器扫不出视线内的矿物）。
     *
     * 1.12.2 的矿物价值表靠两条路径填充：
     *   a) IC2.registerOre(OreDictionary.OreRegisterEvent) —— 所有 ore* 前缀的矿石方块（价值 1..5）；
     *   b) 配置 misc/additionalValuableOres。
     * 1.21 没有 OreDictionary，迁移时这两条路径**整体丢失** -> add() 从此没有任何调用者
     * -> stackValues 恒空 -> ItemScanner.scan() 里的 OreValues.get(...) 恒为 0
     * -> 扫描结果恒为空（TileEntityMiner / TileEntityAdvMiner 的矿物判定同样失效，
     *    采矿机只靠 Ic2BlockTags.ORES 兜底，扫不到原版矿石）。
     *
     * 1.21 的等价物是**方块标签**，故此处按标签实时判定：
     *   - BASE_ORE_TAGS：IC2 的 c:ores + 原版 8 个 *_ores（始终可用，不依赖数据包时序）；
     *   - 动态列表：数据包里 c:ores/<材料> 一类矿石标签（重建于数据包重载，见 invalidateOreTags）；
     *   - 兜底：注册名即 "ore"/"*_ore"（覆盖没打标签的模组矿石，等价 1.12.2 的 ore* 前缀语义）。
     * 判定结果不缓存（BlockState.is(TagKey) 是 Holder 的 O(1) 标签集合查找），
     * 只对"标签清单"本身做一次惰性收集，避免每次判定都遍历注册表的全部标签。
     */

    /** 始终有效的矿石标签（1.12.2 "ore* 前缀" 的固定对应物）。 */
    private static final List<TagKey<Block>> BASE_ORE_TAGS = List.of(
        Ic2BlockTags.ORES,
        TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("forge", "ores")),
        BlockTags.COAL_ORES,
        BlockTags.COPPER_ORES,
        BlockTags.DIAMOND_ORES,
        BlockTags.EMERALD_ORES,
        BlockTags.GOLD_ORES,
        BlockTags.IRON_ORES,
        BlockTags.LAPIS_ORES,
        BlockTags.REDSTONE_ORES
    );

    /** 显式补录：不属于任何矿石标签、注册名也不含 "ore"，但语义上是矿物的方块。 */
    private static final Set<ResourceLocation> EXTRA_ORE_IDS = Set.of(
        ResourceLocation.withDefaultNamespace("ancient_debris")
    );

    /** null 表示"尚未收集/需要按最新标签重建"。 */
    private static volatile List<TagKey<Block>> extraOreTags;

    public static void add(ItemStack itemStack, int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("value has to be > 0");
        }
        ItemComparableItemStack itemComparableItemStack = new ItemComparableItemStack(itemStack, true);
        Integer n2 = stackValues.put(itemComparableItemStack, n);
        if (n2 != null && n2 > n) {
            stackValues.put(itemComparableItemStack, n2);
        }
    }

    /** 数据包/标签重载后丢弃标签清单，下次判定时按新数据重建（由 TagsUpdatedEvent 调用）。 */
    public static void invalidateOreTags() {
        extraOreTags = null;
    }

    private static List<TagKey<Block>> extraOreTags() {
        List<TagKey<Block>> list = extraOreTags;
        if (list != null) {
            return list;
        }
        ArrayList<TagKey<Block>> arrayList = new ArrayList<TagKey<Block>>();
        try {
            BuiltInRegistries.BLOCK.getTags().forEach(pair -> {
                TagKey<Block> tagKey = pair.getFirst();
                if (!BASE_ORE_TAGS.contains(tagKey) && OreValues.isOreTagName(tagKey.location())) {
                    arrayList.add(tagKey);
                }
            });
        }
        catch (Throwable throwable) {
            IC2.log.warn(LogCategory.General, "Failed to collect ore block tags: %s", throwable.toString());
        }
        extraOreTags = arrayList;
        return arrayList;
    }

    /** 标签名是否形如"矿石"：ores、ores/<材料>、*_ores。 */
    private static boolean isOreTagName(ResourceLocation resourceLocation) {
        String string = resourceLocation.getPath();
        return string.equals("ores") || string.startsWith("ores/") || string.endsWith("_ores");
    }

    /** 该方块状态是否为矿石（1.21 的 OreDictionary 等价判定）。 */
    public static boolean isOre(BlockState blockState) {
        if (blockState.isAir()) {
            return false;
        }
        for (TagKey<Block> tagKey : BASE_ORE_TAGS) {
            if (blockState.is(tagKey)) {
                return true;
            }
        }
        for (TagKey<Block> tagKey : OreValues.extraOreTags()) {
            if (blockState.is(tagKey)) {
                return true;
            }
        }
        ResourceLocation resourceLocation = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());
        if (resourceLocation == null) {
            return false;
        }
        if (EXTRA_ORE_IDS.contains(resourceLocation)) {
            return true;
        }
        String string = resourceLocation.getPath();
        return string.equals("ore") || string.endsWith("_ore") || string.endsWith("_ores");
    }

    /** 矿物价值（1.12.2 registerOre 的分级：煤 1 / 铜锡铅 2 / 铁金红石青金石银 3 / 铀宝石 4 / 钻石绿宝石钨 5）。 */
    public static int oreValue(Block block) {
        ResourceLocation resourceLocation = BuiltInRegistries.BLOCK.getKey(block);
        String string = resourceLocation == null ? "" : resourceLocation.getPath();
        if (string.contains("diamond") || string.contains("emerald") || string.contains("tungsten")) {
            return 5;
        }
        if (string.contains("uranium") || string.contains("plutonium") || string.contains("iridium") || string.contains("ruby") || string.contains("sapphire")) {
            return 4;
        }
        if (string.contains("iron") || string.contains("gold") || string.contains("redstone") || string.contains("lapis") || string.contains("silver")) {
            return 3;
        }
        if (string.contains("copper") || string.contains("tin") || string.contains("lead") || string.contains("quartz") || string.contains("aluminium") || string.contains("aluminum") || string.contains("nickel") || string.contains("zinc") || string.contains("osmium")) {
            return 2;
        }
        return 1;
    }

    public static int get(ItemStack itemStack) {
        int n;
        if (StackUtil.isEmpty(itemStack)) {
            return 0;
        }
        Integer n2 = stackValues.get(new ItemComparableItemStack(itemStack, false));
        if (n2 != null) {
            n = n2;
        } else {
            // 1.21：不再有 OreDictionary 注册事件，改由标签/注册名实时判定物品对应的方块是不是矿石。
            Block block = Block.byItem(itemStack.getItem());
            n = block != Blocks.AIR && OreValues.isOre(block.defaultBlockState()) ? OreValues.oreValue(block) : 0;
        }
        return n * StackUtil.getSize(itemStack);
    }

    public static int get(List<ItemStack> list) {
        int n = 0;
        for (ItemStack itemStack : list) {
            n += OreValues.get(itemStack);
        }
        return n;
    }
}
