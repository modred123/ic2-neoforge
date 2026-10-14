package ic2.integration.jei.recipe.misc;

import ic2.core.ref.Ic2Items;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class ScrapboxRecipeCategory implements IRecipeCategory<ScrapboxRecipeWrapper> {
    private final RecipeType<ScrapboxRecipeWrapper> recipeType;
    private final IDrawable background;
    private final IDrawable icon;

    public ScrapboxRecipeCategory(RecipeType<ScrapboxRecipeWrapper> recipeType, IGuiHelper guiHelper) {
        this.recipeType = recipeType;
        this.background = guiHelper.createBlankDrawable(112, 26);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(Ic2Items.SCRAP_BOX));
    }

    @Override
    public RecipeType<ScrapboxRecipeWrapper> getRecipeType() {
        return this.recipeType;
    }

    @Override
    public Component getTitle() {
        return new ItemStack(Ic2Items.SCRAP_BOX).getHoverName();
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
    public void setRecipe(IRecipeLayoutBuilder builder, ScrapboxRecipeWrapper recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 0, 4).setStandardSlotBackground()
                .addItemStack(new ItemStack(Ic2Items.SCRAP_BOX));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 48, 4).setStandardSlotBackground().addItemStack(recipe.getOutput());
    }

    @Override
    public void draw(ScrapboxRecipeWrapper recipe, IRecipeSlotsView slotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        float chance = recipe.getChance() * 100.0f;
        String label = chance < 0.01f ? "<0.01%" : String.format(java.util.Locale.ROOT, "%.2f%%", chance);
        guiGraphics.drawString(Minecraft.getInstance().font, label, 70, 9, 0xff404040, false);
    }
}
