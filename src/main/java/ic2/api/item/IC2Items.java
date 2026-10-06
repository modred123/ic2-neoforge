/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.fml.common.Loader
 *  net.minecraftforge.fml.common.ModContainer
 */
package ic2.api.item;

import ic2.api.info.Info;
import ic2.api.item.IItemAPI;
import net.minecraft.world.item.ItemStack;

public final class IC2Items {
    private static IItemAPI instance;

    public static ItemStack getItem(String name, String variant) {
        if (instance == null) {
            return null;
        }
        return instance.getItemStack(name, variant);
    }

    public static ItemStack getItem(String name) {
        return IC2Items.getItem(name, null);
    }

    public static IItemAPI getItemAPI() {
        return instance;
    }

    public static void setInstance(IItemAPI api) {
        instance = api;
    }
}

