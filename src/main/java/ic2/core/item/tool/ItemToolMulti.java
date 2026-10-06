/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.creativetab.CreativeTabs
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.item.Rarity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.util.InteractionResult
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.core.Direction
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.core.NonNullList
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.Level
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.DistOnly
 */
package ic2.core.item.tool;
import net.neoforged.api.distmarker.OnlyIn;

import ic2.api.item.ICustomDamageItem;
import ic2.core.block.state.IIdProvider;
import ic2.core.item.ItemIC2;
import ic2.core.item.ItemMulti;
import ic2.core.item.ItemToolIC2;
import ic2.core.item.tool.HarvestLevel;
import ic2.core.item.tool.IToolClass;
import ic2.core.ref.IMultiItem;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.EnumSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;

public class ItemToolMulti<T extends Enum<T> & IIdProvider>
extends ItemToolIC2
implements IMultiItem<T>,
ICustomDamageItem {
    protected final Class<T> typeClass;
    private final Map<T, ItemMulti.IItemRightClickHandler> rightClickHandlers = new IdentityHashMap<T, ItemMulti.IItemRightClickHandler>();
    private final Map<T, ItemMulti.IItemUseHandler> useHandlers = new IdentityHashMap<T, ItemMulti.IItemUseHandler>();
    private final Map<T, ItemMulti.IItemUpdateHandler> updateHandlers = new IdentityHashMap<T, ItemMulti.IItemUpdateHandler>();
    private final Map<T, Rarity> rarityFilter = new IdentityHashMap<T, Rarity>();

    public static <T extends Enum<T> & IIdProvider> ItemToolMulti<T> create(ItemName name, Class<T> typeClass, float damage, float speed, HarvestLevel harvestLevel, Set<? extends IToolClass> toolClasses, Set<Block> mineableBlocks) {
        if (EnumSet.allOf(typeClass).size() > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Too many values to fit in a short for " + typeClass);
        }
        return new ItemToolMulti<T>(name, typeClass, damage, speed, harvestLevel, toolClasses, mineableBlocks);
    }

    private ItemToolMulti(ItemName name, Class<T> typeClass, float damage, float speed, HarvestLevel harvestLevel, Set<? extends IToolClass> toolClasses, Set<Block> mineableBlocks) {
        super(name, damage, speed, harvestLevel, toolClasses, mineableBlocks);
        this.typeClass = typeClass;
        this.setHasSubtypes(true);
    }

    // == = 变体辅助（替代 1.12.2 的 EnumProperty） == =
    protected Set<T> getAllowedValues() {
        return EnumSet.allOf(this.typeClass);
    }

    protected T getValue(int id) {
        for (T type : this.getAllowedValues()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return null;
    }

    protected T getValue(String variant) {
        for (T type : this.getAllowedValues()) {
            if (type.getName().equals(variant)) {
                return type;
            }
        }
        return null;
    }

    protected ItemToolMulti(ItemName name, Class<T> typeClass, HarvestLevel harvestLevel, Set<? extends IToolClass> toolClasses) {
        this(name, typeClass, harvestLevel, toolClasses, new HashSet<Block>());
    }

    protected ItemToolMulti(ItemName name, Class<T> typeClass, HarvestLevel harvestLevel, Set<? extends IToolClass> toolClasses, Set<Block> mineableBlocks) {
        this(name, typeClass, 0.0f, 0.0f, harvestLevel, toolClasses, mineableBlocks);
    }

    @Override
    public final String getTranslationKey(ItemStack stack) {
        T type = this.getType(stack);
        return type == null ? super.getTranslationKey(stack) : super.getTranslationKey(stack) + "." + type.getName();
    }

    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> subItems) {
        if (this.allowedIn(tab)) {
            for (T type : this.getAllowedValues()) {
                subItems.add(this.getItemStackUnchecked(type));
            }
        }
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        Rarity rarity = this.rarityFilter.get(this.getType(stack));
        return rarity != null ? rarity : super.getRarity(stack);
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = StackUtil.get(player, hand);
        T type = this.getType(stack);
        if (type == null) {
            return InteractionResultHolder.pass(stack);
        }
        ItemMulti.IItemRightClickHandler handler = this.rightClickHandlers.get(type);
        if (handler == null) {
            return InteractionResultHolder.pass(stack);
        }
        return handler.onRightClick(stack, player, hand);
    }

    public InteractionResult onItemUse(Player player, Level world, BlockPos pos, InteractionHand hand, Direction side, float hitX, float hitY, float hitZ) {
        ItemStack stack = StackUtil.get(player, hand);
        T type = this.getType(stack);
        if (type == null) {
            return InteractionResult.PASS;
        }
        ItemMulti.IItemUseHandler handler = this.useHandlers.get(type);
        if (handler == null) {
            return InteractionResult.PASS;
        }
        return handler.onUse(stack, player, pos, hand, side);
    }

    public void onUpdate(ItemStack stack, Level world, Entity entity, int slotIndex, boolean isCurrentItem) {
        T type = this.getType(stack);
        if (type == null) {
            return;
        }
        ItemMulti.IItemUpdateHandler handler = this.updateHandlers.get(type);
        if (handler == null) {
            return;
        }
        handler.onUpdate(stack, world, entity, slotIndex, isCurrentItem);
    }

    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    public double getDurabilityForDisplay(ItemStack stack) {
        return (double)this.getCustomDamage(stack) / (double)this.getMaxCustomDamage(stack);
    }

    @Override
    public boolean isDamageable(ItemStack itemStack) {
        return true;
    }

    public boolean isDamaged(ItemStack stack) {
        return this.getCustomDamage(stack) > 0;
    }

    public int getDamage(ItemStack stack) {
        return this.getCustomDamage(stack);
    }

    public int getMaxDamage(ItemStack stack) {
        return this.getMaxCustomDamage(stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerModels(ItemName name) {
        for (T type : this.getAllowedValues()) {
            ItemIC2.registerModel((Item)this, type.getId(), name, type.getModelName());
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public final int getItemColor(ItemStack stack, int tintIndex) {
        T type = this.getType(stack);
        return type == null ? super.getItemColor(stack, tintIndex) : type.getColor();
    }

    @Override
    public ItemStack getItemStack(T type) {
        if (!this.getAllowedValues().contains(type)) {
            throw new IllegalArgumentException("Invalid property value " + type + " for " + this);
        }
        return this.getItemStackUnchecked(type);
    }

    @Override
    public ItemStack getItemStack(String variant) {
        T type = this.getValue(variant);
        if (type == null) {
            throw new IllegalArgumentException("Invalid variant " + variant + " for " + this);
        }
        return this.getItemStackUnchecked(type);
    }

    @Override
    public String getVariant(ItemStack stack) {
        if (stack == null) {
            throw new NullPointerException("The stack cannot be null");
        }
        if (stack.getItem() != this) {
            throw new IllegalArgumentException("The stack " + stack + " does not match " + this);
        }
        T type = this.getType(stack);
        if (type == null) {
            throw new IllegalArgumentException("The stack " + stack + " does not reference any valid subtype");
        }
        return type.getName();
    }

    @Override
    public Set<T> getAllTypes() {
        return EnumSet.allOf(this.typeClass);
    }

    @Override
    public int getCustomDamage(ItemStack stack) {
        if (StackUtil.getTag(stack) == null) {
            return 0;
        }
        CompoundTag data = StackUtil.getTag(stack);
        assert (data != null);
        return data.contains("durability") ? data.getInt("durability") : 0;
    }

    @Override
    public int getMaxCustomDamage(ItemStack stack) {
        if (StackUtil.getTag(stack) == null) {
            return 0;
        }
        CompoundTag data = StackUtil.getTag(stack);
        assert (data != null);
        return data.contains("maxDurability") ? data.getInt("maxDurability") : 0;
    }

    @Override
    public void setCustomDamage(ItemStack stack, int damage) {
        CompoundTag data = StackUtil.getOrCreateNbtData(stack);
        data.putInt("durability", damage);
    }

    @Override
    public boolean applyCustomDamage(ItemStack stack, int damage, LivingEntity source) {
        this.setCustomDamage(stack, this.getCustomDamage(stack) + damage);
        return true;
    }

    public final T getType(ItemStack stack) {
        return this.getValue(stack.getDamageValue());
    }

    public void setRightClickHandler(T type, ItemMulti.IItemRightClickHandler handler) {
        if (type == null) {
            for (T cType : this.getAllowedValues()) {
                this.setRightClickHandler(cType, handler);
            }
        } else {
            this.rightClickHandlers.put(type, handler);
        }
    }

    public void setUseHandler(T type, ItemMulti.IItemUseHandler handler) {
        if (type == null) {
            for (T cType : this.getAllowedValues()) {
                this.setUseHandler(cType, handler);
            }
        } else {
            this.useHandlers.put(type, handler);
        }
    }

    public void setUpdateHandler(T type, ItemMulti.IItemUpdateHandler handler) {
        if (type == null) {
            for (T cType : this.getAllowedValues()) {
                this.setUpdateHandler(cType, handler);
            }
        } else {
            this.updateHandlers.put(type, handler);
        }
    }

    public void setRarity(T type, Rarity rarity) {
        if (type == null) {
            this.setRarity(rarity);
        } else {
            this.rarityFilter.put(type, rarity);
        }
    }

    private ItemStack getItemStackUnchecked(T type) {
        ItemStack stack = new ItemStack((Item)this, 1);
        stack.setDamageValue(type.getId());
        return stack;
    }
}

