package ic2.core.item;

import ic2.core.IC2;
import ic2.core.init.BlocksItems;
import ic2.core.init.Localization;
import ic2.core.profile.Version;
import ic2.core.ref.IItemModelProvider;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

/**
 * IC2 物品基类（ex112 兼容版，适配 1.21.1）：
 * - 1.21.1 的 Item 构造必须传 Item.Properties
 * - 物品注册走 BlocksItems.registerItem -> EnvProxyForge.registerItem（pendingRegistrations）
 * - registerModels 保留签名但为空（1.21.1 物品模型由 assets JSON 自动加载）
 * - 1.12.2 的 setCreativeTab/Capability 体系在 1.21.1 移除，不再使用
 */
public class ItemIC2
extends Item
implements IItemModelProvider {
    private Rarity rarity = Rarity.COMMON;

    // == = 1.12.2 兼容字段（运行时属性设置 → 1.21.1 override） == =
    private int ic2MaxDamage = -1;
    private int ic2MaxStackSize = 0;
    private Item ic2ContainerItem = null;
    private boolean ic2HasSubtypes = false;
    private boolean ic2NoRepair = false;

    public ItemIC2(ItemName name) {
        this(name, new Item.Properties());
    }

    public ItemIC2(ItemName name, Item.Properties properties) {
        super(properties);
        if (name != null) {
            BlocksItems.registerItem(this, IC2.getIdentifier(name.name()));
            name.setInstance(this);
        }
    }

    // == = 1.12.2 兼容方法（子类沿用旧调用，内部适配 1.21.1） == =
    protected void setMaxDamage(int damage) {
        this.ic2MaxDamage = damage;
    }

    protected void setMaxStackSize(int size) {
        this.ic2MaxStackSize = size;
    }

    protected void setContainerItem(Item item) {
        this.ic2ContainerItem = item;
    }

    protected void setHasSubtypes(boolean hasSubtypes) {
        this.ic2HasSubtypes = hasSubtypes;
    }

    protected void setNoRepair(boolean noRepair) {
        this.ic2NoRepair = noRepair;
    }

    protected void setCreativeTab(net.minecraft.world.item.CreativeModeTab tab) {
        // 1.21.1 物品加入创造栏由 CreativeModeTab 的 displayItems 处理，此处忽略
    }

    /** ex112 兼容：创造模式标签页过滤（1.21.1 无该钩子，默认允许） */
    protected boolean allowedIn(net.minecraft.world.item.CreativeModeTab tab) {
        return true;
    }

    protected void setHarvestLevel(String tool, int level) {
        // 1.21.1 挖掘等级由 block tags（mineable/needs_*_tool）处理，此处忽略
    }

    public int getMaxDamage() {
        return this.ic2MaxDamage >= 0 ? this.ic2MaxDamage : 0;
    }

    public int getMaxStackSize() {
        return this.ic2MaxStackSize > 0 ? this.ic2MaxStackSize : 64;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return this.ic2ContainerItem != null ? new ItemStack(this.ic2ContainerItem) : ItemStack.EMPTY;
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return this.ic2ContainerItem != null;
    }

    @Override
    public void registerModels(ItemName name) {
        // 1.21.1：物品模型由 assets/ic2/models/item/ 下的 JSON 自动加载，无需手动注册
    }

    protected void registerModel(int meta, ItemName name) {
    }

    protected void registerModel(int meta, ItemName name, String extraName) {
    }

    public static void registerModel(Item item, int meta, ItemName name, String extraName) {
    }

    public static ResourceLocation getModelLocation(ItemName name, String extraName) {
        return IC2.getIdentifier(name.getPath(extraName));
    }

    public int getItemColor(ItemStack stack) {
        return 0xFFFFFF;
    }

    public String getTranslationKey() {
        return "ic2." + super.getDescriptionId().substring(5);
    }

    public String getTranslationKey(ItemStack stack) {
        return this.getTranslationKey();
    }

    public String getUnlocalizedNameInefficiently(ItemStack stack) {
        return this.getTranslationKey(stack);
    }

    public String getItemStackDisplayName(ItemStack stack) {
        return Localization.translate(this.getTranslationKey(stack));
    }

    protected boolean isEnabled() {
        return Version.shouldEnable(this.getClass());
    }

    public ItemIC2 setRarity(Rarity rarity) {
        if (rarity == null) {
            throw new NullPointerException("null rarity");
        }
        this.rarity = rarity;
        return this;
    }

    public Rarity getRarity(ItemStack stack) {
        if (stack.isEnchanted() && this.rarity != Rarity.EPIC) {
            return Rarity.RARE;
        }
        return this.rarity;
    }

    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return ItemIC2.shouldReequip(oldStack, newStack, slotChanged);
    }

    public static boolean shouldReequip(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (!StackUtil.checkItemEquality(newStack, oldStack)) {
            return true;
        }
        if (oldStack == null) {
            return false;
        }
        if (StackUtil.getSize(oldStack) != StackUtil.getSize(newStack)) {
            return true;
        }
        return slotChanged && StackUtil.checkItemEqualityStrict(oldStack, newStack);
    }

    protected static int getRemainingUses(ItemStack stack) {
        return stack.getMaxDamage() - stack.getDamageValue() + 1;
    }
}
