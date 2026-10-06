/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.DistOnly
 */
package ic2.core.ref;
import net.neoforged.api.distmarker.OnlyIn;

import ic2.core.ref.ItemName;
import net.neoforged.api.distmarker.Dist;

public interface IItemModelProvider {
    @OnlyIn(Dist.CLIENT)
    public void registerModels(ItemName var1);
}

