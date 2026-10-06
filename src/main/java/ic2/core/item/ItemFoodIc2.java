package ic2.core.item;

import ic2.core.IC2;
import ic2.core.init.Localization;
import ic2.core.item.ItemIC2;
import ic2.core.ref.IItemModelProvider;
import ic2.core.ref.ItemName;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ItemFoodIc2
extends Item
implements IItemModelProvider {
    protected ItemFoodIc2(ItemName name, int amount, float saturation, boolean isWolfFood) {
        super(new Item.Properties().food(new FoodProperties.Builder().nutrition(amount).saturationModifier(saturation).build()));
        ic2.core.init.BlocksItems.registerItem(this, IC2.getIdentifier(name.name()));
        name.setInstance(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerModels(ItemName name) {
        ItemIC2.registerModel((Item)this, 0, name, null);
    }

    public String getTranslationKey() {
        return "ic2." + super.getDescriptionId().substring(5);
    }

    public String getTranslationKey(ItemStack stack) {
        return this.getTranslationKey();
    }

    public String getUnlocalizedNameInefficiently(ItemStack stack) {
        return this.getTranslationKey(stack);
    }

    public String getItemStackDisplayName(ItemStack stack) {
        return Localization.translate(this.getTranslationKey(stack));
    }
}
