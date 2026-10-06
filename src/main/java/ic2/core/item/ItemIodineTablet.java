package ic2.core.item;

import ic2.core.IC2;
import ic2.core.Ic2Potion;
import ic2.core.item.ItemIC2;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@NotClassic
public class ItemIodineTablet
extends ItemIC2 {
    public ItemIodineTablet() {
        super(ItemName.iodine_tablet);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = StackUtil.get(player, hand);
        if (!world.isClientSide) {
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    public InteractionResult onEaten(Player player, ItemStack stack) {
        Holder<MobEffect> radiationHolder = Ic2Potion.radiation.getHolder(); // 第三十二轮：必须用注册表 Holder，否则查不到
        MobEffectInstance radiation = player.getEffect(radiationHolder);
        if (radiation == null) {
            return InteractionResult.PASS;
        }
        int duration = radiation.getDuration() / 20;
        int amount = Math.min(StackUtil.getSize(stack), duration);
        if (amount <= 0) {
            return InteractionResult.PASS;
        }
        player.removeEffect(radiationHolder);
        if (amount < duration) {
            player.addEffect(new MobEffectInstance(radiationHolder, (duration - amount) * 20));
        }
        stack = StackUtil.decSize(stack, amount);
        IC2.platform.playSoundSp("Tools/eat.ogg", 1.0f, 1.0f);
        return InteractionResult.SUCCESS;
    }
}
