package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.init.Localization;
import ic2.core.item.ItemIC2;
import ic2.core.ref.IItemModelProvider;
import ic2.core.ref.ItemName;
import ic2.core.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class Ic2Sword
extends SwordItem
implements IItemModelProvider {
    public int weaponDamage = 7;
    private final Object repairMaterial = "ingotBronze";

    public Ic2Sword(Tier material) {
        super(material, new Item.Properties());
        ic2.core.init.BlocksItems.registerItem(this, IC2.getIdentifier(ItemName.bronze_sword.name()));
        ItemName.bronze_sword.setInstance(this);
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

    public boolean getIsRepairable(ItemStack stack1, ItemStack stack2) {
        return stack2 != null && Util.matchesOD(stack2, (String)this.repairMaterial);
    }
}
