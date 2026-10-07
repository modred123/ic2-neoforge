package ic2.integration.jei.recipe.canner;

import ic2.api.recipe.IRecipeInput;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/**
 * 第四十九轮新增：装罐机「装瓶/装罐」配方的 JEI 包装。
 *
 * 装罐机（Canner）的这条配方是 **两个输入**：容器（空燃料棒/空罐） + 填充物（铀锭/MOX 等），
 * 与其它机器的"单输入多输出"不同，因此不能复用 {@code IORecipeWrapper}。
 *
 * 对应 1.12.2 的权威定义：{@code ic2/api/recipe/ICannerBottleRecipeManager.java}
 * （{@code Input { IRecipeInput container; IRecipeInput fill; }}，输出为单个 {@code ItemStack}）。
 */
public class CannerBottleRecipeWrapper {
    private final IRecipeInput container;
    private final IRecipeInput fill;
    private final ItemStack output;

    public CannerBottleRecipeWrapper(IRecipeInput container, IRecipeInput fill, ItemStack output) {
        this.container = container;
        this.fill = fill;
        this.output = output;
    }

    /** 容器槽的可选物品（例如空燃料棒）。 */
    public List<ItemStack> getContainerInputs() {
        return this.container.getInputs();
    }

    /** 填充物槽的可选物品（例如铀锭）。 */
    public List<ItemStack> getFillInputs() {
        return this.fill.getInputs();
    }

    /** 产物（例如铀燃料棒）。 */
    public ItemStack getOutput() {
        return this.output;
    }
}
