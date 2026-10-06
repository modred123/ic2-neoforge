package ic2.core.ref;

import ic2.core.ref.Ic2Items;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class Ic2ArmorMaterials {
    public static final ArmorMaterial BRONZE = Ic2ArmorMaterials.make("ic2_bronze", new int[]{2, 5, 6, 2}, 9, 0.0f, 0.0f, () -> Ic2Items.BRONZE_INGOT);
    public static final ArmorMaterial ALLOY = Ic2ArmorMaterials.make("ic2_alloy", new int[]{4, 7, 9, 4}, 12, 2.0f, 0.0f, () -> Ic2Items.ALLOY);
    public static final ArmorMaterial NANO_SUIT = Ic2ArmorMaterials.make("ic2_nano", new int[]{0, 0, 0, 0}, 0, 2.0f, 0.0f, () -> Items.AIR);
    public static final ArmorMaterial QUANTUM_SUIT = Ic2ArmorMaterials.make("ic2_quantum", new int[]{0, 0, 0, 0}, 0, 2.0f, 0.0f, () -> Items.AIR);
    public static final ArmorMaterial NIGHT_VISION_GOGGLES = Ic2ArmorMaterials.make("ic2_night_vision", new int[]{3, 0, 0, 0}, 0, 2.0f, 0.0f, () -> Items.AIR);
    public static final ArmorMaterial HAZMAT = Ic2ArmorMaterials.make("ic2_hazmat", new int[]{0, 0, 0, 0}, 0, 0.0f, 0.0f, () -> Items.AIR);
    public static final ArmorMaterial CF_PACK = Ic2ArmorMaterials.make("ic2_cf_pack", new int[]{0, 0, 0, 0}, 0, 0.0f, 0.0f, () -> Items.AIR);
    public static final ArmorMaterial JET_PACK = Ic2ArmorMaterials.make("ic2_jet_pack", new int[]{0, 0, 0, 0}, 0, 0.0f, 0.0f, () -> Items.AIR);

    private static ArmorMaterial make(String name, int[] protectionAmounts, int enchantability, float toughness, float knockbackResistance, Supplier<Item> repairIngredient) {
        Map<ArmorItem.Type, Integer> defense = Map.of(
            ArmorItem.Type.HELMET, protectionAmounts[3],
            ArmorItem.Type.CHESTPLATE, protectionAmounts[2],
            ArmorItem.Type.LEGGINGS, protectionAmounts[1],
            ArmorItem.Type.BOOTS, protectionAmounts[0]
        );
        java.util.function.Supplier<net.minecraft.world.item.crafting.Ingredient> repairIngredientSupplier = () -> net.minecraft.world.item.crafting.Ingredient.of(repairIngredient.get());
        return new ArmorMaterial(defense, enchantability, SoundEvents.ARMOR_EQUIP_IRON, repairIngredientSupplier, List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath("ic2", name))), toughness, knockbackResistance);
    }
}
