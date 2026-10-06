/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.core.Registry
 *  net.minecraft.world.effect.MobEffectCategory
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.food.FoodProperties
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.RecipeManager
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.ItemLike
 */
package ic2.core.init;

import com.mojang.datafixers.util.Pair;
import ic2.api.recipe.IBasicMachineRecipeManager;
import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.ICannerEnrichRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import ic2.core.block.machine.CannerBottleRecipeManager;
import ic2.core.block.machine.CannerEnrichRecipeManager;
import ic2.core.block.machine.EmptyFluidContainerRecipeManager;
import ic2.core.block.machine.FillFluidContainerRecipeManager;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.recipe.BasicMachineRecipeManager;
import ic2.core.recipe.SmeltingRecipeManager;
import ic2.core.recipe.input.RecipeInputIngredient;
import ic2.core.recipe.v2.RecipeHolder;
import ic2.core.recipe.v2.RecipeManagerGetter;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2RecipeTypes;
import java.util.Collection;
import java.util.function.Function;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;

public class Rezepte {
    static void loadRecipes() {
        Recipes.furnace = new SmeltingRecipeManager();
        Recipes.emptyFluidContainer = new EmptyFluidContainerRecipeManager();
        Recipes.fillFluidContainer = new FillFluidContainerRecipeManager();
        Recipes.macerator = Rezepte.basicRecipe(Ic2RecipeTypes.MACERATOR);
        Recipes.extractor = Rezepte.basicRecipe(Ic2RecipeTypes.EXTRACTOR);
        Recipes.compressor = Rezepte.basicRecipe(Ic2RecipeTypes.COMPRESSOR);
        Recipes.centrifuge = Rezepte.basicRecipe(Ic2RecipeTypes.CENTRIFUGE);
        Recipes.blockcutter = Rezepte.basicRecipe(Ic2RecipeTypes.BLOCK_CUTTER);
        Recipes.blastfurnace = Rezepte.basicRecipe(Ic2RecipeTypes.BLAST_FURNACE);
        Recipes.metalformerExtruding = Rezepte.basicRecipe(Ic2RecipeTypes.METAL_FORMER_EXTRUDING);
        Recipes.metalformerCutting = Rezepte.basicRecipe(Ic2RecipeTypes.METAL_FORMER_CUTTING);
        Recipes.metalformerRolling = Rezepte.basicRecipe(Ic2RecipeTypes.METAL_FORMER_ROLLING);
        Recipes.oreWashing = Rezepte.basicRecipe(Ic2RecipeTypes.ORE_WASHER);
        Recipes.cannerBottle = new RecipeManagerGetter<ICannerBottleRecipeManager>(recipeManager -> createCannerBottleManager(recipeManager));
        Recipes.cannerEnrich = new RecipeManagerGetter<ICannerEnrichRecipeManager>(recipeManager -> createCannerEnrichManager(recipeManager));
    }

    public static void registerRecipes() {
        Rezepte.loadRecipes();
    }

    private static RecipeManagerGetter<IBasicMachineRecipeManager> basicRecipe(RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> recipeType) {
        return new RecipeManagerGetter<IBasicMachineRecipeManager>(recipeManager -> createBasicMachineRecipeManager(recipeType, recipeManager));
    }

    private static /* synthetic */ IBasicMachineRecipeManager createBasicMachineRecipeManager(RecipeType recipeType, RecipeManager recipeManager) {
        BasicMachineRecipeManager basicMachineRecipeManager = new BasicMachineRecipeManager();
        for (net.minecraft.world.item.crafting.RecipeHolder<?> holder : (Iterable<net.minecraft.world.item.crafting.RecipeHolder<?>>)(Iterable)recipeManager.getAllRecipesFor((RecipeType<net.minecraft.world.item.crafting.Recipe<net.minecraft.world.item.crafting.RecipeInput>>)(Object)recipeType)) {
            RecipeHolder recipeHolder = (RecipeHolder)holder.value();
            basicMachineRecipeManager.addRecipe((IRecipeInput)recipeHolder.recipe().getInput(), (Collection)recipeHolder.recipe().getOutput(), recipeHolder.recipe().getMetaData(), false);
        }
        return basicMachineRecipeManager;
    }

    private static /* synthetic */ ICannerEnrichRecipeManager createCannerEnrichManager(RecipeManager recipeManager) {
        CannerEnrichRecipeManager cannerEnrichRecipeManager = new CannerEnrichRecipeManager();
        for (net.minecraft.world.item.crafting.RecipeHolder<?> holder : (Iterable<net.minecraft.world.item.crafting.RecipeHolder<?>>)(Iterable)recipeManager.getAllRecipesFor((RecipeType<net.minecraft.world.item.crafting.Recipe<net.minecraft.world.item.crafting.RecipeInput>>)(Object)Ic2RecipeTypes.CANNER_ENRICH)) {
            RecipeHolder recipeHolder = (RecipeHolder)holder.value();
            cannerEnrichRecipeManager.addRecipe((ICannerEnrichRecipeManager.Input)recipeHolder.recipe().getInput(), (Ic2FluidStack)recipeHolder.recipe().getOutput(), null, false);
        }
        return cannerEnrichRecipeManager;
    }

    private static /* synthetic */ ICannerBottleRecipeManager createCannerBottleManager(RecipeManager recipeManager) {
        CannerBottleRecipeManager cannerBottleRecipeManager = new CannerBottleRecipeManager();
        for (net.minecraft.world.item.crafting.RecipeHolder<?> holder : (Iterable<net.minecraft.world.item.crafting.RecipeHolder<?>>)(Iterable)recipeManager.getAllRecipesFor((RecipeType<net.minecraft.world.item.crafting.Recipe<net.minecraft.world.item.crafting.RecipeInput>>)(Object)Ic2RecipeTypes.CANNER_BOTTLE)) {
            RecipeHolder recipeHolder = (RecipeHolder)holder.value();
            cannerBottleRecipeManager.addRecipe((ICannerBottleRecipeManager.Input)recipeHolder.recipe().getInput(), (ItemStack)recipeHolder.recipe().getOutput(), null, false);
        }
        Ingredient ingredient = Ingredient.of((ItemLike[])new ItemLike[]{Ic2Items.TIN_CAN});
        for (Item item : BuiltInRegistries.ITEM) {
            int n;
            FoodProperties foodProperties = item.components().get(net.minecraft.core.component.DataComponents.FOOD);
            if (foodProperties == null || (n = Math.min(64, foodProperties.nutrition())) < 1) continue;
            boolean bl = false;
            for (net.minecraft.world.food.FoodProperties.PossibleEffect possibleEffect : foodProperties.effects()) {
                if (possibleEffect.effect().getEffect().value().getCategory() != MobEffectCategory.HARMFUL) continue;
                bl = true;
                break;
            }
            if (bl && (n /= 2) < 1) continue;
            cannerBottleRecipeManager.addRecipe(new RecipeInputIngredient(ingredient, n), new RecipeInputIngredient(Ingredient.of((ItemLike[])new ItemLike[]{item}), 1), new ItemStack((ItemLike)Ic2Items.FILLED_TIN_CAN, n));
        }
        return cannerBottleRecipeManager;
    }
}

