/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntCollection
 *  it.unimi.dsi.fastutil.ints.IntIterator
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.NonNullList
 *  net.minecraft.core.Vec3i
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.Container
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 *  net.minecraft.world.item.enchantment.Enchantments
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.Vec3
 */
package ic2.core.util;

import com.mojang.authlib.GameProfile;
import ic2.api.recipe.IRecipeInput;
import ic2.core.IC2;
import ic2.core.Ic2Player;
import ic2.core.item.EnvItemHandler;
import ic2.core.util.LogCategory;
import ic2.core.util.Tuple;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class StackUtil {
    static final Set<String> ignoredNbtKeys = new HashSet<String>(Arrays.asList("damage", "charge", "energy", "advDmg"));
    public static final Predicate<ItemStack> anyStack = (Predicate<ItemStack>)StackUtil::lambda$static$0;
    public static final ItemStack emptyStack = ItemStack.EMPTY;
    private static final int[] emptySlotArray = new int[0];
    public static final EnvItemHandler ENV = IC2.envProxy.createItemHandler();

    public static boolean isEmpty(ItemStack itemStack) {
        return itemStack == emptyStack || itemStack == null || itemStack.getItem() == null || itemStack.getCount() <= 0;
    }

    public static boolean isEmpty(Player player, InteractionHand interactionHand) {
        return StackUtil.isEmpty(player.getItemInHand(interactionHand));
    }

    public static int getSize(ItemStack itemStack) {
        if (StackUtil.isEmpty(itemStack)) {
            return 0;
        }
        return itemStack.getCount();
    }

    public static ItemStack setSize(ItemStack itemStack, int n) {
        itemStack.setCount(n);
        return n <= 0 ? emptyStack : itemStack;
    }

    public static ItemStack incSize(ItemStack itemStack) {
        return StackUtil.incSize(itemStack, 1);
    }

    public static ItemStack incSize(ItemStack itemStack, int n) {
        return StackUtil.setSize(itemStack, StackUtil.getSize(itemStack) + n);
    }

    public static ItemStack decSize(ItemStack itemStack) {
        return StackUtil.decSize(itemStack, 1);
    }

    public static ItemStack decSize(ItemStack itemStack, int n) {
        return StackUtil.incSize(itemStack, -n);
    }

    public static ItemStack wrapEmpty(ItemStack itemStack) {
        return itemStack == null ? emptyStack : itemStack;
    }

    public static boolean check2(Iterable<List<ItemStack>> iterable) {
        for (List<ItemStack> list : iterable) {
            if (StackUtil.check(list)) continue;
            return false;
        }
        return true;
    }

    public static boolean check(ItemStack[] itemStackArray) {
        return StackUtil.check(Arrays.asList(itemStackArray));
    }

    public static boolean check(Iterable<ItemStack> iterable) {
        for (ItemStack itemStack : iterable) {
            if (StackUtil.check(itemStack)) continue;
            return false;
        }
        return true;
    }

    public static boolean check(ItemStack itemStack) {
        return itemStack.getItem() != null;
    }

    public static String toStringSafe2(Iterable<List<ItemStack>> iterable) {
        StringBuilder stringBuilder = new StringBuilder("[");
        for (List<ItemStack> list : iterable) {
            if (stringBuilder.length() > 1) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(StackUtil.toStringSafe(list));
        }
        return stringBuilder.append(']').toString();
    }

    public static String toStringSafe(ItemStack[] itemStackArray) {
        return StackUtil.toStringSafe(Arrays.asList(itemStackArray));
    }

    public static String toStringSafe(Iterable<ItemStack> iterable) {
        StringBuilder stringBuilder = new StringBuilder("[");
        for (ItemStack itemStack : iterable) {
            if (stringBuilder.length() > 1) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(StackUtil.toStringSafe(itemStack));
        }
        return stringBuilder.append(']').toString();
    }

    public static String toStringSafe(ItemStack itemStack) {
        if (itemStack == null) {
            return "(null)";
        }
        if (itemStack.getItem() == null) {
            return StackUtil.getSize(itemStack) + "x(null)@(unknown)";
        }
        return itemStack.toString();
    }

    public static ItemStack copy(ItemStack itemStack) {
        return itemStack.copy();
    }

    public static ItemStack copyWithSize(ItemStack itemStack, int n) {
        if (StackUtil.isEmpty(itemStack)) {
            throw new IllegalArgumentException("empty stack: " + StackUtil.toStringSafe(itemStack));
        }
        return StackUtil.setSize(StackUtil.copy(itemStack), n);
    }

    public static ItemStack copyShrunk(ItemStack itemStack, int n) {
        if (StackUtil.isEmpty(itemStack)) {
            throw new IllegalArgumentException("empty stack: " + StackUtil.toStringSafe(itemStack));
        }
        return StackUtil.setSize(StackUtil.copy(itemStack), StackUtil.getSize(itemStack) - n);
    }

    public static Collection<ItemStack> copy(Collection<ItemStack> collection) {
        ArrayList<ItemStack> arrayList = new ArrayList<ItemStack>(collection.size());
        for (ItemStack itemStack : collection) {
            arrayList.add(StackUtil.copy(itemStack));
        }
        return arrayList;
    }

    public static CompoundTag getOrCreateNbtData(ItemStack itemStack) {
        // 1.21.1 修复（第二十七轮）：必须返回【活引用】，否则调用方的所有写入都会静默丢失。
        //
        // 1.12.2（权威实现，ic2_src_112/ic2/core/util/StackUtil.java:936-943）：
        //     NBTTagCompound ret = stack.getTagCompound();       ← 活引用
        //     if (ret == null) { ret = new NBTTagCompound(); stack.setTagCompound(ret); }
        //     return ret;                                        ← 拿到即可直接 putXxx 生效
        //
        // 迁移版误用了 CustomData 的"拷贝式"取值 API（javap 字节码实证）：
        //     CustomData.copyTag()   → invokevirtual CompoundTag.copy; areturn   ← 深拷贝副本
        //     CustomData.of(tag)     → invokevirtual CompoundTag.copy; <init>    ← 存进去的又是副本
        //     CustomData.getUnsafe() → getfield tag; areturn                     ← 唯一返回内部真实引用
        // 于是原实现的两条分支返回的都是"孤儿 tag"：
        //     ElectricItemManager.charge() 返回非零（机器照常扣电、以为充电成功），
        //     但 "charge" 键写进了孤儿 tag ⇒ 物品电量恒为 0，tooltip 永远显示 0/10 M EU。
        // 同一根因波及全项目 81 处 getOrCreateNbtData 调用点
        // （流体单元 uses、机器 energy、升级模块配置、覆盖板、喷气背包电量……）。
        //
        // 为什么不是"直接 getUnsafe() 后原地改"：
        //     ItemStack.copy() 走 PatchedDataComponentMap.copy()，它把【同一个 patch map
        //     引用】交给新 map 并只设了 copyOnWrite=true（字节码实证），因此两个 stack 会
        //     共享同一个 CustomData 实例。原地改会串改背包/容器快照等其它 stack。
        //     必须经 set() —— PatchedDataComponentMap.set() 内部会先 ensureMapOwnership()
        //     做 patch 的 copy-on-write，让本 stack 拥有独立实例。
        net.minecraft.world.item.component.CustomData data = itemStack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        CompoundTag compoundTag = data == null ? new CompoundTag() : data.copyTag();
        itemStack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(compoundTag));
        return itemStack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).getUnsafe();
    }

    public static CompoundTag getTag(ItemStack itemStack) {
        net.minecraft.world.item.component.CustomData data = itemStack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        return data == null ? null : data.copyTag();
    }

    public static void setTag(ItemStack itemStack, CompoundTag tag) {
        if (tag == null) {
            itemStack.remove(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        } else {
            itemStack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
        }
    }

    public static boolean checkItemEquality(ItemStack itemStack, ItemStack itemStack2) {
        return StackUtil.isEmpty(itemStack) && StackUtil.isEmpty(itemStack2) || !StackUtil.isEmpty(itemStack) && !StackUtil.isEmpty(itemStack2) && itemStack.getItem() == itemStack2.getItem() && StackUtil.checkNbtEquality(itemStack, itemStack2);
    }

    public static boolean checkItemEquality(ItemStack itemStack, Item item) {
        return StackUtil.isEmpty(itemStack) && item == null || !StackUtil.isEmpty(itemStack) && item != null && itemStack.getItem() == item;
    }

    public static boolean checkItemEqualityStrict(ItemStack itemStack, ItemStack itemStack2) {
        return StackUtil.isEmpty(itemStack) && StackUtil.isEmpty(itemStack2) || !StackUtil.isEmpty(itemStack) && !StackUtil.isEmpty(itemStack2) && ItemStack.isSameItemSameComponents(itemStack, itemStack2) && StackUtil.checkNbtEqualityStrict(itemStack, itemStack2);
    }

    private static boolean checkNbtEquality(ItemStack itemStack, ItemStack itemStack2) {
        return StackUtil.checkNbtEquality(StackUtil.getTag(itemStack), StackUtil.getTag(itemStack2));
    }

    public static boolean checkNbtEquality(CompoundTag compoundTag, CompoundTag compoundTag2) {
        if (compoundTag == compoundTag2) {
            return true;
        }
        Set<String> set = compoundTag != null ? compoundTag.getAllKeys() : Collections.emptySet();
        Set<String> set2 = compoundTag2 != null ? compoundTag2.getAllKeys() : Collections.emptySet();
        HashSet<String> hashSet = new HashSet<String>(Math.max(set.size(), set2.size()));
        for (String string : set) {
            if (ignoredNbtKeys.contains(string)) continue;
            if (!set2.contains(string)) {
                return false;
            }
            hashSet.add(string);
        }
        for (String string : set2) {
            if (ignoredNbtKeys.contains(string)) continue;
            if (!set.contains(string)) {
                return false;
            }
            hashSet.add(string);
        }
        for (String string : hashSet) {
            if (compoundTag.get(string).equals(compoundTag2.get(string))) continue;
            return false;
        }
        return true;
    }

    public static boolean checkNbtEqualityStrict(ItemStack itemStack, ItemStack itemStack2) {
        CompoundTag compoundTag;
        CompoundTag compoundTag2 = StackUtil.getTag(itemStack);
        if (compoundTag2 == (compoundTag = StackUtil.getTag(itemStack2))) {
            return true;
        }
        return compoundTag2 != null && compoundTag != null && compoundTag2.equals((Object)compoundTag);
    }

    public static Predicate<ItemStack> sameStack(final ItemStack itemStack) {
        if (StackUtil.isEmpty(itemStack)) {
            throw new IllegalArgumentException("empty stack");
        }
        return new Predicate<ItemStack>(){

            @Override
            public boolean test(ItemStack itemStack2) {
                return StackUtil.checkItemEquality(itemStack2, itemStack);
            }

            public String toString() {
                return "stack == " + itemStack;
            }
        };
    }

    public static Predicate<ItemStack> sameItem(final Item item) {
        if (item == null) {
            throw new NullPointerException("null item");
        }
        return new Predicate<ItemStack>(){

            @Override
            public boolean test(ItemStack itemStack) {
                return itemStack.getItem() == item;
            }

            public String toString() {
                return "item == " + item;
            }
        };
    }

    public static Predicate<ItemStack> sameItem(ItemLike itemLike) {
        if (itemLike == null) {
            throw new NullPointerException("null block");
        }
        Item item = itemLike.asItem();
        if (item == null || item == Items.AIR && itemLike != Blocks.AIR) {
            throw new IllegalArgumentException("block " + itemLike + " doesn't have an associated item");
        }
        return StackUtil.sameItem(item);
    }

    public static Predicate<ItemStack> recipeInput(final IRecipeInput iRecipeInput) {
        return new Predicate<ItemStack>(){

            @Override
            public boolean test(ItemStack itemStack) {
                return iRecipeInput.matches(itemStack);
            }

            public String toString() {
                return iRecipeInput.toString();
            }
        };
    }

    public static boolean consume(Player player, InteractionHand interactionHand, Predicate<ItemStack> predicate, int n) {
        return StackUtil.consume0(player, interactionHand, predicate, n, false) != emptyStack;
    }

    public static ItemStack consumeAndGet(Player player, Predicate<ItemStack> predicate, int n) {
        return StackUtil.consumeAndGet(player, InteractionHand.MAIN_HAND, predicate, n);
    }

    public static ItemStack consumeAndGet(Player player, InteractionHand interactionHand, Predicate<ItemStack> predicate, int n) {
        return StackUtil.consume0(player, interactionHand, predicate, n, true);
    }

    public static void consumeOrError(Player player, InteractionHand interactionHand, int n) {
        StackUtil.consumeOrError(player, interactionHand, anyStack, n);
    }

    public static void consumeOrError(Player player, InteractionHand interactionHand, Predicate<ItemStack> predicate, int n) {
        if (!StackUtil.consume(player, interactionHand, predicate, n)) {
            throw new IllegalStateException("consume failed");
        }
    }

    private static ItemStack consume0(Player player, InteractionHand interactionHand, Predicate<ItemStack> predicate, int n, boolean bl) {
        ItemStack itemStack;
        if (n <= 0) {
            throw new IllegalArgumentException("negative/zero amount");
        }
        ItemStack itemStack2 = StackUtil.get(player, interactionHand);
        if (StackUtil.isEmpty(itemStack2)) {
            return emptyStack;
        }
        if (!predicate.test(itemStack2)) {
            return emptyStack;
        }
        if (player.getAbilities().instabuild) {
            return bl ? StackUtil.copyWithSize(itemStack2, n) : itemStack2;
        }
        if (StackUtil.getSize(itemStack2) < n) {
            return emptyStack;
        }
        if (StackUtil.getSize(itemStack2) == n) {
            itemStack = itemStack2;
            StackUtil.clear(player, interactionHand);
        } else {
            itemStack = bl ? StackUtil.copyWithSize(itemStack2, n) : itemStack2;
            StackUtil.set(player, interactionHand, StackUtil.decSize(itemStack2, n));
        }
        return itemStack;
    }

    public static boolean consumeFromPlayerInventory(Player player, Predicate<ItemStack> predicate, int n, boolean bl) {
        NonNullList nonNullList = player.getInventory().items;
        for (int i = 0; i < 2; ++i) {
            int n2 = n;
            for (int j = 0; j < nonNullList.size(); ++j) {
                ItemStack itemStack = (ItemStack)nonNullList.get(j);
                if (!predicate.test(itemStack)) continue;
                if (player.getAbilities().instabuild) {
                    return true;
                }
                int n3 = Math.min(StackUtil.getSize(itemStack), n2);
                n2 -= n3;
                if (i == 1) {
                    nonNullList.set(j, (Object)StackUtil.decSize(itemStack, n3));
                }
                if (n2 <= 0) break;
            }
            if (n2 > 0) {
                if (i == 1) {
                    IC2.log.warn(LogCategory.General, "Inconsistent inventory transaction for player %s, request %s: %d missing", player, predicate, n2);
                }
                return false;
            }
            if (!bl) continue;
            return true;
        }
        return true;
    }

    public static boolean damage(Player player, InteractionHand interactionHand, Predicate<ItemStack> predicate, int n) {
        return StackUtil.damage0(player, interactionHand, predicate, n, false) != emptyStack;
    }

    public static void damageOrError(Player player, InteractionHand interactionHand, int n) {
        StackUtil.damageOrError(player, interactionHand, anyStack, n);
    }

    public static void damageOrError(Player player, InteractionHand interactionHand, Predicate<ItemStack> predicate, int n) {
        if (!StackUtil.damage(player, interactionHand, predicate, n)) {
            throw new IllegalStateException("damage failed");
        }
    }

    private static ItemStack damage0(Player player, InteractionHand interactionHand, Predicate<ItemStack> predicate, int n, boolean bl) {
        ItemStack itemStack;
        if (n <= 0) {
            throw new IllegalArgumentException("negative/zero amount");
        }
        ItemStack itemStack2 = StackUtil.get(player, interactionHand);
        if (StackUtil.isEmpty(itemStack2)) {
            return emptyStack;
        }
        int n2 = itemStack2.getMaxDamage();
        if (n2 <= 0) {
            return emptyStack;
        }
        if (!predicate.test(itemStack2)) {
            return emptyStack;
        }
        if (player.getAbilities().instabuild || !itemStack2.isDamageableItem()) {
            return bl ? StackUtil.copy(itemStack2) : itemStack2;
        }
        itemStack2.hurtAndBreak(n, player, interactionHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        if (StackUtil.isEmpty(itemStack2)) {
            itemStack = itemStack2;
            StackUtil.clear(player, interactionHand);
        } else {
            itemStack = bl ? StackUtil.copy(itemStack2) : itemStack2;
            StackUtil.set(player, interactionHand, itemStack2);
        }
        return itemStack;
    }

    public static ItemStack get(Player player, InteractionHand interactionHand) {
        return player.getItemInHand(interactionHand);
    }

    public static void set(Player player, InteractionHand interactionHand, ItemStack itemStack) {
        if (StackUtil.isEmpty(itemStack)) {
            itemStack = emptyStack;
        }
        Inventory inventory = player.getInventory();
        if (interactionHand == InteractionHand.MAIN_HAND) {
            inventory.items.set(inventory.selected, itemStack);
        } else if (interactionHand == InteractionHand.OFF_HAND) {
            inventory.offhand.set(0, itemStack);
        } else {
            throw new IllegalArgumentException("invalid hand: " + interactionHand);
        }
    }

    public static void clear(Player player, InteractionHand interactionHand) {
        StackUtil.set(player, interactionHand, emptyStack);
    }

    public static void clearEmpty(Player player, InteractionHand interactionHand) {
        if (StackUtil.isEmpty(player, interactionHand)) {
            StackUtil.clear(player, interactionHand);
        }
    }

    public static boolean storeInventoryItem(ItemStack itemStack, Player player, boolean bl) {
        if (bl) {
            int n = StackUtil.getSize(itemStack);
            int n2 = Math.min(player.getInventory().getMaxStackSize(), itemStack.getMaxStackSize());
            for (int i = 0; i < player.getInventory().items.size() && n > 0; ++i) {
                ItemStack itemStack2 = (ItemStack)player.getInventory().items.get(i);
                if (StackUtil.isEmpty(itemStack2)) {
                    n -= n2;
                    continue;
                }
                if (!StackUtil.checkItemEqualityStrict(itemStack, itemStack2) || StackUtil.getSize(itemStack2) >= n2) continue;
                n -= n2 - StackUtil.getSize(itemStack2);
            }
            return n <= 0;
        }
        return player.getInventory().add(itemStack);
    }

    public static void dropAsEntity(Level level, BlockPos blockPos, ItemStack itemStack) {
        if (StackUtil.isEmpty(itemStack)) {
            return;
        }
        double d = 0.7;
        double d2 = (double)level.random.nextFloat() * d + (1.0 - d) * 0.5;
        double d3 = (double)level.random.nextFloat() * d + (1.0 - d) * 0.5;
        double d4 = (double)level.random.nextFloat() * d + (1.0 - d) * 0.5;
        ItemEntity itemEntity = new ItemEntity(level, (double)blockPos.getX() + d2, (double)blockPos.getY() + d3, (double)blockPos.getZ() + d4, itemStack.copy());
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity((Entity)itemEntity);
    }

    public static ItemStack setImmutableSize(ItemStack itemStack, int n) {
        if (StackUtil.getSize(itemStack) != n) {
            itemStack = StackUtil.copyWithSize(itemStack, n);
        }
        return itemStack;
    }

    public static int fetch(BlockEntity blockEntity, ItemStack itemStack, boolean bl) {
        return ENV.fetch(blockEntity, itemStack, bl);
    }

    public static int distribute(BlockEntity blockEntity, ItemStack itemStack, boolean bl) {
        return ENV.distribute(blockEntity, itemStack, bl);
    }

    public static void distributeDrops(BlockEntity blockEntity, List<ItemStack> list) {
        java.util.ListIterator<ItemStack> iterator = list.listIterator();
        while (iterator.hasNext()) {
            ItemStack itemStack = iterator.next();
            int n = StackUtil.distribute(blockEntity, itemStack, false);
            if (n == StackUtil.getSize(itemStack)) {
                iterator.remove();
                continue;
            }
            iterator.set(StackUtil.decSize(itemStack, n));
        }
        for (ItemStack itemStack : list) {
            StackUtil.dropAsEntity(blockEntity.getLevel(), blockEntity.getBlockPos(), itemStack);
        }
        list.clear();
    }

    public static boolean placeBlock(ItemStack itemStack, Level level, BlockPos blockPos) {
        if (StackUtil.isEmpty(itemStack)) {
            return false;
        }
        Item item = itemStack.getItem();
        if (item instanceof BlockItem) {
            int n = StackUtil.getSize(itemStack);
            Player player = Ic2Player.get(level);
            InteractionHand interactionHand = InteractionHand.MAIN_HAND;
            ItemStack itemStack2 = player.getItemInHand(interactionHand);
            player.setItemInHand(interactionHand, itemStack);
            InteractionResult interactionResult = item.useOn(new UseOnContext(player, interactionHand, new BlockHitResult(Vec3.atLowerCornerOf((Vec3i)blockPos).add(0.5, 1.0, 0.5), Direction.DOWN, blockPos, false)));
            player.setItemInHand(interactionHand, itemStack2);
            itemStack = StackUtil.setSize(itemStack, n);
            return interactionResult == InteractionResult.SUCCESS || interactionResult == InteractionResult.CONSUME;
        }
        return false;
    }

    public static int putInInventory(BlockEntity blockEntity, Direction direction, ItemStack itemStack, boolean bl) {
        return StackUtil.putInInventory(blockEntity, direction, itemStack, null, bl);
    }

    public static int putInInventory(BlockEntity blockEntity, Direction direction, ItemStack itemStack, GameProfile gameProfile, boolean bl) {
        return ENV.deposit(blockEntity, direction, itemStack, gameProfile, bl);
    }

    public static ItemStack getPickStack(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        ItemStack itemStack = blockState.getBlock().getCloneItemStack(level, blockPos, blockState);
        if (StackUtil.isEmpty(itemStack)) {
            return emptyStack;
        }
        return itemStack;
    }

    public static List<ItemStack> getDrops(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, int n) {
        return StackUtil.getDrops(blockGetter, blockPos, blockState, blockState.getBlock(), n);
    }

    public static List<ItemStack> getDrops(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Block block, int n) {
        return StackUtil.getDrops(blockGetter, blockPos, blockState, null, n, false);
    }

    public static List<ItemStack> getDrops(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Player player, int n, boolean bl) {
        if (blockState.isAir()) {
            return Collections.emptyList();
        }
        // 第三十六轮修复：Block.getDrops 需要 ServerLevel（掉落表解析要动态注册表与随机源）。
        // 旧代码直接 (ServerLevel)blockGetter 强转，调用方只要传非 ServerLevel（ItemScanner 曾传
        // PathNavigationRegion）就 ClassCastException 崩服（logs/2026-10-02-1.log:1085）。
        // 改为显式检查：非 ServerLevel 返回空列表，而不是把服务器砸掉。
        if (!(blockGetter instanceof ServerLevel)) {
            IC2.log.warn(LogCategory.General, "getDrops called with a non-ServerLevel %s at %s; returning no drops", blockGetter.getClass().getName(), blockPos);
            return Collections.emptyList();
        }
        ItemStack itemStack = new ItemStack((ItemLike)Items.DIAMOND_PICKAXE);
        if (bl) {
            itemStack.enchant(IC2.getRegistryAccess().holderOrThrow(Enchantments.SILK_TOUCH), n);
        } else if (n > 0) {
            itemStack.enchant(IC2.getRegistryAccess().holderOrThrow(Enchantments.FORTUNE), n);
        }
        return Block.getDrops((BlockState)blockState, (ServerLevel)((ServerLevel)blockGetter), (BlockPos)blockPos, (BlockEntity)blockGetter.getBlockEntity(blockPos), (Entity)player, (ItemStack)itemStack);
    }

    public static IntSet getSlotsFromInv(Container container) {
        IntOpenHashSet intOpenHashSet = new IntOpenHashSet();
        for (int i = 0; i < container.getContainerSize(); ++i) {
            intOpenHashSet.add(i);
        }
        return intOpenHashSet;
    }

    public static Tuple.T2<List<ItemStack>, ? extends IntCollection> balanceStacks(Container container) {
        return StackUtil.balanceStacks(container, Collections.emptySet());
    }

    public static Tuple.T2<List<ItemStack>, ? extends IntCollection> balanceStacks(Container container, ItemStack itemStack) {
        return StackUtil.balanceStacks(container, Collections.singleton(itemStack));
    }

    public static Tuple.T2<List<ItemStack>, ? extends IntCollection> balanceStacks(final Container container, Collection<ItemStack> collection) {
        return StackUtil.balanceStacks(container, new Predicate<Tuple.T2<ItemStack, Integer>>(){

            @Override
            public boolean test(Tuple.T2<ItemStack, Integer> t2) {
                return !StackUtil.isEmpty(container.getItem(((Integer)t2.b).intValue()));
            }
        }, StackUtil.getSlotsFromInv(container), collection);
    }

    public static Tuple.T2<List<ItemStack>, ? extends IntCollection> balanceStacks(Container container, Predicate<Tuple.T2<ItemStack, Integer>> predicate) {
        return StackUtil.balanceStacks(container, predicate, StackUtil.getSlotsFromInv(container), Collections.emptySet());
    }

    public static Tuple.T2<List<ItemStack>, ? extends IntCollection> balanceStacks(Container container, Predicate<Tuple.T2<ItemStack, Integer>> predicate, IntSet intSet, Collection<ItemStack> collection) {
        int n;
        LinkedList<ItemStack> linkedList = new LinkedList<ItemStack>(collection);
        IntOpenHashSet intOpenHashSet = new IntOpenHashSet((IntCollection)intSet);
        ArrayList<ItemStack> arrayList = new ArrayList<ItemStack>();
        for (int i = 0; i < container.getContainerSize(); ++i) {
            int n2;
            ItemStack itemStack;
            if (!intOpenHashSet.contains(i) || StackUtil.isEmpty(itemStack = container.getItem(i))) continue;
            n = 0;
            ListIterator listIterator = linkedList.listIterator();
            while (listIterator.hasNext()) {
                ItemStack itemStack2 = (ItemStack)listIterator.next();
                if (!StackUtil.checkItemEqualityStrict(itemStack2, itemStack)) continue;
                listIterator.remove();
                n += StackUtil.getSize(itemStack2);
            }
            for (n = StackUtil.distributeStackToSlots(container, itemStack, (IntSet)intOpenHashSet, predicate, n); n > 0; n -= n2) {
                n2 = Math.min(itemStack.getMaxStackSize(), n);
                arrayList.add(StackUtil.copyWithSize(itemStack, n2));
            }
        }
        for (ItemStack itemStack : linkedList) {
            n = StackUtil.distributeStackToSlots(container, itemStack, (IntSet)intOpenHashSet, predicate, StackUtil.getSize(itemStack));
            if (n <= 0) continue;
            arrayList.add(StackUtil.copyWithSize(itemStack, n));
        }
        intSet.removeAll((IntCollection)intOpenHashSet);
        return new Tuple.T2(arrayList, intSet);
    }

    private static int distributeStackToSlots(Container container, ItemStack itemStack, IntSet intSet, Predicate<Tuple.T2<ItemStack, Integer>> predicate, int n) {
        int n2;
        ItemStack itemStack2;
        int n3;
        IntArrayList intArrayList = new IntArrayList();
        IntIterator intIterator = intSet.iterator();
        while (intIterator.hasNext()) {
            n3 = intIterator.nextInt();
            itemStack2 = container.getItem(n3);
            if (!StackUtil.checkItemEqualityStrict(itemStack, itemStack2) && !StackUtil.isEmpty(itemStack2) || !predicate.test(new Tuple.T2<ItemStack, Integer>(itemStack, n3))) continue;
            n += StackUtil.getSize(itemStack2);
            intArrayList.add(n3);
            intIterator.remove();
        }
        intArrayList.sort(Comparator.naturalOrder());
        int n4 = Math.min(itemStack.getMaxStackSize(), container.getMaxStackSize());
        n3 = intArrayList.size();
        IntIterator intIterator2 = intArrayList.iterator();
        while (intIterator2.hasNext() && n > 0) {
            n2 = intIterator2.nextInt();
            int n5 = n / n3;
            if (n % n3 > 0) {
                ++n5;
            }
            n5 = Math.min(n5, n4);
            container.setItem(n2, StackUtil.copyWithSize(itemStack, n5));
            n -= n5;
            --n3;
            intIterator2.remove();
        }
        if (!intArrayList.isEmpty()) {
            assert (n <= 0);
            IntIterator intIterator3 = intArrayList.iterator();
            while (intIterator3.hasNext()) {
                n2 = intIterator3.nextInt();
                container.setItem(n2, emptyStack);
            }
        }
        assert (n <= 0 || n3 == 0);
        return n;
    }

    private static /* synthetic */ boolean lambda$static$0(ItemStack itemStack) {
        return true;
    }
}

