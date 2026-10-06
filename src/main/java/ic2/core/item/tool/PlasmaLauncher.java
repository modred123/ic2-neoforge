/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.util.InteractionResult
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.level.Level
 */
package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.item.tool.EntityParticle;
import ic2.core.ref.Ic2Entities;
import ic2.core.item.tool.ItemElectricTool;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

public class PlasmaLauncher
extends ItemElectricTool {
    public PlasmaLauncher() {
        super(ItemName.plasma_launcher, 100);
        this.maxCharge = 40000;
        this.transferLimit = 128;
        this.tier = 3;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!IC2.platform.isSimulating()) {
            return InteractionResultHolder.pass(StackUtil.get(player, hand));
        }
        EntityParticle particle = new EntityParticle(Ic2Entities.PLASMA_PARTICLE, world, (LivingEntity)player, 8.0f, 1.0, 2.0);
        world.addFreshEntity(particle);
        return super.use(world, player, hand);
    }
}

