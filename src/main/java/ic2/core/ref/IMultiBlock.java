/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.state.BlockState
 */
package ic2.core.ref;

import ic2.core.block.state.IIdProvider;
import ic2.core.ref.IMultiItem;
import net.minecraft.world.level.block.state.BlockState;

public interface IMultiBlock<T extends IIdProvider>
extends IMultiItem<T> {
    public BlockState getState(T var1);

    public BlockState getState(String var1);
}

