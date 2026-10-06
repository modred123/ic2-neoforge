/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.Container
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.Level
 */
package ic2.core.recipe.v2;

import ic2.api.recipe.MachineRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public record RecipeHolder<I, O>(MachineRecipe<I, O> recipe, ResourceLocation id, RecipeSerializer<?> serializer, RecipeType<?> type) implements Recipe<RecipeInput>
{
    public boolean matches(RecipeInput recipeInput, Level level) {
        throw new UnsupportedOperationException("Not supported for IC2 machine recipes.");
    }

    public ItemStack assemble(RecipeInput recipeInput, HolderLookup.Provider provider) {
        throw new UnsupportedOperationException("Not supported for IC2 machine recipes.");
    }

    public boolean canCraftInDimensions(int n, int n2) {
        throw new UnsupportedOperationException("Not supported for IC2 machine recipes.");
    }

    public ItemStack getResultItem(net.minecraft.core.HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public RecipeSerializer<?> getSerializer() {
        return this.serializer;
    }

    public RecipeType<?> getType() {
        return this.type;
    }
}

