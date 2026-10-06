/*
 * ex112 ItemToolIC2 兼容层 1.21.1 适配版
 * 1.12.2 的 ItemTool/ToolMaterial/HarvestLevel 体系在 1.21.1 已移除，
 * 改用 Item + 自定义 efficiency 字段（保持子类构造签名不变）。
 */
package ic2.core.item;

import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.init.BlocksItems;
import ic2.core.init.Localization;
import ic2.core.item.tool.HarvestLevel;
import ic2.core.item.tool.IToolClass;
import ic2.core.item.tool.ToolClass;
import ic2.core.ref.IItemModelProvider;
import ic2.core.profile.Version;
import ic2.core.ref.ItemName;
import java.util.Set;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public abstract class ItemToolIC2
extends Item
implements IItemModelProvider,
IBoxable {
    protected Rarity rarity = Rarity.COMMON;
    protected final Set<? extends IToolClass> toolClasses;
    protected final float efficiency;
    private int ic2MaxDamage = -1;

    protected ItemToolIC2(ItemName name, HarvestLevel harvestLevel, Set<? extends IToolClass> toolClasses) {
        this(name, harvestLevel, toolClasses, new java.util.HashSet<Block>());
    }

    protected ItemToolIC2(ItemName name, HarvestLevel harvestLevel, Set<? extends IToolClass> toolClasses, Set<Block> mineableBlocks) {
        this(name, 0.0f, 0.0f, harvestLevel, toolClasses, mineableBlocks);
    }

    protected ItemToolIC2(ItemName name, float damage, float speed, HarvestLevel harvestLevel, Set<? extends IToolClass> toolClasses, Set<Block> mineableBlocks) {
        super(new Item.Properties().stacksTo(1));
        this.toolClasses = toolClasses;
        this.efficiency = harvestLevel.level >= 3 ? 8.0f : (harvestLevel.level >= 2 ? 6.0f : (harvestLevel.level >= 1 ? 4.0f : 2.0f));
        if (toolClasses.contains(ToolClass.Pickaxe) && harvestLevel.level >= 3) {
            mineableBlocks.add(Blocks.OBSIDIAN);
        }
        if (name != null) {
            BlocksItems.registerItem(this, IC2.getIdentifier(name.name()));
            name.setInstance(this);
        }
    }

    public String getTranslationKey() {
        return "ic2." + super.getDescriptionId().substring(5);
    }

    protected void setHasSubtypes(boolean hasSubtypes) {
    }

    protected boolean allowedIn(net.minecraft.world.item.CreativeModeTab tab) {
        return true;
    }

    /** ex112 兼容：运行时设置最大耐久（1.21.1 的 durability 需在 Properties 中设置，此处用字段模拟） */
    protected void setMaxDamage(int damage) {
        this.ic2MaxDamage = damage;
    }

    public int getMaxDamage(ItemStack stack) {
        return this.ic2MaxDamage >= 0 ? this.ic2MaxDamage : super.getMaxDamage(stack);
    }

    public String getTranslationKey(ItemStack itemStack) {
        return this.getTranslationKey();
    }

    public String getUnlocalizedNameInefficiently(ItemStack itemStack) {
        return this.getTranslationKey(itemStack);
    }

    public String getItemStackDisplayName(ItemStack itemStack) {
        return Localization.translate(this.getTranslationKey(itemStack));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return ItemIC2.shouldReequip(oldStack, newStack, slotChanged);
    }

    public boolean canHarvestBlock(BlockState state, ItemStack itemStack) {
        for (IToolClass iToolClass : this.toolClasses) {
            if (iToolClass.getBlacklist().contains(state.getBlock())) {
                return false;
            }
            if (iToolClass.getWhitelist().contains(state.getBlock())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public float getDestroySpeed(ItemStack itemStack, BlockState state) {
        return this.canHarvestBlock(state, itemStack) ? this.efficiency : super.getDestroySpeed(itemStack, state);
    }

    public Rarity getRarity(ItemStack stack) {
        return stack.isEnchanted() && this.rarity != Rarity.EPIC ? Rarity.RARE : this.rarity;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerModels(ItemName name) {
        ItemIC2.registerModel((Item)this, 0, name, null);
    }

    @Override
    public boolean canBeStoredInToolbox(ItemStack itemStack) {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public int getItemColor(ItemStack stack, int tintIndex) {
        return 0xFFFFFF;
    }

    public ItemToolIC2 setRarity(Rarity rarity) {
        if (rarity == null) {
            throw new NullPointerException("Rarity cannot be null");
        }
        this.rarity = rarity;
        return this;
    }

    protected boolean isEnabled() {
        return Version.shouldEnable(this.getClass());
    }
}
