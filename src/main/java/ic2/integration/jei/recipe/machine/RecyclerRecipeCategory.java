package ic2.integration.jei.recipe.machine;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;

public final class RecyclerRecipeCategory implements IRecipeCategory<RecyclerRecipeWrapper> {
    private final Block block;
    private final RecipeType<RecyclerRecipeWrapper> recipeType;
    private final IDrawable background;
    private final IDrawable icon;

    public RecyclerRecipeCategory(Block block, RecipeType<RecyclerRecipeWrapper> recipeType, IGuiHelper guiHelper) {
        this.block = block;
        this.recipeType = recipeType;
        this.background = guiHelper.createBlankDrawable(102, 26);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(block));
    }

    @Override
    public RecipeType<RecyclerRecipeWrapper> getRecipeType() {
        return this.recipeType;
    }

    @Override
    public Component getTitle() {
        return new ItemStack(this.block).getHoverName();
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecyclerRecipeWrapper recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 0, 4).setStandardSlotBackground().addItemStacks(recipe.getInputs());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 48, 4).setStandardSlotBackground().addItemStack(recipe.getOutput());
    }

    @Override
    public void draw(RecyclerRecipeWrapper recipe, IRecipeSlotsView slotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        String chance = String.format(java.util.Locale.ROOT, "%.1f%%", recipe.getOutputChance() * 100.0);
        guiGraphics.drawString(Minecraft.getInstance().font, chance, 70, 9, 0xff404040, false);
    }
}
