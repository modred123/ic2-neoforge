package ic2.core.item.tool;

import ic2.api.item.ElectricItem;
import ic2.core.item.tool.HarvestLevel;
import ic2.core.item.tool.ItemElectricTool;
import ic2.core.item.tool.ToolClass;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class ItemElectricToolHoe
extends ItemElectricTool {
    public ItemElectricToolHoe() {
        super(ItemName.electric_hoe, 50, HarvestLevel.Iron, EnumSet.of(ToolClass.Hoe));
        this.maxCharge = 10000;
        this.transferLimit = 100;
        this.tier = 1;

    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        ItemStack stack = context.getItemInHand();
        if (player == null || !world.mayInteract(player, pos)) {
            return InteractionResult.PASS;
        }
        if (!ElectricItem.manager.canUse(stack, this.operationEnergyCost)) {
            return InteractionResult.PASS;
        }
        BlockState state = world.getBlockState(pos);
        BlockState tilled = state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) ? Blocks.FARMLAND.defaultBlockState() : null;
        if (side != Direction.DOWN && tilled != null && world.isEmptyBlock(pos.above())) {
            SoundType stepSound = tilled.getSoundType(world, pos, player);
            world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stepSound.getStepSound(), SoundSource.BLOCKS, (stepSound.getVolume() + 1.0f) / 2.0f, stepSound.getPitch() * 0.8f);
            if (!world.isClientSide) {
                world.setBlockAndUpdate(pos, tilled);
                ElectricItem.manager.use(stack, this.operationEnergyCost, (LivingEntity)player);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
