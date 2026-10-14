package ic2.integration.jei.recipe.machine;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public class IORecipeWrapper {
    private final IRecipeInput input;
    private final Collection<ItemStack> output;
    private final List<List<ItemStack>> additionalInputs;

    public IORecipeWrapper(MachineRecipe<IRecipeInput, Collection<ItemStack>> machineRecipe) {
        this(machineRecipe.getInput(), machineRecipe.getOutput());
    }

    public IORecipeWrapper(IRecipeInput input, Collection<ItemStack> output) {
        this(input, output, Collections.emptyList());
    }

    public IORecipeWrapper(IRecipeInput input, Collection<ItemStack> output, List<List<ItemStack>> additionalInputs) {
        this.input = input;
        this.output = output;
        this.additionalInputs = additionalInputs;
    }

    public List<List<ItemStack>> getInputs() {
        List<ItemStack> list = this.input.getInputs();
        if (list.isEmpty() && this.additionalInputs.isEmpty()) {
            return Collections.emptyList();
        }
        java.util.ArrayList<List<ItemStack>> inputs = new java.util.ArrayList<>();
        inputs.add(list);
        inputs.addAll(this.additionalInputs);
        return inputs;
    }

    public List<ItemStack> getOutputs() {
        return new ArrayList<>(this.output);
    }
}
