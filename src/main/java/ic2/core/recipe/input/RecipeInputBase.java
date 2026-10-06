/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  net.minecraft.world.item.ItemStack
 */
package ic2.core.recipe.input;

import com.google.gson.JsonElement;
import ic2.api.recipe.IRecipeInput;
import ic2.core.util.StackUtil;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.world.item.ItemStack;

public abstract class RecipeInputBase
implements IRecipeInput {
    private List<ItemStack> inputs = null;

    protected RecipeInputBase() {
    }

    public abstract JsonElement toJson();

    protected abstract List<ItemStack> listStacks();

    @Override
    public final List<ItemStack> getInputs() {
        if (this.inputs == null) {
            this.inputs = this.listStacks();
            this.inputs.replaceAll(this::lambda$getInputs$0);
            this.inputs = Collections.unmodifiableList(this.inputs);
        }
        return this.inputs;
    }

    private /* synthetic */ ItemStack lambda$getInputs$0(ItemStack itemStack) {
        return StackUtil.setImmutableSize(itemStack, this.getAmount());
    }
}

