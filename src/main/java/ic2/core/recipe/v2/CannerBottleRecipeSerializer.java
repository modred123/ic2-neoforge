/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.GsonHelper
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.RecipeSerializer
 */
package ic2.core.recipe.v2;

import com.google.gson.JsonObject;
import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.core.recipe.v2.RecipeHolder;
import ic2.core.recipe.v2.RecipeIo;
import ic2.core.ref.Ic2RecipeTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CannerBottleRecipeSerializer
implements RecipeSerializer<RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack>> {
    public com.mojang.serialization.MapCodec<RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack>> codec() {
        return ic2.core.ref.Ic2RecipeSerializers.customJsonCodec(this::read);
    }

    public net.minecraft.network.codec.StreamCodec<RegistryFriendlyByteBuf, RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack>> streamCodec() {
        return net.minecraft.network.codec.StreamCodec.of(this::write, buf -> read(ResourceLocation.fromNamespaceAndPath("ic2", "recipe"), buf));
    }

    public RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> read(ResourceLocation resourceLocation, JsonObject jsonObject) {
        IRecipeInput iRecipeInput = RecipeIo.parseInput(jsonObject.get("container_ingredient"));
        IRecipeInput iRecipeInput2 = RecipeIo.parseInput(jsonObject.get("fill_ingredient"));
        ItemStack itemStack = RecipeIo.parseOutput(GsonHelper.getAsJsonObject((JsonObject)jsonObject, (String)"result"));
        return new RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack>(new MachineRecipe<ICannerBottleRecipeManager.Input, ItemStack>(new ICannerBottleRecipeManager.Input(iRecipeInput, iRecipeInput2), itemStack), resourceLocation, this, Ic2RecipeTypes.CANNER_BOTTLE);
    }

    public RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> read(ResourceLocation resourceLocation, RegistryFriendlyByteBuf friendlyByteBuf) {
        IRecipeInput iRecipeInput = RecipeIo.readInput(friendlyByteBuf);
        IRecipeInput iRecipeInput2 = RecipeIo.readInput(friendlyByteBuf);
        ItemStack itemStack = ItemStack.STREAM_CODEC.decode(friendlyByteBuf);
        return new RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack>(new MachineRecipe<ICannerBottleRecipeManager.Input, ItemStack>(new ICannerBottleRecipeManager.Input(iRecipeInput, iRecipeInput2), itemStack), resourceLocation, this, Ic2RecipeTypes.CANNER_BOTTLE);
    }

    public void write(RegistryFriendlyByteBuf friendlyByteBuf, RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> recipeHolder) {
        RecipeIo.writeInput(friendlyByteBuf, recipeHolder.recipe().getInput().container);
        RecipeIo.writeInput(friendlyByteBuf, recipeHolder.recipe().getInput().fill);
        ItemStack.STREAM_CODEC.encode(friendlyByteBuf, recipeHolder.recipe().getOutput());
    }
}

