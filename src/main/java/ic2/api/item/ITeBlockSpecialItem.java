/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.block.model.ModelResourceLocation
 *  net.minecraft.world.item.ItemStack
 */
package ic2.api.item;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface ITeBlockSpecialItem {
    public boolean doesOverrideDefault(ItemStack var1);

    public ModelResourceLocation getModelLocation(ItemStack var1);
}

