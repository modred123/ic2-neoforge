package ic2.compat;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CraftingRecipe;

public interface Ic2CraftingRecipe
extends CraftingRecipe {
    public int getIc2RecipeWidth();

    public int getIc2RecipeHeight();
}
