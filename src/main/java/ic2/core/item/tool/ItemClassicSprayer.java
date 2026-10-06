/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.core.Direction
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.Level
 */
package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.block.wiring.CableBlock;
import ic2.core.block.wiring.FoamCableBlock;
import ic2.core.ref.Ic2Blocks;
import ic2.core.item.ItemGradualInt;
import ic2.core.item.armor.ItemArmorClassicCFPack;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.Random;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class ItemClassicSprayer
extends ItemGradualInt {
    public ItemClassicSprayer() {
        super(ItemName.foam_sprayer, 1602);
        this.setMaxStackSize(1);
    }

    public InteractionResult onItemUse(Player player, Level world, BlockPos pos, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        boolean pulledFromCFPack;
        if (!IC2.platform.isSimulating()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = StackUtil.get(player, hand);
        ItemStack pack = (ItemStack)player.getInventory().armor.get(2);
        boolean bl = pulledFromCFPack = StackUtil.check(pack) && pack.getItem() == ItemName.cf_pack.getInstance() && ((ItemArmorClassicCFPack)pack.getItem()).getCFPellet(player, pack);
        if (!pulledFromCFPack && this.getCustomDamage(stack) < 100) {
            return InteractionResult.FAIL;
        }
        if (ItemClassicSprayer.isScaffold(world.getBlockState(pos).getBlock())) {
            this.sprayFoam(world, pos, ItemClassicSprayer.calculateDirectionsFromPlayer(player), true);
            if (!pulledFromCFPack) {
                this.applyCustomDamage(stack, 100, (LivingEntity)player);
            }
            return InteractionResult.SUCCESS;
        }
        if (this.sprayFoam(world, pos.relative(facing), ItemClassicSprayer.calculateDirectionsFromPlayer(player), false)) {
            if (!pulledFromCFPack) {
                this.applyCustomDamage(stack, 100, (LivingEntity)player);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static boolean[] calculateDirectionsFromPlayer(Player player) {
        float yaw = player.getYRot() % 360.0f;
        float pitch = player.getXRot();
        boolean[] r = new boolean[]{true, true, true, true, true, true};
        if (pitch >= -65.0f && pitch <= 65.0f) {
            if (yaw >= 300.0f && yaw <= 360.0f || yaw >= 0.0f && yaw <= 60.0f) {
                r[2] = false;
            }
            if (yaw >= 30.0f && yaw <= 150.0f) {
                r[5] = false;
            }
            if (yaw >= 120.0f && yaw <= 240.0f) {
                r[3] = false;
            }
            if (yaw >= 210.0f && yaw <= 330.0f) {
                r[4] = false;
            }
        }
        if (pitch <= -40.0f) {
            r[0] = false;
        }
        if (pitch >= 40.0f) {
            r[1] = false;
        }
        return r;
    }

    public boolean sprayFoam(Level world, BlockPos start, boolean[] directions, boolean scaffold) {
        BlockPos set;
        if (!ItemClassicSprayer.canFoam(world, start, scaffold)) {
            return false;
        }
        ArrayDeque<BlockPos> check = new ArrayDeque<BlockPos>();
        LinkedHashSet<BlockPos> place = new LinkedHashSet<BlockPos>();
        int foamcount = ItemClassicSprayer.getSprayMass();
        check.add(start);
        while ((set = (BlockPos)check.poll()) != null && foamcount > 0) {
            if (!ItemClassicSprayer.canFoam(world, set, scaffold) || !place.add(set)) continue;
            for (int i : ItemClassicSprayer.generateRngSpread(IC2.random)) {
                if (!scaffold && !directions[i]) continue;
                check.add(set.relative(Direction.from3DDataValue(i)));
            }
            --foamcount;
        }
        for (BlockPos pos : place) {
            BlockState state = world.getBlockState(pos);
            Block targetBlock = state.getBlock();
            if (ItemClassicSprayer.isScaffold(targetBlock)) {
                world.destroyBlock(pos, true);
                world.setBlockAndUpdate(pos, Ic2Blocks.FOAM.defaultBlockState());
                continue;
            }
            if (targetBlock instanceof CableBlock && !(targetBlock instanceof FoamCableBlock)) {
                continue;
            }
            world.setBlockAndUpdate(pos, Ic2Blocks.FOAM.defaultBlockState());
        }
        return true;
    }

    private static boolean isScaffold(Block block) {
        return block == Ic2Blocks.WOODEN_SCAFFOLD || block == Ic2Blocks.REINFORCED_WOODEN_SCAFFOLD
            || block == Ic2Blocks.IRON_SCAFFOLD || block == Ic2Blocks.REINFORCED_IRON_SCAFFOLD;
    }

    private static boolean canFoam(Level world, BlockPos pos, boolean scaffold) {
        if (!scaffold) {
            if (Ic2Blocks.FOAM.defaultBlockState().canSurvive(world, pos)) {
                return true;
            }
            Block block = world.getBlockState(pos).getBlock();
            return block instanceof CableBlock && !(block instanceof FoamCableBlock);
        }
        return ItemClassicSprayer.isScaffold(world.getBlockState(pos).getBlock());
    }

    private static int[] generateRngSpread(net.minecraft.util.RandomSource random) {
        int[] re = new int[]{0, 1, 2, 3, 4, 5};
        for (int i = 0; i < 16; ++i) {
            int first = random.nextInt(6);
            int second = random.nextInt(6);
            int temp = re[first];
            re[first] = re[second];
            re[second] = temp;
        }
        return re;
    }

    public static int getSprayMass() {
        return 13;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1.0 - super.getDurabilityForDisplay(stack);
    }
}

