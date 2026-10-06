/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 */
package ic2.core.block.machine.tileentity;

import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.block.machine.tileentity.TileEntityStandardMachine;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2SoundEvents;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityMacerator
extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack> {
    public static List<Map.Entry<ItemStack, ItemStack>> recipes = new Vector<Map.Entry<ItemStack, ItemStack>>();

    public TileEntityMacerator(BlockPos blockPos, BlockState blockState) {
        super(Ic2BlockEntities.MACERATOR, blockPos, blockState, 2, 300, 1);
        this.inputSlot = new InvSlotProcessableGeneric((IInventorySlotHolder<?>)this, "input", 1, (Recipes.IGetter<? extends IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack>>)Recipes.macerator);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void updateEntityClient() {
        super.updateEntityClient();
        Level level = this.getLevel();
        if (this.getActive() && level.random.nextInt(8) == 0) {
            for (int i = 0; i < 4; ++i) {
                double d = (double)this.worldPosition.getX() + 0.5 + (double)level.random.nextFloat() * 0.6 - 0.3;
                double d2 = (double)(this.worldPosition.getY() + 1) + (double)level.random.nextFloat() * 0.2 - 0.1;
                double d3 = (double)this.worldPosition.getZ() + 0.5 + (double)level.random.nextFloat() * 0.6 - 0.3;
                level.addParticle((ParticleOptions)ParticleTypes.SMOKE, d, d2, d3, 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    public SoundEvent getLoopingSoundEvent() {
        return Ic2SoundEvents.MACHINE_MACERATOR_OPERATE;
    }

    @Override
    public SoundEvent getInterruptSoundEvent() {
        return Ic2SoundEvents.MACHINE_INTERRUPT1;
    }

    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
    }
}

