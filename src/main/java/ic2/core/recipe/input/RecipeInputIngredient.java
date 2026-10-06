/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Ingredient
 */
package ic2.core.recipe.input;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ic2.core.recipe.input.RecipeInputBase;
import java.util.Arrays;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class RecipeInputIngredient
extends RecipeInputBase {
    private final Ingredient ingredient;
    private final int amount;

    public RecipeInputIngredient(Ingredient ingredient, int n) {
        this.ingredient = ingredient;
        this.amount = n;
    }

    @Override
    public boolean matches(ItemStack itemStack) {
        return !itemStack.isEmpty() && this.ingredient.test(itemStack);
    }

    @Override
    protected List<ItemStack> listStacks() {
        return Arrays.asList(this.ingredient.getItems());
    }

    @Override
    public int getAmount() {
        return this.amount;
    }

    @Override
    public Ingredient getIngredient() {
        return this.ingredient;
    }

    @Override
    public JsonElement toJson() {
        JsonElement jsonElement = net.minecraft.world.item.crafting.Ingredient.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, this.ingredient).getOrThrow(com.google.gson.JsonSyntaxException::new);
        if (this.amount == 1) {
            return jsonElement;
        }
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            jsonObject.addProperty("count", (Number)this.amount);
            return jsonObject;
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("any", jsonElement);
        jsonObject.addProperty("count", (Number)this.amount);
        return jsonObject;
    }
}

