/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Supplier
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.Container
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.Vec3
 */
package ic2.core.block.reactor.tileentity;

import com.google.common.base.Supplier;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.reactor.IReactorChamber;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.Redstone;
import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class TileEntityReactorChamberElectric
extends Ic2TileEntity
implements Container,
IReactorChamber,
IEnergyEmitter {
    public final Redstone redstone = this.addComponent(new Redstone(this));
    protected final Fluids fluids = this.addComponent(new Fluids(this));
    private TileEntityNuclearReactorElectric reactor;
    private long lastReactorUpdate;

    public TileEntityReactorChamberElectric(BlockPos blockPos, BlockState blockState) {
        super(Ic2BlockEntities.REACTOR_CHAMBER, blockPos, blockState);
        this.fluids.addUnmanagedTankHook((Supplier<? extends Collection<Fluids.InternalFluidTank>>)new Supplier<Collection<Fluids.InternalFluidTank>>(){

            public Collection<Fluids.InternalFluidTank> get() {
                TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = TileEntityReactorChamberElectric.this.getReactor();
                if (tileEntityNuclearReactorElectric == null) {
                    return Collections.emptySet();
                }
                return Arrays.asList(tileEntityNuclearReactorElectric.inputTank, tileEntityNuclearReactorElectric.outputTank);
            }
        });
    }

    @Override
    protected void onLoaded() {
        super.onLoaded();
        this.updateRedstoneLink();
    }

    private void updateRedstoneLink() {
        if (this.getLevel().isClientSide) {
            return;
        }
        TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = this.getReactor();
        if (tileEntityNuclearReactorElectric != null) {
            this.redstone.linkTo(tileEntityNuclearReactorElectric.redstone);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void updateEntityClient() {
        super.updateEntityClient();
        TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = this.getReactor();
        if (tileEntityNuclearReactorElectric != null) {
            TileEntityNuclearReactorElectric.showHeatEffects(this.getLevel(), this.worldPosition, tileEntityNuclearReactorElectric.getHeat());
        }
    }

    @Override
    protected InteractionResult onActivated(Player player, InteractionHand interactionHand, Direction direction, Vec3 vec3) {
        TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = this.getReactor();
        if (tileEntityNuclearReactorElectric != null) {
            Level level = this.getLevel();
            return tileEntityNuclearReactorElectric.getBlockType().useWithoutItem(tileEntityNuclearReactorElectric.getBlockState(), level, tileEntityNuclearReactorElectric.getBlockPos(), player, new BlockHitResult(vec3, direction, tileEntityNuclearReactorElectric.getBlockPos(), false));
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void onNeighborChange(Block block, BlockPos blockPos) {
        super.onNeighborChange(block, blockPos);
        this.lastReactorUpdate = 0L;
        if (this.reactor == null) {
            this.destoryChamber(true);
        }
    }

    public void destoryChamber(boolean bl) {
        Level level = this.getLevel();
        level.removeBlock(this.worldPosition, false);
        for (ItemStack itemStack : this.getSelfDrops(0, bl)) {
            StackUtil.dropAsEntity(level, this.worldPosition, itemStack);
        }
    }

    public int getContainerSize() {
        TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = this.getReactor();
        return tileEntityNuclearReactorElectric != null ? tileEntityNuclearReactorElectric.getContainerSize() : 0;
    }

    public boolean isEmpty() {
        TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = this.getReactor();
        return tileEntityNuclearReactorElectric != null ? tileEntityNuclearReactorElectric.isEmpty() : true;
    }

    public ItemStack getItem(int n) {
        TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = this.getReactor();
        return tileEntityNuclearReactorElectric != null ? tileEntityNuclearReactorElectric.getItem(n) : null;
    }

    public ItemStack removeItem(int n, int n2) {
        TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = this.getReactor();
        return tileEntityNuclearReactorElectric != null ? tileEntityNuclearReactorElectric.removeItem(n, n2) : null;
    }

    public ItemStack removeItemNoUpdate(int n) {
        TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = this.getReactor();
        return tileEntityNuclearReactorElectric != null ? tileEntityNuclearReactorElectric.removeItemNoUpdate(n) : null;
    }

    public void setItem(int n, ItemStack itemStack) {
        TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = this.getReactor();
        if (tileEntityNuclearReactorElectric != null) {
            tileEntityNuclearReactorElectric.setItem(n, itemStack);
        }
    }

    public int getMaxStackSize() {
        TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = this.getReactor();
        return tileEntityNuclearReactorElectric != null ? tileEntityNuclearReactorElectric.getMaxStackSize() : 0;
    }

    public boolean stillValid(Player player) {
        TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = this.getReactor();
        return tileEntityNuclearReactorElectric != null ? tileEntityNuclearReactorElectric.stillValid(player) : false;
    }

    public void startOpen(Player player) {
        TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = this.getReactor();
        if (tileEntityNuclearReactorElectric != null) {
            tileEntityNuclearReactorElectric.startOpen(player);
        }
    }

    public void stopOpen(Player player) {
        TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = this.getReactor();
        if (tileEntityNuclearReactorElectric != null) {
            tileEntityNuclearReactorElectric.stopOpen(player);
        }
    }

    public boolean canPlaceItem(int n, ItemStack itemStack) {
        TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = this.getReactor();
        return tileEntityNuclearReactorElectric != null ? tileEntityNuclearReactorElectric.canPlaceItem(n, itemStack) : false;
    }

    public void clearContent() {
        TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = this.getReactor();
        if (tileEntityNuclearReactorElectric != null) {
            tileEntityNuclearReactorElectric.clearContent();
        }
    }

    @Override
    public boolean emitsEnergyTo(IEnergyAcceptor iEnergyAcceptor, Direction direction) {
        return true;
    }

    @Override
    public TileEntityNuclearReactorElectric getReactorInstance() {
        return this.reactor;
    }

    @Override
    public boolean isWall() {
        return false;
    }

    private TileEntityNuclearReactorElectric getReactor() {
        long l = this.getLevel().getGameTime();
        if (l != this.lastReactorUpdate) {
            this.updateReactor();
            this.lastReactorUpdate = l;
        } else if (this.reactor != null && this.reactor.isRemoved()) {
            this.reactor = null;
        }
        return this.reactor;
    }

    private void updateReactor() {
        Level level = this.getLevel();
        this.reactor = null;
        for (Direction direction : Util.ALL_DIRS) {
            BlockEntity blockEntity = level.getBlockEntity(this.worldPosition.relative(direction));
            if (!(blockEntity instanceof TileEntityNuclearReactorElectric)) continue;
            this.reactor = (TileEntityNuclearReactorElectric)blockEntity;
            break;
        }
    }
}

