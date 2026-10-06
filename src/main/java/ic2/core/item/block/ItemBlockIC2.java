/*
 * ex112 ItemBlockIC2 兼容层 1.21.1 适配版
 * 1.12.2 的 ItemBlock → 1.21.1 BlockItem；删 1.12.2 的 BlockName/Material 依赖。
 */
package ic2.core.item.block;

import ic2.core.init.Localization;
import java.util.function.Function;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ItemBlockIC2
extends net.minecraft.world.item.BlockItem {
    public static Function<Block, Item> supplier = ItemBlockIC2::new;

    public ItemBlockIC2(Block block) {
        super(block, new Item.Properties());
    }

    @Override
    public String getDescriptionId() {
        return this.getBlock().getDescriptionId();
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return this.getDescriptionId();
    }

    public String getTranslationKey(ItemStack stack) {
        return this.getDescriptionId(stack);
    }

    public String getItemStackDisplayName(ItemStack stack) {
        return Localization.translate(this.getTranslationKey(stack));
    }

    public boolean canHarvestBlock(BlockState block, ItemStack stack) {
        return false;
    }

    @Override
    public int getBurnTime(ItemStack stack, net.minecraft.world.item.crafting.RecipeType<?> recipeType) {
        return -1;
    }

    public Rarity getRarity(ItemStack stack) {
        return stack.getRarity();
    }
}
