package ic2.integration.jei.recipe.misc;

import net.minecraft.world.item.ItemStack;

public final class ScrapboxRecipeWrapper {
    private final ItemStack output;
    private final float chance;

    public ScrapboxRecipeWrapper(ItemStack output, float chance) {
        this.output = output.copy();
        this.chance = chance;
    }

    public ItemStack getOutput() {
        return this.output;
    }

    public float getChance() {
        return this.chance;
    }
}
