/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.Container
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.Vec3
 */
package ic2.core.block.reactor.tileentity;

import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;
import ic2.core.block.reactor.tileentity.TileEntityReactorVessel;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@NotClassic
public class TileEntityReactorAccessHatch
extends TileEntityReactorVessel
implements Container {
    public TileEntityReactorAccessHatch(BlockPos blockPos, BlockState blockState) {
        super((BlockEntityType<? extends TileEntityReactorVessel>)Ic2BlockEntities.REACTOR_ACCESS_HATCH, blockPos, blockState);
    }

    @Override
    protected InteractionResult onActivated(Player player, InteractionHand interactionHand, Direction direction, Vec3 vec3) {
        TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = this.getReactorInstance();
        if (tileEntityNuclearReactorElectric != null) {
            return tileEntityNuclearReactorElectric.onActivated(player, interactionHand, direction, vec3);
        }
        return InteractionResult.PASS;
    }

    public int getContainerSize() {
        TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = this.getReactorInstance();
        return tileEntityNuclearReactorElectric != null ? tileEntityNuclearReactorElectric.getContainerSize() : 0;
    }

    public boolean isEmpty() {
        TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = this.getReactorInstance();
        return tileEntityNuclearReactorElectric != null ? tileEntityNuclearReactorElectric.isEmpty() : true;
    }

    public ItemStack getItem(int n) {
        TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = this.getReactorInstance();
        return tileEntityNuclearReactorElectric != null ? tileEntityNuclearReactorElectric.getItem(n) : null;
    }

    public ItemStack removeItem(int n, int n2) {
        TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = this.getReactorInstance();
        return tileEntityNuclearReactorElectric != null ? tileEntityNuclearReactorElectric.removeItem(n, n2) : null;
    }

    public ItemStack removeItemNoUpdate(int n) {
        TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = this.getReactorInstance();
        return tileEntityNuclearReactorElectric != null ? tileEntityNuclearReactorElectric.removeItemNoUpdate(n) : null;
    }

    public void setItem(int n, ItemStack itemStack) {
        TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = this.getReactorInstance();
        if (tileEntityNuclearReactorElectric != null) {
            tileEntityNuclearReactorElectric.setItem(n, itemStack);
        }
    }

    public int getMaxStackSize() {
        TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = this.getReactorInstance();
        return tileEntityNuclearReactorElectric != null ? tileEntityNuclearReactorElectric.getMaxStackSize() : 0;
    }

    public boolean stillValid(Player player) {
        TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = this.getReactorInstance();
        return tileEntityNuclearReactorElectric != null ? tileEntityNuclearReactorElectric.stillValid(player) : false;
    }

    public void startOpen(Player player) {
        TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = this.getReactorInstance();
        if (tileEntityNuclearReactorElectric != null) {
            tileEntityNuclearReactorElectric.startOpen(player);
        }
    }

    public void stopOpen(Player player) {
        TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = this.getReactorInstance();
        if (tileEntityNuclearReactorElectric != null) {
            tileEntityNuclearReactorElectric.stopOpen(player);
        }
    }

    public boolean canPlaceItem(int n, ItemStack itemStack) {
        TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = this.getReactorInstance();
        return tileEntityNuclearReactorElectric != null ? tileEntityNuclearReactorElectric.canPlaceItem(n, itemStack) : false;
    }

    public void clearContent() {
        TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = this.getReactorInstance();
        if (tileEntityNuclearReactorElectric != null) {
            tileEntityNuclearReactorElectric.clearContent();
        }
    }
}

