/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Tier
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.level.ItemLike
 */
package ic2.core.ref;

import ic2.core.ref.Ic2Items;
import java.util.function.Supplier;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public enum Ic2ToolMaterials implements Tier
{
    BRONZE(2, 350, 6.0f, 2.0f, 14, (Supplier<Ingredient>)Ic2ToolMaterials::lambda$static$0),
    CHAINSAW(3, 250, 12.0f, 9.0f, 14, (Supplier<Ingredient>)Ingredient::of);

    private final int miningLevel;
    private final int itemDurability;
    private final float miningSpeed;
    private final float attackDamage;
    private final int enchantability;
    private final Supplier<Ingredient> repairIngredient;

    private Ic2ToolMaterials(int n2, int n3, float f, float f2, int n4, Supplier<Ingredient> supplier) {
        this.miningLevel = n2;
        this.itemDurability = n3;
        this.miningSpeed = f;
        this.attackDamage = f2;
        this.enchantability = n4;
        this.repairIngredient = supplier;
    }

    public int getUses() {
        return this.itemDurability;
    }

    public float getSpeed() {
        return this.miningSpeed;
    }

    public float getAttackDamageBonus() {
        return this.attackDamage;
    }

    public int getLevel() {
        return this.miningLevel;
    }

    public int getEnchantmentValue() {
        return this.enchantability;
    }

    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    public net.minecraft.tags.TagKey<Block> getIncorrectBlocksForDrops() {
        return this.miningLevel >= 3 ? net.minecraft.tags.BlockTags.INCORRECT_FOR_NETHERITE_TOOL : net.minecraft.tags.BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
    }

    private static /* synthetic */ Ingredient lambda$static$0() {
        return Ingredient.of((ItemLike[])new ItemLike[]{Ic2Items.BRONZE_INGOT});
    }
}

