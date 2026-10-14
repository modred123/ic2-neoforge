package ic2.integration.jei.recipe.machine;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public final class FluidRecipeWrapper {
    private final Fluid inputFluid;
    private final int inputAmount;
    private final List<ItemStack> itemInputs;
    private final List<FluidOutput> outputs;

    public FluidRecipeWrapper(Fluid inputFluid, int inputAmount, List<ItemStack> itemInputs, List<FluidOutput> outputs) {
        this.inputFluid = inputFluid;
        this.inputAmount = inputAmount;
        this.itemInputs = List.copyOf(itemInputs);
        this.outputs = List.copyOf(outputs);
    }

    public Fluid getInputFluid() {
        return this.inputFluid;
    }

    public int getInputAmount() {
        return this.inputAmount;
    }

    public List<ItemStack> getItemInputs() {
        return this.itemInputs;
    }

    public List<FluidOutput> getOutputs() {
        return this.outputs;
    }

    public record FluidOutput(Fluid fluid, int amount) {
    }
}
