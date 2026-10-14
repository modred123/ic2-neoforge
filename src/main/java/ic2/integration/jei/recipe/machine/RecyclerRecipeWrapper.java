package ic2.integration.jei.recipe.machine;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public final class RecyclerRecipeWrapper {
    private final List<ItemStack> inputs;
    private final ItemStack output;
    private final double outputChance;

    public RecyclerRecipeWrapper(List<ItemStack> inputs, ItemStack output, double outputChance) {
        this.inputs = List.copyOf(inputs);
        this.output = output;
        this.outputChance = outputChance;
    }

    public List<ItemStack> getInputs() {
        return this.inputs;
    }

    public ItemStack getOutput() {
        return this.output;
    }

    public double getOutputChance() {
        return this.outputChance;
    }
}
