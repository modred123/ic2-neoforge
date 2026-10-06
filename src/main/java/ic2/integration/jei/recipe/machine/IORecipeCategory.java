package ic2.integration.jei.recipe.machine;

import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.integration.jeirei.SlotPosition;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public abstract class IORecipeCategory implements IRecipeCategory<IORecipeWrapper> {
    protected final Ic2TileEntityBlock block;

    public IORecipeCategory(Ic2TileEntityBlock block) {
        this.block = block;
    }

    @Override
    public Component getTitle() {
        return this.getBlockStack().getHoverName();
    }

    protected abstract List<SlotPosition> getInputSlotPos();

    protected abstract List<SlotPosition> getOutputSlotPos();

    protected void addRecipeSlots(IRecipeLayoutBuilder builder, IORecipeWrapper recipe, IFocusGroup focusGroup, int xOffset, int yOffset) {
        List<SlotPosition> inputSlots = this.getInputSlotPos();
        List<List<ItemStack>> inputs = recipe.getInputs();
        for (int i = 0; i < inputSlots.size(); i++) {
            SlotPosition pos = inputSlots.get(i);
            IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT, pos.getX() + xOffset, pos.getY() + yOffset);
            if (i < inputs.size()) {
                slot.addItemStacks(inputs.get(i));
            }
        }
        List<SlotPosition> outputSlots = this.getOutputSlotPos();
        List<ItemStack> outputs = recipe.getOutputs();
        for (int i = 0; i < outputSlots.size(); i++) {
            SlotPosition pos = outputSlots.get(i);
            IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.OUTPUT, pos.getX() + xOffset, pos.getY() + yOffset);
            if (i < outputs.size()) {
                slot.addItemStack(outputs.get(i));
            }
        }
    }

    public ItemStack getBlockStack() {
        return new ItemStack(this.block);
    }

    // getIcon 由子类实现（JEI 抽象方法，不能返回 null）
}
