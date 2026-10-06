/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  net.minecraft.core.Registry
 *  net.minecraft.nbt.NbtOps
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.item.ItemStack
 */
package ic2.core.recipe.input;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import ic2.core.recipe.input.RecipeInputBase;
import ic2.core.util.StackUtil;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class RecipeInputItemStack
extends RecipeInputBase {
    public final ItemStack input;

    public RecipeInputItemStack(ItemStack itemStack) {
        if (StackUtil.isEmpty(itemStack)) {
            throw new IllegalArgumentException("invalid input stack");
        }
        this.input = itemStack.copy();
    }

    @Override
    public boolean matches(ItemStack itemStack) {
        return StackUtil.checkItemEqualityStrict(this.input, itemStack);
    }

    @Override
    public int getAmount() {
        return this.input.getCount();
    }

    @Override
    protected List<ItemStack> listStacks() {
        return List.of(this.input);
    }

    public String toString() {
        return "RInputItemStack<" + StackUtil.setImmutableSize(this.input, this.getAmount()) + ">";
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object object) {
        if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;
        RecipeInputItemStack recipeInputItemStack = (RecipeInputItemStack)object;
        if (!StackUtil.checkItemEqualityStrict(recipeInputItemStack.input, this.input)) return false;
        if (recipeInputItemStack.getAmount() != this.getAmount()) return false;
        return true;
    }

    @Override
    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("item", BuiltInRegistries.ITEM.getKey(this.input.getItem()).toString());
        jsonObject.add("data", (JsonElement)NbtOps.INSTANCE.convertTo((DynamicOps)JsonOps.INSTANCE, (Tag)ic2.core.util.StackUtil.getTag(this.input)));
        if (this.input.getCount() != 1) {
            jsonObject.addProperty("count", (Number)this.input.getCount());
        }
        return jsonObject;
    }
}

