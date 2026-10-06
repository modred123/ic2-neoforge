package ic2.core.item;

import ic2.core.block.state.IIdProvider;
import ic2.core.init.Localization;
import ic2.core.item.ItemIC2;
import ic2.core.ref.IMultiItem;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * IC2 多变体物品基类（ex112 兼容版，适配 1.21.1）：
 * - 1.12.2 的 EnumProperty（block.state）简化为 typeClass + IIdProvider 枚举遍历
 * - 变体 id 仍存于 damage（保持 1.12 相似度），1.21.1 用 setDamageValue
 * - 生命周期方法桥接：use/useOn/inventoryTick -> 旧签名钩子（子类可 override）
 * - 内部 handler 接口改用 InteractionResult
 */
public class ItemMulti<T extends Enum<T> & IIdProvider>
extends ItemIC2
implements IMultiItem<T> {
    protected final Class<T> typeClass;
    private final Map<T, IItemRightClickHandler> rightClickHandlers = new IdentityHashMap<T, IItemRightClickHandler>();
    private final Map<T, IItemUseHandler> useHandlers = new IdentityHashMap<T, IItemUseHandler>();
    private final Map<T, IItemUpdateHandler> updateHandlers = new IdentityHashMap<T, IItemUpdateHandler>();
    private final Map<T, Rarity> rarityFilter = new IdentityHashMap<T, Rarity>();

    public static <T extends Enum<T> & IIdProvider> ItemMulti<T> create(ItemName name, Class<T> typeClass) {
        if (EnumSet.allOf(typeClass).size() > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Too many values to fit in a short for " + typeClass);
        }
        return new ItemMulti<T>(name, typeClass);
    }

    protected ItemMulti(ItemName name, Class<T> typeClass) {
        super(name);
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

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerModels(ItemName name) {
        for (T type : this.getAllowedValues()) {
            this.registerModel(type.getId(), name, type.getModelName());
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public final int getItemColor(ItemStack stack) {
        T type = this.getType(stack);
        if (type == null) {
            return super.getItemColor(stack);
        }
        return type.getColor();
    }

    @Override
    public final String getTranslationKey(ItemStack stack) {
        T type = this.getType(stack);
        if (type == null) {
            return super.getTranslationKey(stack);
        }
        return super.getTranslationKey(stack) + "." + type.getName();
    }

    @Override
    public ItemStack getItemStack(T type) {
        if (!this.getAllowedValues().contains(type)) {
            throw new IllegalArgumentException("invalid property value " + type + " for " + this);
        }
        return this.getItemStackUnchecked(type);
    }

    private ItemStack getItemStackUnchecked(T type) {
        ItemStack stack = new ItemStack(this, 1);
        stack.setDamageValue(type.getId());
        return stack;
    }

    @Override
    public ItemStack getItemStack(String variant) {
        T type = this.getValue(variant);
        if (type == null) {
            throw new IllegalArgumentException("invalid variant " + variant + " for " + this);
        }
        return this.getItemStackUnchecked(type);
    }

    @Override
    public String getVariant(ItemStack stack) {
        if (stack == null) {
            throw new NullPointerException("null stack");
        }
        if (stack.getItem() != this) {
            throw new IllegalArgumentException("The stack " + stack + " doesn't match " + this);
        }
        T type = this.getType(stack);
        if (type == null) {
            throw new IllegalArgumentException("The stack " + stack + " doesn't reference any valid subtype");
        }
        return type.getName();
    }

    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
        if (this.allowedIn(tab)) {
            for (T type : this.getAllowedValues()) {
                items.add(this.getItemStackUnchecked(type));
            }
        }
    }

    @Override
    public Set<T> getAllTypes() {
        return EnumSet.allOf(this.typeClass);
    }

    public final T getType(ItemStack stack) {
        return this.getValue(stack.getDamageValue());
    }

    // == = 1.21.1 生命周期（桥接旧签名钩子，子类可 override） == =

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        return this.onItemRightClick(world, player, hand);
    }

    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        ItemStack stack = StackUtil.get(player, hand);
        T type = this.getType(stack);
        if (type == null) {
            return InteractionResultHolder.pass(stack);
        }
        IItemRightClickHandler handler = this.rightClickHandlers.get(type);
        if (handler == null) {
            return InteractionResultHolder.pass(stack);
        }
        return handler.onRightClick(stack, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        return this.onItemUse(player, context.getLevel(), pos, hand, side,
            (float)context.getClickLocation().x - pos.getX(),
            (float)context.getClickLocation().y - pos.getY(),
            (float)context.getClickLocation().z - pos.getZ());
    }

    public InteractionResult onItemUse(Player player, Level world, BlockPos pos, InteractionHand hand, Direction side, float hitX, float hitY, float hitZ) {
        ItemStack stack = StackUtil.get(player, hand);
        T type = this.getType(stack);
        if (type == null) {
            return InteractionResult.PASS;
        }
        IItemUseHandler handler = this.useHandlers.get(type);
        if (handler == null) {
            return InteractionResult.PASS;
        }
        return handler.onUse(stack, player, pos, hand, side);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slotIndex, boolean isCurrentItem) {
        this.onUpdate(stack, world, entity, slotIndex, isCurrentItem);
    }

    public void onUpdate(ItemStack stack, Level world, Entity entity, int slotIndex, boolean isCurrentItem) {
        T type = this.getType(stack);
        if (type == null) {
            return;
        }
        IItemUpdateHandler handler = this.updateHandlers.get(type);
        if (handler == null) {
            return;
        }
        handler.onUpdate(stack, world, entity, slotIndex, isCurrentItem);
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        Rarity rarity = this.rarityFilter.get(this.getType(stack));
        return rarity != null ? rarity : super.getRarity(stack);
    }

    public void setRightClickHandler(T type, IItemRightClickHandler handler) {
        if (type == null) {
            for (T cType : this.getAllowedValues()) {
                this.setRightClickHandler(cType, handler);
            }
        } else {
            this.rightClickHandlers.put(type, handler);
        }
    }

    public void setUseHandler(T type, IItemUseHandler handler) {
        if (type == null) {
            for (T cType : this.getAllowedValues()) {
                this.setUseHandler(cType, handler);
            }
        } else {
            this.useHandlers.put(type, handler);
        }
    }

    public void setUpdateHandler(T type, IItemUpdateHandler handler) {
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

    public static interface IItemUpdateHandler {
        public void onUpdate(ItemStack var1, Level var2, Entity var3, int var4, boolean var5);
    }

    public static interface IItemUseHandler {
        public InteractionResult onUse(ItemStack var1, Player var2, BlockPos var3, InteractionHand var4, Direction var5);
    }

    public static interface IItemRightClickHandler {
        public InteractionResultHolder<ItemStack> onRightClick(ItemStack var1, Player var2, InteractionHand var3);
    }
}
