/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 */
package ic2.api.crops;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public enum CropSoilType {
    FARMLAND(Blocks.FARMLAND),
    MYCELIUM(Blocks.MYCELIUM),
    SAND(Blocks.SAND),
    SOULSAND(Blocks.SOUL_SAND);

    private final Block block;

    private CropSoilType(Block block) {
        if (block == null) {
            throw new NullPointerException("null block");
        }
        this.block = block;
    }

    public Block getBlock() {
        return this.block;
    }

    public static boolean contains(Block block) {
        for (CropSoilType cropSoilType : CropSoilType.values()) {
            if (cropSoilType.getBlock() != block) continue;
            return true;
        }
        return false;
    }
}

