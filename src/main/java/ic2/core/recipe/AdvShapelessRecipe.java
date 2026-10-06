/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  net.minecraft.core.NonNullList
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.GsonHelper
 *  net.minecraft.world.inventory.CraftingContainer
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.CraftingRecipe
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.minecraft.world.level.Level
 */
package ic2.core.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import ic2.api.item.ElectricItem;
import ic2.api.recipe.IRecipeInput;
import ic2.core.IC2;
import ic2.core.item.tool.ItemToolCrafting;
import ic2.core.recipe.AdvRecipe;
import ic2.core.recipe.v2.RecipeIo;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class AdvShapelessRecipe
implements CraftingRecipe {
    public final ItemStack output;
    public final IRecipeInput[] input;
    public final boolean hidden;
    public final boolean consuming;
    private final ResourceLocation id;

    public AdvShapelessRecipe(ResourceLocation resourceLocation, IRecipeInput[] iRecipeInputArray, ItemStack itemStack, boolean bl, boolean bl2) {
        this.id = resourceLocation;
        this.input = iRecipeInputArray;
        this.output = itemStack;
        this.hidden = bl;
        this.consuming = bl2;
    }

    public boolean matches(net.minecraft.world.item.crafting.CraftingInput craftingInput, Level level) {
        return this.craft(craftingInput) != StackUtil.emptyStack;
    }

    public ItemStack craft(net.minecraft.world.item.crafting.CraftingInput craftingInput) {
        int n = craftingInput.size();
        if (n < this.input.length) {
            return StackUtil.emptyStack;
        }
        ArrayList<IRecipeInput> arrayList = new ArrayList<IRecipeInput>(Arrays.asList(this.input));
        double d = 0.0;
        block0: for (int i = 0; i < n; ++i) {
            ItemStack itemStack = craftingInput.getItem(i);
            if (StackUtil.isEmpty(itemStack)) continue;
            for (int j = 0; j < arrayList.size(); ++j) {
                if (!((IRecipeInput)arrayList.get(j)).matches(itemStack)) continue;
                d += ElectricItem.manager.getCharge(StackUtil.copyWithSize(itemStack, 1));
                arrayList.remove(j);
                continue block0;
            }
            return StackUtil.emptyStack;
        }
        if (!arrayList.isEmpty()) {
            return StackUtil.emptyStack;
        }
        ItemStack itemStack = this.output.copy();
        ElectricItem.manager.charge(itemStack, d, Integer.MAX_VALUE, true, false);
        return itemStack;
    }

    public ItemStack assemble(net.minecraft.world.item.crafting.CraftingInput craftingInput, net.minecraft.core.HolderLookup.Provider provider) {
        return this.getResultItem(provider).copy();
    }

    public ItemStack getResultItem(net.minecraft.core.HolderLookup.Provider provider) {
        return this.output;
    }

    public boolean canShow() {
        return AdvRecipe.canShow(this.input, this.output, this.hidden);
    }

    public NonNullList<ItemStack> getRemainder(CraftingContainer craftingContainer) {
        if (this.consuming) {
            return NonNullList.withSize(craftingContainer.getContainerSize(), StackUtil.emptyStack);
        }
        NonNullList nonNullList = NonNullList.withSize(craftingContainer.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < nonNullList.size(); ++i) {
            ItemStack itemStack = craftingContainer.getItem(i);
            ItemStack itemStack2 = IC2.envProxy.getRecipeRemainder(itemStack);
            if (itemStack.getItem() instanceof ItemToolCrafting) {
                itemStack2 = itemStack.copy();
                itemStack2.setDamageValue(itemStack2.getDamageValue() + 1);
                if (itemStack2.getDamageValue() == itemStack2.getMaxDamage()) {
                    itemStack2 = ItemStack.EMPTY;
                }
            }
            if (itemStack2.isEmpty()) continue;
            nonNullList.set(i, (Object)itemStack2);
        }
        return nonNullList;
    }

    public boolean canCraftInDimensions(int n, int n2) {
        return n * n2 >= this.input.length;
    }

    @Override
    public net.minecraft.world.item.crafting.CraftingBookCategory category() {
        return net.minecraft.world.item.crafting.CraftingBookCategory.MISC;
    }

    public NonNullList<Ingredient> getIngredients() {
        NonNullList nonNullList = NonNullList.create();
        if (!this.hidden) {
            for (IRecipeInput iRecipeInput : this.input) {
                nonNullList.add((Object)iRecipeInput.getIngredient());
            }
        }
        return nonNullList;
    }

    public boolean isSpecial() {
        return this.hidden;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public RecipeSerializer<?> getSerializer() {
        return Ic2RecipeSerializers.SHAPELESS;
    }

    public static class Serializer
    implements RecipeSerializer<AdvShapelessRecipe> {
        public com.mojang.serialization.MapCodec<AdvShapelessRecipe> codec() {
            return ic2.core.ref.Ic2RecipeSerializers.customJsonCodec(this::read);
        }

        public net.minecraft.network.codec.StreamCodec<RegistryFriendlyByteBuf, AdvShapelessRecipe> streamCodec() {
            return net.minecraft.network.codec.StreamCodec.of(this::write, buf -> read(ResourceLocation.fromNamespaceAndPath("ic2", "recipe"), buf));
        }

        public AdvShapelessRecipe read(ResourceLocation resourceLocation, JsonObject jsonObject) {
            IRecipeInput[] iRecipeInputArray = Serializer.getIngredients(GsonHelper.getAsJsonArray((JsonObject)jsonObject, (String)"ingredients"));
            if (iRecipeInputArray.length == 0) {
                throw new JsonParseException("No ingredients for IC2 shapeless recipe");
            }
            if (iRecipeInputArray.length > 9) {
                throw new JsonParseException("Too many ingredients for IC2 shapeless recipe");
            }
            ItemStack itemStack = RecipeIo.parseOutput(GsonHelper.getAsJsonObject((JsonObject)jsonObject, (String)"result"));
            boolean bl = GsonHelper.getAsBoolean((JsonObject)jsonObject, (String)"consuming", (boolean)false);
            boolean bl2 = GsonHelper.getAsBoolean((JsonObject)jsonObject, (String)"hidden", (boolean)false);
            return new AdvShapelessRecipe(resourceLocation, iRecipeInputArray, itemStack, bl2, bl);
        }

        private static IRecipeInput[] getIngredients(JsonArray jsonArray) {
            IRecipeInput[] iRecipeInputArray = new IRecipeInput[jsonArray.size()];
            for (int i = 0; i < jsonArray.size(); ++i) {
                iRecipeInputArray[i] = RecipeIo.parseInput(jsonArray.get(i));
            }
            return iRecipeInputArray;
        }

        public AdvShapelessRecipe read(ResourceLocation resourceLocation, RegistryFriendlyByteBuf friendlyByteBuf) {
            IRecipeInput[] iRecipeInputArray = new IRecipeInput[friendlyByteBuf.readVarInt()];
            for (int i = 0; i < iRecipeInputArray.length; ++i) {
                iRecipeInputArray[i] = RecipeIo.readInput(friendlyByteBuf);
            }
            return new AdvShapelessRecipe(resourceLocation, iRecipeInputArray, ItemStack.STREAM_CODEC.decode(friendlyByteBuf), friendlyByteBuf.readBoolean(), friendlyByteBuf.readBoolean());
        }

        public void write(RegistryFriendlyByteBuf friendlyByteBuf, AdvShapelessRecipe advShapelessRecipe) {
            friendlyByteBuf.writeVarInt(advShapelessRecipe.input.length);
            for (IRecipeInput iRecipeInput : advShapelessRecipe.input) {
                RecipeIo.writeInput(friendlyByteBuf, iRecipeInput);
            }
            ItemStack.STREAM_CODEC.encode(friendlyByteBuf, advShapelessRecipe.output);
            friendlyByteBuf.writeBoolean(advShapelessRecipe.hidden);
            friendlyByteBuf.writeBoolean(advShapelessRecipe.consuming);
        }
    }
}

