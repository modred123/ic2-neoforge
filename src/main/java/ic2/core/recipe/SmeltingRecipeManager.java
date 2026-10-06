/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.Container
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.item.crafting.SmeltingRecipe
 */
package ic2.core.recipe;

import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.core.IC2;
import ic2.core.util.StackUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;

public class SmeltingRecipeManager
implements IMachineRecipeManager<ItemStack, ItemStack, ItemStack> {
    @Override
    public MachineRecipeResult<ItemStack, ItemStack, ItemStack> apply(ItemStack itemStack, boolean bl) {
        SmeltingRecipe smeltingRecipe = IC2.sideProxy.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new net.minecraft.world.item.crafting.SingleRecipeInput(itemStack), null).map(net.minecraft.world.item.crafting.RecipeHolder::value).orElse(null);
        if (smeltingRecipe == null) {
            return null;
        }
        ItemStack itemStack2 = smeltingRecipe.getResultItem(IC2.getRegistryAccess());
        if (StackUtil.isEmpty(itemStack2)) {
            return null;
        }
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putFloat("experience", smeltingRecipe.getExperience() * (float)StackUtil.getSize(itemStack2));
        return new MachineRecipe<ItemStack, ItemStack>(itemStack, itemStack2, compoundTag).getResult(StackUtil.copyShrunk(itemStack, 1));
    }

    @Override
    public Iterable<? extends MachineRecipe<ItemStack, ItemStack>> getRecipes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isIterable() {
        return false;
    }

    public static enum SmeltingBridge implements IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack>
    {
        INSTANCE;


        @Override
        public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> apply(ItemStack itemStack, boolean bl) {
            MachineRecipeResult<ItemStack, ItemStack, ItemStack> machineRecipeResult = Recipes.furnace.apply(itemStack, bl);
            if (machineRecipeResult == null) {
                return null;
            }
            MachineRecipe<ItemStack, ItemStack> machineRecipe = machineRecipeResult.getRecipe();
            IRecipeInput iRecipeInput = Recipes.inputFactory.forStack(machineRecipe.getInput());
            List<ItemStack> list = Collections.singletonList(machineRecipe.getOutput());
            CompoundTag compoundTag = machineRecipe.getMetaData();
            return new MachineRecipe<IRecipeInput, Collection<ItemStack>>(iRecipeInput, list, compoundTag).getResult(machineRecipeResult.getAdjustedInput());
        }

        @Override
        public Iterable<? extends MachineRecipe<IRecipeInput, Collection<ItemStack>>> getRecipes() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isIterable() {
            return false;
        }
    }
}

