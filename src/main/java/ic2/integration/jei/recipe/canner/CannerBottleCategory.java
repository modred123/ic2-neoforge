package ic2.integration.jei.recipe.canner;

import ic2.core.ref.Ic2Blocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 第四十九轮新增：装罐机「装瓶/装罐」配方的 JEI 分类。
 *
 * 用户实测反馈："JEI 里没有核燃料棒的相关配方（不论作为材料还是产物）" ——
 * 根因是 {@code Ic2JeiPlugin} 只注册了 10 种基础机器配方类型，
 * **漏掉了 {@code canner_bottle}**（它的管理器是 {@code ICannerBottleRecipeManager}，
 * 与其它机器的 {@code IBasicMachineRecipeManager} 不同，所以没被现成的辅助方法覆盖）。
 * "把核燃料压进空燃料棒"恰好就是这条配方。
 */
public class CannerBottleCategory implements IRecipeCategory<CannerBottleRecipeWrapper> {
    /** 装罐机的 GUI 贴图（与 `ic2.core.block.machine.gui.GuiCanner.texture` 同一张）。 */
    private static final net.minecraft.resources.ResourceLocation TEXTURE =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guicanner.png");

    private final RecipeType<CannerBottleRecipeWrapper> recipeType;
    private final IDrawable background;
    private final IDrawable icon;

    public CannerBottleCategory(RecipeType<CannerBottleRecipeWrapper> recipeType, IGuiHelper guiHelper) {
        this.recipeType = recipeType;
        // 第五十轮修复：原先用 `createBlankDrawable`（空白），配方页里只看得到物品、看不到机器界面
        // （用户实测反馈"没看到机器的 gui"）。现改为直接截取装罐机 GUI 贴图的机器面板区域
        // (0,0)-(140,72) —— 这块正好包含三个槽位与进度条，槽位坐标也与贴图对齐（见 setRecipe）。
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 140, 72);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(Ic2Blocks.CANNER));
    }

    @Override
    public RecipeType<CannerBottleRecipeWrapper> getRecipeType() {
        return this.recipeType;
    }

    @Override
    public Component getTitle() {
        return new ItemStack(Ic2Blocks.CANNER).getHoverName();
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
    public void setRecipe(IRecipeLayoutBuilder builder, CannerBottleRecipeWrapper recipe, IFocusGroup focuses) {
        // 坐标取自装罐机 GUI 贴图上的真实槽位（左上=容器、中间=填充物、右上=产物）
        builder.addSlot(RecipeIngredientRole.INPUT, 41, 17).addItemStacks(recipe.getContainerInputs());
        builder.addSlot(RecipeIngredientRole.INPUT, 76, 40).addItemStacks(recipe.getFillInputs());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 116, 14).addItemStack(recipe.getOutput());
    }
}
