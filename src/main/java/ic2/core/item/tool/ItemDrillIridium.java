/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Tier
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 *  net.minecraft.world.item.enchantment.Enchantments
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 */
package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.item.tool.ItemDrill;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;
import java.util.IdentityHashMap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;

@NotClassic
public class ItemDrillIridium
extends ItemDrill {
    private static final Tier IRIDIUM_TOOL_MATERIAL = new Tier(){

        public int getUses() {
            return 3000;
        }

        public float getSpeed() {
            return 15.0f;
        }

        public float getAttackDamageBonus() {
            return 5.0f;
        }

        public net.minecraft.tags.TagKey<Block> getIncorrectBlocksForDrops() {
            return net.minecraft.tags.BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
        }

        public int getEnchantmentValue() {
            return 20;
        }

        public Ingredient getRepairIngredient() {
            return Ingredient.of(new ItemLike[]{Ic2Items.IRIDIUM});
        }
    };

    public ItemDrillIridium(Item.Properties properties) {
        super(properties, 800, IRIDIUM_TOOL_MATERIAL, 300000, 1000, 3, 24.0f);
    }

    @Override
    protected ItemStack getItemStack(double d) {
        ItemStack itemStack = super.getItemStack(d);
        itemStack.enchant(IC2.getRegistryAccess().holderOrThrow(Enchantments.FORTUNE), 3);
        return itemStack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        if (!level.isClientSide && IC2.keyboard.isModeSwitchKeyDown(player)) {
            ItemStack itemStack = StackUtil.get(player, interactionHand);
            if (EnchantmentHelper.getItemEnchantmentLevel(IC2.getRegistryAccess().holderOrThrow(Enchantments.SILK_TOUCH), itemStack) == 0) {
                itemStack.enchant(IC2.getRegistryAccess().holderOrThrow(Enchantments.SILK_TOUCH), 1);
                IC2.sideProxy.messagePlayer(player, "ic2.tooltip.mode", "ic2.tooltip.mode.silkTouch");
            } else {
                IC2.sideProxy.messagePlayer(player, "ic2.tooltip.mode", "ic2.tooltip.mode.normal");
            }
        }
        return super.use(level, player, interactionHand);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        if (IC2.keyboard.isModeSwitchKeyDown(useOnContext.getPlayer())) {
            return InteractionResult.PASS;
        }
        return super.useOn(useOnContext);
    }
}

