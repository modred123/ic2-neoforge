/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonSyntaxException
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  net.minecraft.core.Registry
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtOps
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.GsonHelper
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.material.Fluid
 *  org.jetbrains.annotations.Contract
 *  org.jetbrains.annotations.Nullable
 */
package ic2.core.recipe.v2;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import ic2.core.util.StackUtil;
import ic2.api.recipe.IRecipeInput;
import ic2.core.fluid.FluidHandler;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.recipe.input.RecipeInputFluidContainer;
import ic2.core.recipe.input.RecipeInputIngredient;
import ic2.core.recipe.input.RecipeInputItemStack;
import ic2.core.recipe.input.RecipeInputMultiple;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class RecipeIo {
    public static IRecipeInput parseInput(JsonElement jsonElement) {
        if (jsonElement.isJsonArray()) {
            return RecipeIo.parseMultiple(jsonElement.getAsJsonArray(), 1);
        }
        int n = 1;
        if (jsonElement.isJsonObject()) {
            n = GsonHelper.getAsInt((JsonObject)jsonElement.getAsJsonObject(), (String)"count", (int)1);
        }
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (jsonObject.has("fluid")) {
                return new RecipeInputFluidContainer(RecipeIo.asFluid(jsonObject.get("fluid"), "fluid"), GsonHelper.getAsInt((JsonObject)jsonObject, (String)"amount"));
            }
            if (jsonObject.has("nbt")) {
                Item item = GsonHelper.getAsItem(jsonObject, "item").value();
                ItemStack itemStack = new ItemStack(item, n);
                StackUtil.setTag(itemStack, RecipeIo.asNbt(jsonObject.get("data"), "data"));
                return new RecipeInputItemStack(itemStack);
            }
            if (jsonObject.has("any")) {
                JsonElement jsonElement2 = jsonObject.get("any");
                if (!jsonElement2.isJsonArray()) {
                    throw new JsonSyntaxException("\"any\" IC2 ingredient entry must be an array, was " + GsonHelper.getType((JsonElement)jsonElement2));
                }
                return RecipeIo.parseMultiple(jsonElement2.getAsJsonArray(), n);
            }
        }
        return new RecipeInputIngredient(Ingredient.CODEC_NONEMPTY.parse(JsonOps.INSTANCE, jsonElement).getOrThrow(JsonSyntaxException::new), n);
    }

    private static RecipeInputMultiple parseMultiple(JsonArray jsonArray, int n) {
        IRecipeInput[] iRecipeInputArray = new IRecipeInput[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); ++i) {
            iRecipeInputArray[i] = RecipeIo.parseInput(jsonArray.get(i));
        }
        return new RecipeInputMultiple(n, iRecipeInputArray);
    }

    public static Ic2FluidStack parseFluidStack(JsonObject jsonObject) {
        Fluid fluid = RecipeIo.asFluid(jsonObject.get("fluid"), "fluid");
        int n = GsonHelper.getAsInt((JsonObject)jsonObject, (String)"amount");
        return FluidHandler.createFluidStackMb(fluid, n, null);
    }

    public static Collection<ItemStack> parseOutputs(JsonElement jsonElement, String string) {
        if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            ArrayList<ItemStack> arrayList = new ArrayList<ItemStack>(jsonArray.size());
            for (int i = 0; i < jsonArray.size(); ++i) {
                arrayList.addAll(RecipeIo.parseOutputs(jsonArray.get(i), "array element"));
            }
            return arrayList;
        }
        if (jsonElement.isJsonObject()) {
            return List.of(RecipeIo.parseOutput(jsonElement.getAsJsonObject()));
        }
        throw new JsonSyntaxException("Expected " + string + " to be an array or object for output parsing.");
    }

    public static ItemStack parseOutput(JsonObject jsonObject) {
        Item item = GsonHelper.getAsItem(jsonObject, "item").value();
        int n = GsonHelper.getAsInt((JsonObject)jsonObject, (String)"count", (int)1);
        CompoundTag compoundTag = RecipeIo.getNbt(jsonObject, "nbt", null);
        ItemStack itemStack = new ItemStack(item, n);
        StackUtil.setTag(itemStack, compoundTag);
        return itemStack;
    }

    public static JsonObject resultToJson(ItemStack itemStack) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("item", BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString());
        if (itemStack.getCount() != 1) {
            jsonObject.addProperty("count", (Number)itemStack.getCount());
        }
        return jsonObject;
    }

    public static JsonObject fluidStackToJson(Ic2FluidStack ic2FluidStack) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("fluid", BuiltInRegistries.FLUID.getKey(ic2FluidStack.getFluid()).toString());
        jsonObject.addProperty("amount", (Number)ic2FluidStack.getAmountMb());
        return jsonObject;
    }

    public static void writeInput(RegistryFriendlyByteBuf friendlyByteBuf, IRecipeInput iRecipeInput) {
        if (iRecipeInput instanceof RecipeInputFluidContainer) {
            RecipeInputFluidContainer recipeInputFluidContainer = (RecipeInputFluidContainer)iRecipeInput;
            friendlyByteBuf.writeByte(0);
            friendlyByteBuf.writeVarInt(BuiltInRegistries.FLUID.getId(recipeInputFluidContainer.fluid));
            friendlyByteBuf.writeVarInt(recipeInputFluidContainer.getAmount());
        } else if (iRecipeInput instanceof RecipeInputIngredient) {
            RecipeInputIngredient recipeInputIngredient = (RecipeInputIngredient)iRecipeInput;
            friendlyByteBuf.writeByte(1);
            Ingredient.CONTENTS_STREAM_CODEC.encode(friendlyByteBuf, recipeInputIngredient.getIngredient());
            friendlyByteBuf.writeVarInt(recipeInputIngredient.getAmount());
        } else if (iRecipeInput instanceof RecipeInputItemStack) {
            RecipeInputItemStack recipeInputItemStack = (RecipeInputItemStack)iRecipeInput;
            friendlyByteBuf.writeByte(2);
            ItemStack.STREAM_CODEC.encode(friendlyByteBuf, recipeInputItemStack.input);
        } else if (iRecipeInput instanceof RecipeInputMultiple) {
            RecipeInputMultiple recipeInputMultiple = (RecipeInputMultiple)iRecipeInput;
            friendlyByteBuf.writeByte(3);
            friendlyByteBuf.writeVarInt(recipeInputMultiple.inputs.length);
            for (IRecipeInput iRecipeInput2 : recipeInputMultiple.inputs) {
                RecipeIo.writeInput(friendlyByteBuf, iRecipeInput2);
            }
            friendlyByteBuf.writeVarInt(recipeInputMultiple.getAmount());
        } else {
            throw new IllegalArgumentException("Unkown RecipeInput type: " + iRecipeInput.getClass().getName());
        }
    }

    public static IRecipeInput readInput(RegistryFriendlyByteBuf friendlyByteBuf) {
        return switch (friendlyByteBuf.readByte()) {
            case 0 -> new RecipeInputFluidContainer((Fluid)BuiltInRegistries.FLUID.byId(friendlyByteBuf.readVarInt()), friendlyByteBuf.readVarInt());
            case 1 -> new RecipeInputIngredient(Ingredient.CONTENTS_STREAM_CODEC.decode(friendlyByteBuf), friendlyByteBuf.readVarInt());
            case 2 -> new RecipeInputItemStack(ItemStack.STREAM_CODEC.decode(friendlyByteBuf));
            case 3 -> {
                IRecipeInput[] var1_1 = new IRecipeInput[friendlyByteBuf.readVarInt()];
                for (int var2_2 = 0; var2_2 < var1_1.length; ++var2_2) {
                    var1_1[var2_2] = RecipeIo.readInput(friendlyByteBuf);
                }
                yield new RecipeInputMultiple(friendlyByteBuf.readVarInt(), var1_1);
            }
            default -> throw new IllegalArgumentException("Unkown RecipeInput type.");
        };
    }

    public static void writeOutput(RegistryFriendlyByteBuf friendlyByteBuf, Collection<ItemStack> collection) {
        friendlyByteBuf.writeVarInt(collection.size());
        for (ItemStack itemStack : collection) {
            ItemStack.STREAM_CODEC.encode(friendlyByteBuf, itemStack);
        }
    }

    public static Collection<ItemStack> readOutput(RegistryFriendlyByteBuf friendlyByteBuf) {
        int n = friendlyByteBuf.readVarInt();
        ArrayList<ItemStack> arrayList = new ArrayList<ItemStack>(n);
        for (int i = 0; i < n; ++i) {
            arrayList.add(ItemStack.STREAM_CODEC.decode(friendlyByteBuf));
        }
        return arrayList;
    }

    public static void writeFluidStack(RegistryFriendlyByteBuf friendlyByteBuf, Ic2FluidStack ic2FluidStack) {
        friendlyByteBuf.writeVarInt(BuiltInRegistries.FLUID.getId(ic2FluidStack.getFluid()));
        friendlyByteBuf.writeVarInt(ic2FluidStack.getAmountMb());
    }

    public static Ic2FluidStack readFluidStack(RegistryFriendlyByteBuf friendlyByteBuf) {
        return FluidHandler.createFluidStackMb((Fluid)BuiltInRegistries.FLUID.byId(friendlyByteBuf.readVarInt()), friendlyByteBuf.readVarInt(), null);
    }

    private static Fluid asFluid(JsonElement jsonElement, String string) {
        if (jsonElement.isJsonPrimitive()) {
            String string2 = jsonElement.getAsString();
            return (Fluid)BuiltInRegistries.FLUID.getOptional(ResourceLocation.parse(string2)).orElseThrow((Supplier<JsonSyntaxException>)() -> fluidNotFound(string, string2));
        }
        throw new JsonSyntaxException("Expected " + string + " to be an fluid, was " + GsonHelper.getType((JsonElement)jsonElement));
    }

    private static CompoundTag asNbt(JsonElement jsonElement, String string) {
        Tag tag = (Tag)JsonOps.INSTANCE.convertTo((DynamicOps)NbtOps.INSTANCE, jsonElement);
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag = (CompoundTag)tag;
            return compoundTag;
        }
        throw new JsonSyntaxException("Expected " + string + " to be an NBT compound (an object), was " + GsonHelper.getType((JsonElement)jsonElement));
    }

    @Nullable
    @Contract(value="_,_,!null->!null;_,_,null->_")
    private static CompoundTag getNbt(JsonObject jsonObject, String string, @Nullable CompoundTag compoundTag) {
        if (jsonObject.has(string)) {
            return RecipeIo.asNbt(jsonObject.get(string), string);
        }
        return compoundTag;
    }

    private static JsonSyntaxException fluidNotFound(String string, String string2) {
        return new JsonSyntaxException("Expected " + string + " to be an fluid, was unknown string '" + string2 + "'");
    }
}

