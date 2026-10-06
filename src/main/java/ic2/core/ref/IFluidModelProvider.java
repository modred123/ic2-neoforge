/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.DistOnly
 */
package ic2.core.ref;
import net.neoforged.api.distmarker.OnlyIn;

import ic2.core.ref.FluidName;
import net.neoforged.api.distmarker.Dist;

public interface IFluidModelProvider {
    @OnlyIn(Dist.CLIENT)
    public void registerModels(FluidName var1);
}

