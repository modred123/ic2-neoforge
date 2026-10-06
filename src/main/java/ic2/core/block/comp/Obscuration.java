/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 */
package ic2.core.block.comp;

import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.state.BlockStateUtil;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.item.tool.ItemObscurator;
import ic2.core.util.Util;
import java.util.Arrays;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class Obscuration
extends TileEntityComponent {
    private final Runnable changeHandler;
    private ObscurationData[] dataMap;

    public Obscuration(Ic2TileEntity ic2TileEntity, Runnable runnable) {
        super(ic2TileEntity);
        this.changeHandler = runnable;
    }

    @Override
    public void readFromNbt(CompoundTag compoundTag) {
        if (compoundTag.isEmpty()) {
            return;
        }
        for (Direction direction : Util.ALL_DIRS) {
            byte by;
            String string;
            BlockState blockState;
            CompoundTag compoundTag2;
            Block block;
            if (!compoundTag.contains(direction.getSerializedName(), 10) || (block = Util.getBlock((compoundTag2 = compoundTag.getCompound(direction.getSerializedName())).getString("block"))) == null || (blockState = BlockStateUtil.getState(block, string = compoundTag2.getString("variant"))) == null || (by = compoundTag2.getByte("side")) < 0 || by >= Util.ALL_DIRS.length) continue;
            Direction direction2 = Util.ALL_DIRS[by];
            int[] nArray = ItemObscurator.internColorMultipliers(compoundTag2.getIntArray("colorMuls"));
            ObscurationData obscurationData = new ObscurationData(blockState, string, direction2, nArray);
            if (this.dataMap == null) {
                this.dataMap = new ObscurationData[Util.ALL_DIRS.length];
            }
            this.dataMap[direction.ordinal()] = obscurationData.intern();
        }
    }

    @Override
    public CompoundTag writeToNbt() {
        if (this.dataMap == null) {
            return null;
        }
        CompoundTag compoundTag = new CompoundTag();
        for (Direction direction : Util.ALL_DIRS) {
            ObscurationData obscurationData = this.dataMap[direction.ordinal()];
            if (obscurationData == null) continue;
            CompoundTag compoundTag2 = new CompoundTag();
            compoundTag2.putString("block", Util.getName(obscurationData.state.getBlock()).toString());
            compoundTag2.putString("variant", obscurationData.variant);
            compoundTag2.putByte("side", (byte)obscurationData.side.ordinal());
            compoundTag2.putIntArray("colorMuls", obscurationData.colorMultipliers);
            compoundTag.put(direction.getSerializedName(), (Tag)compoundTag2);
        }
        return compoundTag;
    }

    public boolean applyObscuration(Direction direction, ObscurationData obscurationData) {
        if (this.dataMap != null && obscurationData.equals(this.dataMap[direction.ordinal()])) {
            return false;
        }
        if (this.dataMap == null) {
            this.dataMap = new ObscurationData[Util.ALL_DIRS.length];
        }
        this.dataMap[direction.ordinal()] = obscurationData.intern();
        this.changeHandler.run();
        return true;
    }

    public void clear() {
        this.dataMap = null;
        this.changeHandler.run();
    }

    public boolean hasObscuration() {
        return this.dataMap != null;
    }

    public ObscurationData[] getRenderState() {
        if (this.dataMap == null) {
            return null;
        }
        return Arrays.copyOf(this.dataMap, this.dataMap.length);
    }

    public static class ObscurationData {
        public final BlockState state;
        public final String variant;
        public final Direction side;
        public final int[] colorMultipliers;

        public ObscurationData(BlockState blockState, String string, Direction direction, int[] nArray) {
            this.state = blockState;
            this.variant = string;
            this.side = direction;
            this.colorMultipliers = nArray;
        }

        public boolean equals(Object object) {
            if (object == this) {
                return true;
            }
            if (!(object instanceof ObscurationData)) {
                return false;
            }
            ObscurationData obscurationData = (ObscurationData)object;
            return obscurationData.state.equals(this.state) && obscurationData.variant.equals(this.variant) && obscurationData.side == this.side && Arrays.equals(obscurationData.colorMultipliers, this.colorMultipliers);
        }

        public int hashCode() {
            return (this.state.hashCode() * 7 + this.side.ordinal()) * 23;
        }

        public ObscurationData intern() {
            return this;
        }
    }
}

