/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.WorldlyContainer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 */
package ic2.core.block.tileentity;

import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.comp.ComparatorEmitter;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityInventory
extends Ic2TileEntity
implements WorldlyContainer,
IInventorySlotHolder<TileEntityInventory> {
    private final List<InvSlot> invSlots = new ArrayList<InvSlot>();
    protected final ComparatorEmitter comparator = this.addComponent(new ComparatorEmitter(this));

    public TileEntityInventory(BlockEntityType<? extends TileEntityInventory> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        this.comparator.setUpdate((IntSupplier)this::calcRedstoneFromInvSlots);
    }

    @Override
    public void loadAdditional(CompoundTag compoundTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(compoundTag, registries);
        CompoundTag compoundTag2 = compoundTag.getCompound("InvSlots");
        for (InvSlot invSlot : this.invSlots) {
            invSlot.readFromNbt(compoundTag2.getCompound(invSlot.name));
        }
    }

    @Override
    public void saveAdditional(CompoundTag compoundTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(compoundTag, registries);
        CompoundTag compoundTag2 = new CompoundTag();
        for (InvSlot invSlot : this.invSlots) {
            CompoundTag compoundTag3 = new CompoundTag();
            invSlot.writeToNbt(compoundTag3);
            compoundTag2.put(invSlot.name, (Tag)compoundTag3);
        }
        compoundTag.put("InvSlots", (Tag)compoundTag2);
    }

    public int getContainerSize() {
        int n = 0;
        for (InvSlot invSlot : this.invSlots) {
            n += invSlot.size();
        }
        return n;
    }

    public boolean isEmpty() {
        for (InvSlot invSlot : this.invSlots) {
            if (invSlot.isEmpty()) continue;
            return false;
        }
        return true;
    }

    public ItemStack getItem(int n) {
        int n2 = this.locateInvSlot(n);
        if (n2 == -1) {
            return StackUtil.emptyStack;
        }
        return this.getStackAt(n2);
    }

    public ItemStack removeItem(int n, int n2) {
        int n3 = this.locateInvSlot(n);
        if (n3 == -1) {
            return StackUtil.emptyStack;
        }
        ItemStack itemStack = this.getStackAt(n3);
        if (StackUtil.isEmpty(itemStack)) {
            return StackUtil.emptyStack;
        }
        if (n2 >= StackUtil.getSize(itemStack)) {
            this.putStackAt(n3, StackUtil.emptyStack);
            return itemStack;
        }
        if (n2 != 0) {
            if (n2 < 0) {
                int n4 = Math.min(this.getAt(n3).getStackSizeLimit(), itemStack.getMaxStackSize()) - StackUtil.getSize(itemStack);
                n2 = Math.max(n2, -n4);
            }
            this.putStackAt(n3, StackUtil.decSize(itemStack, n2));
        }
        ItemStack itemStack2 = itemStack.copy();
        itemStack2 = StackUtil.setSize(itemStack2, n2);
        return itemStack2;
    }

    public ItemStack removeItemNoUpdate(int n) {
        int n2 = this.locateInvSlot(n);
        if (n2 == -1) {
            return StackUtil.emptyStack;
        }
        ItemStack itemStack = this.getStackAt(n2);
        if (!StackUtil.isEmpty(itemStack)) {
            this.putStackAt(n2, StackUtil.emptyStack);
        }
        return itemStack;
    }

    public void setItem(int n, ItemStack itemStack) {
        int n2 = this.locateInvSlot(n);
        if (n2 == -1) {
            assert (false);
            return;
        }
        if (StackUtil.isEmpty(itemStack)) {
            itemStack = StackUtil.emptyStack;
        }
        this.putStackAt(n2, itemStack);
    }

    public void setChanged() {
        super.setChanged();
        for (InvSlot invSlot : this.invSlots) {
            invSlot.onChanged();
        }
    }

    public int getMaxStackSize() {
        int n = 0;
        for (InvSlot invSlot : this.invSlots) {
            n = Math.max(n, invSlot.getStackSizeLimit());
        }
        return n;
    }

    public boolean stillValid(Player player) {
        return !this.isRemoved() && player.distanceToSqr((double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5, (double)this.worldPosition.getZ() + 0.5) <= 64.0;
    }

    public void startOpen(Player player) {
    }

    public void stopOpen(Player player) {
    }

    public boolean canPlaceItem(int n, ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }
        InvSlot invSlot = this.getInventorySlot(n);
        return invSlot != null && invSlot.canInput() && invSlot.accepts(itemStack);
    }

    public int[] getSlotsForFace(Direction direction) {
        int[] nArray = new int[this.getContainerSize()];
        for (int i = 0; i < nArray.length; ++i) {
            nArray[i] = i;
        }
        return nArray;
    }

    public boolean canPlaceItemThroughFace(int n, ItemStack itemStack, Direction direction) {
        if (StackUtil.isEmpty(itemStack)) {
            return false;
        }
        InvSlot invSlot = this.getInventorySlot(n);
        if (invSlot == null) {
            return false;
        }
        if (!invSlot.canInput() || !invSlot.accepts(itemStack)) {
            return false;
        }
        if (invSlot.preferredSide != InvSlot.InvSide.ANY && invSlot.preferredSide.matches(direction)) {
            return true;
        }
        for (InvSlot invSlot2 : this.invSlots) {
            if (invSlot2 == invSlot || invSlot2.preferredSide == InvSlot.InvSide.ANY || !invSlot2.preferredSide.matches(direction) || !invSlot2.canInput() || !invSlot2.accepts(itemStack)) continue;
            return false;
        }
        return true;
    }

    public boolean canTakeItemThroughFace(int n, ItemStack itemStack, Direction direction) {
        InvSlot invSlot = this.getInventorySlot(n);
        if (invSlot == null || !invSlot.canOutput()) {
            return false;
        }
        boolean bl = invSlot.preferredSide.matches(direction);
        if (invSlot.preferredSide != InvSlot.InvSide.ANY && bl) {
            return true;
        }
        for (InvSlot invSlot2 : this.invSlots) {
            if (invSlot2 == invSlot || invSlot2.preferredSide == InvSlot.InvSide.ANY && bl || !invSlot2.preferredSide.matches(direction) || !invSlot2.canOutput() || invSlot2.isEmpty()) continue;
            return false;
        }
        return true;
    }

    public void clearContent() {
        for (InvSlot invSlot : this.invSlots) {
            invSlot.clear();
        }
    }

    @Override
    public int getBaseIndex(InvSlot invSlot) {
        int n = 0;
        for (InvSlot invSlot2 : this.invSlots) {
            if (invSlot2 == invSlot) {
                return n;
            }
            n += invSlot2.size();
        }
        return -1;
    }

    @Override
    public TileEntityInventory getParent() {
        return this;
    }

    @Override
    public InvSlot getInventorySlot(String string) {
        for (InvSlot invSlot : this.invSlots) {
            if (!invSlot.name.equals(string)) continue;
            return invSlot;
        }
        return null;
    }

    @Override
    public void addInventorySlot(InvSlot invSlot) {
        assert (this.invSlots.stream().noneMatch(invSlot2 -> invSlot2.name.equals(invSlot.name)));
        this.invSlots.add(invSlot);
    }

    private int locateInvSlot(int n) {
        if (n < 0) {
            return -1;
        }
        for (int i = 0; i < this.invSlots.size(); ++i) {
            int n2 = this.invSlots.get(i).size();
            if (n < n2) {
                return i << 16 | n;
            }
            n -= n2;
        }
        return -1;
    }

    private static int getIndex(int n) {
        return n >>> 16;
    }

    private static int getOffset(int n) {
        return n & 0xFFFF;
    }

    private InvSlot getAt(int n) {
        assert (n != -1);
        return this.invSlots.get(TileEntityInventory.getIndex(n));
    }

    private ItemStack getStackAt(int n) {
        return this.getAt(n).get(TileEntityInventory.getOffset(n));
    }

    private void putStackAt(int n, ItemStack itemStack) {
        this.getAt(n).put(TileEntityInventory.getOffset(n), itemStack);
        super.setChanged();
    }

    private InvSlot getInventorySlot(int n) {
        int n2 = this.locateInvSlot(n);
        if (n2 == -1) {
            return null;
        }
        return this.getAt(n2);
    }

    @Override
    protected List<ItemStack> getAuxDrops(int n) {
        ArrayList<ItemStack> arrayList = new ArrayList<ItemStack>(super.getAuxDrops(n));
        for (InvSlot invSlot : this.invSlots) {
            for (ItemStack itemStack : invSlot) {
                if (StackUtil.isEmpty(itemStack)) continue;
                arrayList.add(itemStack);
            }
        }
        return arrayList;
    }

    protected static int calcRedstoneFromInvSlots(InvSlot ... invSlotArray) {
        return TileEntityInventory.calcRedstoneFromInvSlots(Arrays.asList(invSlotArray));
    }

    protected int calcRedstoneFromInvSlots() {
        return TileEntityInventory.calcRedstoneFromInvSlots(this.invSlots);
    }

    protected static int calcRedstoneFromInvSlots(Iterable<InvSlot> iterable) {
        int n = 0;
        int n2 = 0;
        for (InvSlot invSlot : iterable) {
            if (invSlot instanceof InvSlotUpgrade) continue;
            int n3 = invSlot.size();
            int n4 = invSlot.getStackSizeLimit();
            n += n3 * n4;
            for (int i = 0; i < n3; ++i) {
                ItemStack itemStack = invSlot.get(i);
                if (StackUtil.isEmpty(itemStack)) continue;
                n2 += Math.min(n4, itemStack.getCount() * n4 / itemStack.getMaxStackSize());
            }
        }
        if (n2 == 0 || n == 0) {
            return 0;
        }
        return 1 + n2 * 14 / n;
    }

}

