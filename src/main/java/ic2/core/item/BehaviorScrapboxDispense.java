package ic2.core.item;

import ic2.api.recipe.Recipes;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;

public class BehaviorScrapboxDispense implements DispenseItemBehavior {
    @Override
    public ItemStack dispense(BlockSource blockSource, ItemStack stack) {
        if (StackUtil.checkItemEquality(stack, ItemName.crafting.getItemStack(CraftingItemType.scrap_box))) {
            Direction facing = blockSource.state().getValue(DispenserBlock.FACING);
            Position position = DispenserBlock.getDispensePosition(blockSource);
            ItemStack drop = Recipes.scrapboxDrops.getDrop(stack, true);
            if (drop != null) {
                BehaviorScrapboxDispense.spawnItem(blockSource.level(), drop, 6, facing, position);
            }
        }
        return stack;
    }

    private static void spawnItem(net.minecraft.server.level.ServerLevel level, ItemStack stack, int speed, Direction facing, Position position) {
        net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(level,
            position.x(), position.y(), position.z(), stack);
        double d = level.random.nextDouble() * 0.1 + 0.2;
        itemEntity.setDeltaMovement(level.random.triangle((double)facing.getStepX() * d, 0.0172275 * (double)speed),
            level.random.triangle(0.2, 0.0172275 * (double)speed),
            level.random.triangle((double)facing.getStepZ() * d, 0.0172275 * (double)speed));
        level.addFreshEntity(itemEntity);
    }
}
