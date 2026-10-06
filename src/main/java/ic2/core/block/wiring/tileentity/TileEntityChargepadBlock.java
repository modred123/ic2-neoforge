/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3f
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 */
package ic2.core.block.wiring.tileentity;

import org.joml.Vector3f;
import ic2.api.item.ElectricItem;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.block.wiring.ContainerChargepadBlock;
import ic2.core.block.wiring.tileentity.TileEntityElectricBlock;
import ic2.core.init.Localization;
import ic2.core.network.GrowingBuffer;
import ic2.core.ref.Ic2Items;
import ic2.core.util.Util;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public abstract class TileEntityChargepadBlock
extends TileEntityElectricBlock {
    private static final List<AABB> aabbs = Arrays.asList(new AABB(0.0, 0.0, 0.0, 1.0, 0.9375, 1.0));
    private static final DustParticleOptions effect = new DustParticleOptions(new Vector3f(0.2f, 0.2f, 1.0f), 1.0f);
    private int updateTicker;
    private Player player = null;
    public static byte redstoneModes = (byte)2;

    public TileEntityChargepadBlock(BlockEntityType<? extends TileEntityChargepadBlock> blockEntityType, BlockPos blockPos, BlockState blockState, int n, int n2, int n3) {
        super(blockEntityType, blockPos, blockState, n, n2, n3);
        this.energy.setDirections(EnumSet.complementOf(EnumSet.copyOf(Util.verticalFacings)), EnumSet.of(Direction.DOWN));
        this.updateTicker = IC2.random.nextInt(this.getTickRate());
    }

    @Override
    public void loadAdditional(CompoundTag compoundTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(compoundTag, registries);
        this.energy.setDirections(EnumSet.complementOf(EnumSet.of(this.getFacing(), Direction.UP)), EnumSet.of(this.getFacing()));
    }

    @Override
    protected List<AABB> getAabbs(boolean bl) {
        return aabbs;
    }

    @Override
    protected void onEntityCollision(Entity entity) {
        super.onEntityCollision(entity);
        if (!this.getLevel().isClientSide && entity instanceof Player) {
            this.updatePlayer((Player)entity);
        }
    }

    private void updatePlayer(Player player) {
        this.player = player;
    }

    protected int getTickRate() {
        return 2;
    }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean bl = false;
        if (this.updateTicker++ % this.getTickRate() != 0) {
            return;
        }
        if (this.player != null && this.energy.getEnergy() >= 1.0) {
            if (!this.getActive()) {
                this.setActive(true);
            }
            this.getItems(this.player);
            this.player = null;
            bl = true;
        } else if (this.getActive()) {
            this.setActive(false);
            bl = true;
        }
        if (bl) {
            this.setChanged();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void updateEntityClient() {
        super.updateEntityClient();
        Level level = this.getLevel();
        RandomSource randomSource = level.random;
        if (randomSource.nextInt(8) != 0) {
            return;
        }
        if (this.getActive()) {
            for (int i = 20; i > 0; --i) {
                double d = (float)this.worldPosition.getX() + 0.0f + randomSource.nextFloat();
                double d2 = (float)this.worldPosition.getY() + 0.9f + randomSource.nextFloat();
                double d3 = (float)this.worldPosition.getZ() + 0.0f + randomSource.nextFloat();
                level.addParticle((ParticleOptions)effect, d, d2, d3, 0.0, 0.1, 0.0);
            }
        }
    }

    protected abstract void getItems(Player var1);

    @Override
    protected boolean shouldEmitRedstone() {
        return this.redstoneMode == 0 && this.getActive() || this.redstoneMode == 1 && !this.getActive();
    }

    @Override
    protected void setFacing(Level level, Direction direction) {
        this.energy.setDirections(EnumSet.complementOf(EnumSet.of(direction, Direction.UP)), EnumSet.of(direction));
        this.superSetFacing(level, direction);
    }

    @Override
    public ContainerBase<?> createServerScreenHandler(int n, Player player) {
        return new ContainerChargepadBlock(n, player.getInventory(), this);
    }

    @Override
    public ContainerBase<?> createClientScreenHandler(int n, Inventory inventory, GrowingBuffer growingBuffer) {
        return new ContainerChargepadBlock(n, inventory, this);
    }

    @Override
    public void onNetworkEvent(Player player, int n) {
        this.redstoneMode = (byte)(this.redstoneMode + 1);
        if (this.redstoneMode >= redstoneModes) {
            this.redstoneMode = 0;
        }
        IC2.sideProxy.messagePlayer(player, this.getRedstoneMode(), new Object[0]);
    }

    @Override
    public String getRedstoneMode() {
        if (this.redstoneMode > 1 || this.redstoneMode < 0) {
            return "";
        }
        return Localization.translate("ic2.blockChargepad.gui.mod.redstone" + this.redstoneMode);
    }

    protected void chargeItem(ItemStack itemStack, int n) {
        if (itemStack.getItem() == Ic2Items.DEBUG_ITEM) {
            return;
        }
        double d = ElectricItem.manager.charge(itemStack, Double.POSITIVE_INFINITY, this.energy.getSourceTier(), true, true);
        double d2 = 0.0;
        if (d >= 0.0) {
            d2 = d >= (double)(n * this.getTickRate()) ? (double)(n * this.getTickRate()) : d;
            if (this.energy.getEnergy() < d2) {
                d2 = this.energy.getEnergy();
            }
            this.energy.useEnergy(ElectricItem.manager.charge(itemStack, d2, this.energy.getSourceTier(), true, false));
        }
    }
}

