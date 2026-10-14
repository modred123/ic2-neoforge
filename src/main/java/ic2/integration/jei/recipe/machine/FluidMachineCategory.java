package ic2.integration.jei.recipe.machine;

import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class FluidMachineCategory implements IRecipeCategory<FluidRecipeWrapper> {
    private final RecipeType<FluidRecipeWrapper> recipeType;
    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;
    private final Position inputPosition;
    private final Position itemInputPosition;
    private final List<Position> outputPositions;
    private final int inputFluidSlotWidth;
    private final int inputFluidSlotHeight;
    private final int outputFluidSlotWidth;
    private final int outputFluidSlotHeight;
    private final boolean drawStandardSlotBackground;

    public FluidMachineCategory(Block block, RecipeType<FluidRecipeWrapper> recipeType, IGuiHelper guiHelper,
                                int width, int height, int[] inputPosition, int[] itemInputPosition, int[][] outputPositions) {
        this(recipeType, guiHelper.createBlankDrawable(width, height), guiHelper.createDrawableItemStack(new ItemStack(block)),
                new ItemStack(block).getHoverName(), inputPosition, itemInputPosition, outputPositions,
                16, 16, 16, 16, true);
    }

    public FluidMachineCategory(RecipeType<FluidRecipeWrapper> recipeType, IDrawable background,
                                IDrawable icon, Component title, int[] inputPosition, int[] itemInputPosition,
                                int[][] outputPositions, int fluidSlotWidth, int fluidSlotHeight,
                                boolean drawStandardSlotBackground) {
        this(recipeType, background, icon, title, inputPosition, itemInputPosition, outputPositions,
                fluidSlotWidth, fluidSlotHeight, fluidSlotWidth, fluidSlotHeight, drawStandardSlotBackground);
    }

    public FluidMachineCategory(RecipeType<FluidRecipeWrapper> recipeType, IDrawable background,
                                IDrawable icon, Component title, int[] inputPosition, int[] itemInputPosition,
                                int[][] outputPositions, int inputFluidSlotWidth, int inputFluidSlotHeight,
                                int outputFluidSlotWidth, int outputFluidSlotHeight,
                                boolean drawStandardSlotBackground) {
        this.recipeType = recipeType;
        this.background = background;
        this.icon = icon;
        this.title = title;
        this.inputPosition = new Position(inputPosition[0], inputPosition[1]);
        this.itemInputPosition = itemInputPosition == null ? null : new Position(itemInputPosition[0], itemInputPosition[1]);
        this.outputPositions = new ArrayList<>(outputPositions.length);
        for (int[] position : outputPositions) {
            this.outputPositions.add(new Position(position[0], position[1]));
        }
        this.inputFluidSlotWidth = inputFluidSlotWidth;
        this.inputFluidSlotHeight = inputFluidSlotHeight;
        this.outputFluidSlotWidth = outputFluidSlotWidth;
        this.outputFluidSlotHeight = outputFluidSlotHeight;
        this.drawStandardSlotBackground = drawStandardSlotBackground;
    }

    @Override
    public RecipeType<FluidRecipeWrapper> getRecipeType() {
        return this.recipeType;
    }

    @Override
    public Component getTitle() {
        return this.title;
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
    public void setRecipe(IRecipeLayoutBuilder builder, FluidRecipeWrapper recipe, IFocusGroup focuses) {
        this.addFluidSlot(builder, RecipeIngredientRole.INPUT, this.inputPosition, recipe.getInputFluid(), recipe.getInputAmount(),
                this.inputFluidSlotWidth, this.inputFluidSlotHeight);
        if (this.itemInputPosition != null && !recipe.getItemInputs().isEmpty()) {
            IRecipeSlotBuilder itemSlot = builder.addSlot(RecipeIngredientRole.INPUT, this.itemInputPosition.x, this.itemInputPosition.y);
            if (this.drawStandardSlotBackground) {
                itemSlot.setStandardSlotBackground();
            }
            itemSlot.addItemStacks(recipe.getItemInputs());
        }

        int firstOutput = (this.outputPositions.size() - recipe.getOutputs().size()) / 2;
        for (int i = 0; i < recipe.getOutputs().size() && i + firstOutput < this.outputPositions.size(); i++) {
            FluidRecipeWrapper.FluidOutput output = recipe.getOutputs().get(i);
            if (output.fluid() != null && output.amount() > 0) {
                this.addFluidSlot(builder, RecipeIngredientRole.OUTPUT, this.outputPositions.get(i + firstOutput), output.fluid(), output.amount(),
                        this.outputFluidSlotWidth, this.outputFluidSlotHeight);
            }
        }
    }

    private void addFluidSlot(IRecipeLayoutBuilder builder, RecipeIngredientRole role, Position position,
                              net.minecraft.world.level.material.Fluid fluid, int amount, int width, int height) {
        IRecipeSlotBuilder slot = builder.addSlot(role, position.x, position.y);
        if (this.drawStandardSlotBackground) {
            slot.setStandardSlotBackground();
        }
        slot.setFluidRenderer(Math.max(1, amount), false, width, height).addFluidStack(fluid, amount);
    }

    private record Position(int x, int y) {
    }
}
