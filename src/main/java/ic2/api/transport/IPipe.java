/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.core.Direction
 */
package ic2.api.transport;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;

public interface IPipe {
    public BlockEntity getTile();

    public boolean isConnected(Direction var1);

    public void flipConnection(Direction var1);
}

