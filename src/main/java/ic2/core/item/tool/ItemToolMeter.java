/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.core.Direction
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.Level
 */
package ic2.core.item.tool;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.item.IHandHeldInventory;
import ic2.core.item.ItemIC2;
import ic2.core.item.tool.ContainerMeter;
import ic2.core.item.tool.HandHeldMeter;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class ItemToolMeter
extends ItemIC2
implements IBoxable,
IHandHeldInventory {
    public ItemToolMeter() {
        this(new net.minecraft.world.item.Item.Properties().stacksTo(1));
    }

    /** 1.21.1（第三十二轮）：堆叠上限必须在构造期写进 Item.Properties —— Item.getMaxStackSize 已被移除，
     *  旧写法 setMaxStackSize(1) 只会写进无人读取的兼容字段。 */
    public ItemToolMeter(net.minecraft.world.item.Item.Properties properties) {
        super(ItemName.meter, properties);
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext useOnContext) {
        Player player = useOnContext.getPlayer();
        Level world = useOnContext.getLevel();
        BlockPos pos = useOnContext.getClickedPos();
        Direction side = useOnContext.getClickedFace();
        InteractionHand hand = useOnContext.getHand();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (world.isClientSide) {
            return InteractionResult.PASS;
        }
        IEnergyTile tile = EnergyNet.instance.getTile(world, pos);
        if (tile instanceof IEnergySource || tile instanceof IEnergyConductor || tile instanceof IEnergySink) {
            if (IC2.platform.launchGui(player, this.getInventory(player, hand, StackUtil.get(player, hand)))) {
                ContainerMeter container = (ContainerMeter)player.containerMenu;
                container.setUut(tile);
                return InteractionResult.SUCCESS;
            }
        } else {
            IC2.platform.messagePlayer(player, "Not an energy net tile", new Object[0]);
        }
        return InteractionResult.SUCCESS;
    }

    public boolean onDroppedByPlayer(ItemStack stack, Player player) {
        HandHeldMeter euReader;
        if (!player.level().isClientSide && !StackUtil.isEmpty(stack) && player.containerMenu instanceof ContainerMeter && (euReader = (HandHeldMeter)((ContainerMeter)player.containerMenu).base).isThisContainer(stack)) {
            euReader.saveAsThrown(stack);
            player.closeContainer();
        }
        return true;
    }

    @Override
    public boolean canBeStoredInToolbox(ItemStack itemstack) {
        return true;
    }

    @Override
    public IHasGui getInventory(Player player, net.minecraft.world.InteractionHand hand, ItemStack stack) {
        return new HandHeldMeter(player, hand, stack);
    }
}

