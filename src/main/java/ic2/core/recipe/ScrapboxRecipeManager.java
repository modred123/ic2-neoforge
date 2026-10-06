/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 */
package ic2.core.recipe;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.IScrapboxManager;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;
import ic2.core.IC2;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class ScrapboxRecipeManager
implements IScrapboxManager {
    private final List<Drop> drops = new ArrayList<Drop>();

    public static void setup() {
        if (Recipes.scrapboxDrops != null) {
            throw new IllegalStateException("already initialized");
        }
        Recipes.scrapboxDrops = new ScrapboxRecipeManager();
    }

    public static void load() {
        ((ScrapboxRecipeManager)Recipes.scrapboxDrops).addBuiltinDrops();
    }

    private ScrapboxRecipeManager() {
    }

    public boolean addRecipe(IRecipeInput iRecipeInput, Collection<ItemStack> collection, CompoundTag compoundTag, boolean bl) {
        if (!iRecipeInput.matches(new ItemStack((ItemLike)Ic2Items.SCRAP_BOX))) {
            throw new IllegalArgumentException("currently only scrap boxes are supported");
        }
        if (compoundTag == null || !compoundTag.contains("weight")) {
            throw new IllegalArgumentException("no weight metadata");
        }
        if (collection.size() != 1) {
            throw new IllegalArgumentException("currently only a single drop stack is supported");
        }
        float f = compoundTag.getFloat("weight");
        if (f <= 0.0f || Float.isInfinite(f) || Float.isNaN(f)) {
            throw new IllegalArgumentException("invalid weight");
        }
        this.addDrop(collection.iterator().next(), f);
        return true;
    }

    public boolean addRecipe(IRecipeInput iRecipeInput, CompoundTag compoundTag, boolean bl, ItemStack ... itemStackArray) {
        return this.addRecipe(iRecipeInput, Arrays.asList(itemStackArray), compoundTag, bl);
    }

    @Override
    public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> apply(ItemStack itemStack, boolean bl) {
        if (StackUtil.isEmpty(itemStack) || itemStack.getItem() != Ic2Items.SCRAP_BOX) {
            return null;
        }
        if (this.drops.isEmpty()) {
            return null;
        }
        float f = IC2.random.nextFloat() * Drop.topChance;
        int n = 0;
        int n2 = this.drops.size() - 1;
        while (n < n2) {
            int n3 = n2 + n >>> 1;
            if (f < this.drops.get((int)n3).upperChanceBound) {
                n2 = n3;
                continue;
            }
            n = n3 + 1;
        }
        ItemStack itemStack2 = this.drops.get((int)n).item.copy();
        return new MachineRecipe<IRecipeInput, Collection<ItemStack>>(Recipes.inputFactory.forItem(Ic2Items.SCRAP_BOX), Collections.singletonList(itemStack2)).getResult(StackUtil.copyShrunk(itemStack, 1));
    }

    @Override
    public RecipeOutput getOutputFor(ItemStack itemStack, boolean bl) {
        MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> machineRecipeResult = this.apply(itemStack, false);
        if (machineRecipeResult == null || machineRecipeResult.getOutput().isEmpty()) {
            return null;
        }
        return new RecipeOutput(null, new ArrayList<ItemStack>(machineRecipeResult.getOutput()));
    }

    @Override
    public Iterable<? extends MachineRecipe<IRecipeInput, Collection<ItemStack>>> getRecipes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isIterable() {
        return false;
    }

    public void addDrop(ItemStack itemStack, float f) {
        this.drops.add(new Drop(itemStack, f));
    }

    @Override
    public ItemStack getDrop(ItemStack itemStack, boolean bl) {
        MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> machineRecipeResult = this.apply(itemStack, false);
        if (machineRecipeResult == null || machineRecipeResult.getOutput().isEmpty()) {
            return null;
        }
        if (bl) {
            itemStack.setCount(StackUtil.getSize(machineRecipeResult.getAdjustedInput()));
        }
        return machineRecipeResult.getOutput().iterator().next();
    }

    @Override
    public Map<ItemStack, Float> getDrops() {
        HashMap<ItemStack, Float> hashMap = new HashMap<ItemStack, Float>(this.drops.size());
        for (Drop drop : this.drops) {
            hashMap.put(drop.item, Float.valueOf(drop.originalChance / Drop.topChance));
        }
        return hashMap;
    }

    private void addBuiltinDrops() {
        if (IC2.suddenlyHoes) {
            this.addDrop(Items.WOODEN_HOE, 9001.0f);
        } else {
            this.addDrop(Items.WOODEN_HOE, 5.01f);
        }
        this.addDrop(Blocks.DIRT, 5.0f);
        this.addDrop(Items.STICK, 4.0f);
        this.addDrop(Blocks.GRASS_BLOCK, 3.0f);
        this.addDrop(Blocks.GRAVEL, 3.0f);
        this.addDrop(Blocks.NETHERRACK, 2.0f);
        this.addDrop(Items.ROTTEN_FLESH, 2.0f);
        this.addDrop(Items.APPLE, 1.5f);
        this.addDrop(Items.BREAD, 1.5f);
        this.addDrop(Ic2Items.FILLED_TIN_CAN, 1.5f);
        this.addDrop(Items.WOODEN_SWORD, 1.0f);
        this.addDrop(Items.WOODEN_SHOVEL, 1.0f);
        this.addDrop(Items.WOODEN_PICKAXE, 1.0f);
        this.addDrop(Blocks.SOUL_SAND, 1.0f);
        this.addDrop(Items.OAK_SIGN, 1.0f);
        this.addDrop(Items.LEATHER, 1.0f);
        this.addDrop(Items.FEATHER, 1.0f);
        this.addDrop(Items.BONE, 1.0f);
        this.addDrop(Items.COOKED_PORKCHOP, 0.9f);
        this.addDrop(Items.COOKED_BEEF, 0.9f);
        this.addDrop(Blocks.PUMPKIN, 0.9f);
        this.addDrop(Items.COOKED_CHICKEN, 0.9f);
        this.addDrop(Items.MINECART, 0.01f);
        this.addDrop(Items.REDSTONE, 0.9f);
        this.addDrop(Ic2Items.RUBBER, 0.8f);
        this.addDrop(Items.GLOWSTONE_DUST, 0.8f);
        this.addDrop(Ic2Items.COAL_DUST, 0.8f);
        this.addDrop(Ic2Items.COPPER_DUST, 0.8f);
        this.addDrop(Ic2Items.TIN_DUST, 0.8f);
        this.addDrop(Ic2Items.SINGLE_USE_BATTERY, 0.7f);
        this.addDrop(Ic2Items.IRON_DUST, 0.7f);
        this.addDrop(Ic2Items.GOLD_DUST, 0.7f);
        this.addDrop(Items.SLIME_BALL, 0.6f);
        this.addDrop(Blocks.IRON_ORE, 0.5f);
        this.addDrop(Items.GOLDEN_HELMET, 0.01f);
        this.addDrop(Blocks.GOLD_ORE, 0.5f);
        this.addDrop(Items.CAKE, 0.5f);
        this.addDrop(Items.DIAMOND, 0.1f);
        this.addDrop(Items.EMERALD, 0.05f);
        this.addDrop(Items.ENDER_PEARL, 0.08f);
        this.addDrop(Items.BLAZE_ROD, 0.04f);
        this.addDrop(Items.EGG, 0.8f);
        this.addDrop(Blocks.COPPER_ORE, 0.7f);
        this.addDrop(Ic2Items.TIN_ORE, 0.7f);
    }

    private void addDrop(Block block, float f) {
        this.addDrop(new ItemStack((ItemLike)block), f);
    }

    private void addDrop(Item item, float f) {
        this.addDrop(new ItemStack((ItemLike)item), f);
    }

    private static class Drop {
        final ItemStack item;
        final float originalChance;
        final float upperChanceBound;
        static float topChance;

        Drop(ItemStack itemStack, float f) {
            this.item = itemStack;
            this.originalChance = f;
            this.upperChanceBound = topChance += f;
        }
    }
}

