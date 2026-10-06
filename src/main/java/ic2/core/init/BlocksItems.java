/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffectCategory
 */
package ic2.core.init;

import ic2.core.IC2;
import ic2.core.Ic2Potion;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Fluids;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2ScreenHandlers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class BlocksItems {
    public static void init() {
        BlocksItems.initPotions();
        Ic2Fluids.init();
        Ic2Blocks.init();
        Ic2BlockEntities.init();
        Ic2Items.init();
        Ic2ScreenHandlers.init();
        BlocksItems.initMigration();
    }

    private static void initPotions() {
        Ic2Potion.radiation = new Ic2Potion(MobEffectCategory.HARMFUL, 5149489);
        IC2.envProxy.registerStatusEffect(IC2.getIdentifier("radiation"), Ic2Potion.radiation);
    }

    /** ex112 兼容：物品注册（缓冲到 pendingRegistrations，RegisterEvent 时 flush） */
    public static <T extends Item> T registerItem(T item, ResourceLocation rl) {
        IC2.envProxy.registerItem(rl, item);
        return item;
    }

    public static <T extends Item> T registerItem(T item) {
        return item;
    }

    public static <T extends Block> T registerBlock(T block, ResourceLocation rl) {
        IC2.envProxy.registerBlock(rl, block);
        return block;
    }

    public static <T extends Block> T registerBlock(T block) {
        return block;
    }

    private static void initMigration() {
    }
}

