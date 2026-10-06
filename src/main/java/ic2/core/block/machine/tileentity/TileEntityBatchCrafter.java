/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Predicate
 *  it.unimi.dsi.fastutil.ints.IntCollection
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.NonNullList
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.CraftingContainer
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.CraftingRecipe
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 */
package ic2.core.block.machine.tileentity;

import com.google.common.base.Predicate;
import ic2.api.network.ClientModifiable;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.IUpgradeItem;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.SimpleCraftingInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerBatchCrafter;
import ic2.core.block.machine.tileentity.TileEntityElectricMachine;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.network.GrowingBuffer;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.util.StackUtil;
import ic2.core.util.Tuple;
import it.unimi.dsi.fastutil.ints.IntCollection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityBatchCrafter
extends TileEntityElectricMachine
implements IHasGui,
IUpgradableBlock,
IGuiValueProvider,
INetworkClientTileEntityEventListener {
    private static final Set<UpgradableProperty> UPGRADES = EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
    public static final int defaultTier = 1;
    public static final int defaultEnergyConsume = 2;
    public static final int defaultOperationLength = 40;
    public static final int defaultEnergyStorage = 20000;
    @ClientModifiable
    public final ItemStack[] craftingGrid = new ItemStack[9];
    public final InvSlot[] ingredientsRow = new InvSlot[this.craftingGrid.length];
    public final InvSlotOutput craftingOutput = new InvSlotOutput(this, "output", 1, InvSlot.InvSide.SIDE);
    public final InvSlotOutput containerOutput = new InvSlotOutput(this, "containersOut", this.craftingGrid.length, InvSlot.InvSide.NOTSIDE);
    public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
    protected final CraftingContainer crafting = new SimpleCraftingInventory.ArrayCraftingInventory(this.craftingGrid, 3);
    public final CraftingContainer ingredients = new SimpleCraftingInventory(3, 3){

        @Override
        protected ItemStack get(int n) {
            return TileEntityBatchCrafter.this.ingredientsRow[n].get();
        }

        @Override
        protected void set(int n, ItemStack itemStack) {
            TileEntityBatchCrafter.this.ingredientsRow[n].put(itemStack);
        }
    };
    public final Predicate<Tuple.T2<ItemStack, Integer>> acceptPredicate = new Predicate<Tuple.T2<ItemStack, Integer>>(){

        public boolean apply(Tuple.T2<ItemStack, Integer> t2) {
            return TileEntityBatchCrafter.this.ingredientsRow[(Integer)t2.b].accepts((ItemStack)t2.a);
        }
    };
    protected CraftingRecipe recipe = null;
    protected boolean canCraft = false;
    protected boolean newChange = true;
    protected boolean attemptToBalance = false;
    public ItemStack recipeOutput = StackUtil.emptyStack;
    public int energyConsume;
    public int operationLength;
    public int operationsPerTick;
    protected short progress = 0;
    protected float guiProgress = 0.0f;

    public TileEntityBatchCrafter(BlockPos blockPos, BlockState blockState) {
        this((BlockEntityType<? extends TileEntityBatchCrafter>)Ic2BlockEntities.BATCH_CRAFTER, blockPos, blockState);
    }

    protected TileEntityBatchCrafter(BlockEntityType<? extends TileEntityBatchCrafter> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState, 20000, 1);
        int n = 0;
        while (n < this.ingredientsRow.length) {
            final int n2 = n++;
            this.ingredientsRow[n2] = new InvSlot(this, "ingredient[" + n2 + "]", InvSlot.Access.I, 1){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                public boolean accepts(ItemStack itemStack) {
                    CraftingRecipe craftingRecipe;
                    CraftingRecipe craftingRecipe2 = craftingRecipe = ((TileEntityBatchCrafter)TileEntityBatchCrafter.this).level.isClientSide ? TileEntityBatchCrafter.this.findRecipe() : TileEntityBatchCrafter.this.recipe;
                    if (craftingRecipe == null) {
                        return false;
                    }
                    assert (craftingRecipe.matches(TileEntityBatchCrafter.this.crafting.asCraftInput(), TileEntityBatchCrafter.this.getLevel()));
                    ItemStack itemStack2 = TileEntityBatchCrafter.this.craftingGrid[n2];
                    try {
                        TileEntityBatchCrafter.this.craftingGrid[n2] = itemStack;
                        boolean bl = craftingRecipe.matches(TileEntityBatchCrafter.this.crafting.asCraftInput(), TileEntityBatchCrafter.this.getLevel());
                        return bl;
                    }
                    finally {
                        TileEntityBatchCrafter.this.craftingGrid[n2] = itemStack2;
                    }
                }

                @Override
                public void onChanged() {
                    super.onChanged();
                    TileEntityBatchCrafter.this.ingredientChange(n2);
                }
            };
        }
        this.energyConsume = 2;
        this.operationLength = 40;
        this.operationsPerTick = 1;
        this.comparator.setUpdate(() -> this.computeProgress());
    }

    @Override
    public void loadAdditional(CompoundTag compoundTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(compoundTag, registries);
        this.progress = compoundTag.getShort("progress");
        ListTag listTag = compoundTag.getList("grid", 10);
        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag2 = listTag.getCompound(i);
            this.craftingGrid[compoundTag2.getByte((String)"index")] = ItemStack.parse(this.getLevel().registryAccess(), compoundTag2).orElse(ItemStack.EMPTY);
        }
    }

    @Override
    public void saveAdditional(CompoundTag compoundTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(compoundTag, registries);
        compoundTag.putShort("progress", this.progress);
        ListTag listTag = new ListTag();
        for (byte by = 0; by < this.craftingGrid.length; by = (byte)((byte)(by + 1))) {
            ItemStack itemStack = this.craftingGrid[by];
            if (StackUtil.isEmpty(itemStack)) continue;
            CompoundTag compoundTag2 = new CompoundTag();
            compoundTag2.putByte("index", by);
            itemStack.save(this.getLevel().registryAccess(), compoundTag2);
            listTag.add(compoundTag2);
        }
        compoundTag.put("grid", (Tag)listTag);
    }

    protected CraftingRecipe findRecipe() {
        Level level = this.getLevel();
        return level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, this.crafting.asCraftInput(), level).map(net.minecraft.world.item.crafting.RecipeHolder::value).orElse(null);
    }

    public void matrixChange(int n) {
        if (this.recipe == null || !this.recipe.matches(this.crafting.asCraftInput(), this.getLevel())) {
            this.recipe = this.findRecipe();
        }
        this.recipeOutput = this.recipe != null ? this.recipe.assemble(this.crafting.asCraftInput(), this.getLevel().registryAccess()) : StackUtil.emptyStack;
        this.newChange = true;
    }

    public void ingredientChange(int n) {
        this.newChange = true;
    }

    @Override
    protected void onLoaded() {
        super.onLoaded();
        if (!this.getLevel().isClientSide) {
            this.setOverclockRates();
            this.matrixChange(-1);
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (!this.getLevel().isClientSide) {
            this.setOverclockRates();
            this.attemptToBalance = true;
        }
    }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean bl = false;
        if (this.attemptToBalance) {
            if (!this.ingredients.isEmpty()) {
                bl |= !((IntCollection)StackUtil.balanceStacks((Container)this.ingredients, this.acceptPredicate).b).isEmpty();
            }
            this.attemptToBalance = false;
        }
        if (this.newChange) {
            this.canCraft = this.canCraft();
            this.newChange = false;
        }
        if (this.canCraft && this.craftingOutput.canAdd(this.recipeOutput) && this.energy.useEnergy(this.energyConsume)) {
            this.setActive(true);
            this.progress = (short)(this.progress + 1);
            if (this.progress >= this.operationLength) {
                this.doCrafting();
                bl = true;
                this.newChange = true;
                this.progress = 0;
            }
        } else {
            if (!this.hasRecipe()) {
                this.progress = 0;
            }
            this.setActive(false);
        }
        this.guiProgress = (float)this.progress / (float)this.operationLength;
        if (bl |= this.upgradeSlot.tickNoMark()) {
            super.setChanged();
        }
    }

    public boolean hasRecipe() {
        return this.recipe != null;
    }

    public boolean canCraft() {
        if (!this.hasRecipe()) {
            return false;
        }
        for (int i = 0; i < this.craftingGrid.length; ++i) {
            if (StackUtil.isEmpty(this.craftingGrid[i]) || this.ingredientsRow[i].accepts(this.ingredientsRow[i].get())) continue;
            return false;
        }
        return true;
    }

    protected void doCrafting() {
        for (int i = 0; i < this.operationsPerTick; ++i) {
            List<ItemStack> list = Collections.singletonList(this.recipeOutput);
            for (ItemStack itemStack : this.upgradeSlot) {
                if (itemStack == null || !(itemStack.getItem() instanceof IUpgradeItem)) continue;
                ((IUpgradeItem)itemStack.getItem()).onProcessEnd(itemStack, this, list);
            }
            this.craft();
            if (!this.hasRecipe() || !this.craftingOutput.canAdd(this.recipeOutput)) break;
        }
    }

    protected void craft() {
        ItemStack itemStack;
        int n;
        assert (this.hasRecipe());
        assert (this.craftingOutput.canAdd(this.recipeOutput));
        this.craftingOutput.add(this.recipeOutput);
        NonNullList nonNullList = this.recipe.getRemainingItems(this.ingredients.asCraftInput());
        Level level = this.getLevel();
        for (n = 0; n < this.ingredientsRow.length; ++n) {
            itemStack = this.ingredientsRow[n].get();
            if (!StackUtil.isEmpty(itemStack) && !StackUtil.isEmpty(this.craftingGrid[n])) {
                itemStack = StackUtil.decSize(itemStack);
                this.ingredientsRow[n].put(itemStack);
            }
            if (nonNullList.size() <= n || StackUtil.isEmpty((ItemStack)nonNullList.get(n))) continue;
            ItemStack itemStack2 = (ItemStack)nonNullList.get(n);
            if (StackUtil.isEmpty(itemStack) && this.ingredientsRow[n].accepts(itemStack2)) {
                this.ingredientsRow[n].put(itemStack2);
                continue;
            }
            if (StackUtil.checkItemEqualityStrict(itemStack, itemStack2)) {
                this.ingredientsRow[n].put(StackUtil.incSize(itemStack2, StackUtil.getSize(itemStack)));
                continue;
            }
            if (this.containerOutput.canAdd(itemStack2)) {
                this.containerOutput.add(itemStack2);
                continue;
            }
            StackUtil.dropAsEntity(level, this.worldPosition, itemStack2);
        }
        for (n = this.ingredientsRow.length; n < nonNullList.size(); ++n) {
            itemStack = (ItemStack)nonNullList.get(n);
            if (this.containerOutput.canAdd(itemStack)) {
                this.containerOutput.add(itemStack);
                continue;
            }
            StackUtil.dropAsEntity(level, this.worldPosition, itemStack);
        }
    }

    protected void setOverclockRates() {
        this.upgradeSlot.onChanged();
        double d = (double)this.progress / (double)this.operationLength;
        this.operationsPerTick = this.upgradeSlot.getOperationsPerTick(40);
        this.operationLength = this.upgradeSlot.getOperationLength(40);
        this.energyConsume = this.upgradeSlot.getEnergyDemand(2);
        int n = this.upgradeSlot.getTier(1);
        this.energy.setSinkTier(n);
        this.dischargeSlot.setTier(n);
        this.energy.setCapacity(this.upgradeSlot.getEnergyStorage(20000, 40, 2));
        this.progress = (short)Math.floor(d * (double)this.operationLength + 0.1);
    }

    @Override
    public void onNetworkEvent(Player player, int n) {
        switch (n) {
            case 0: {
                this.matrixChange(-1);
            }
        }
    }

    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return UPGRADES;
    }

    @Override
    public double getEnergy() {
        return this.energy.getEnergy();
    }

    @Override
    public boolean useEnergy(double d) {
        return this.energy.useEnergy(d);
    }

    @Override
    public ContainerBase<?> createServerScreenHandler(int n, Player player) {
        return new ContainerBatchCrafter(n, player.getInventory(), this);
    }

    @Override
    public ContainerBase<?> createClientScreenHandler(int n, Inventory inventory, GrowingBuffer growingBuffer) {
        return new ContainerBatchCrafter(n, inventory, this);
    }

    @Override
    public double getGuiValue(String string) {
        if ("progress".equals(string)) {
            return this.guiProgress;
        }
        throw new IllegalArgumentException("Unexpected value requested: " + string);
    }

    private int computeProgress() {
        return this.progress * 15 / this.operationLength;
    }
}

