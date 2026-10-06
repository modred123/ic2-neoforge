/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtUtils
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.entity.SkullBlockEntity
 *  org.apache.commons.lang3.StringUtils
 */
package ic2.core.gui;

import com.mojang.authlib.GameProfile;
import ic2.core.Ic2Gui;
import ic2.core.gui.ItemImage;
import ic2.core.util.StackUtil;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.apache.commons.lang3.StringUtils;

public class PlayerHead
extends ItemImage {
    private static final Map<GameProfile, ItemStack> IMAGE_MAKER = Collections.synchronizedMap(new WeakHashMap());
    private final GameProfile player;

    public PlayerHead(Ic2Gui<?> ic2Gui, int n, int n2, GameProfile gameProfile) {
        super(ic2Gui, n, n2, new PlayerHeadSupplier(gameProfile));
        this.player = gameProfile;
    }

    @Override
    protected List<Component> getToolTip() {
        List<Component> list = super.getToolTip();
        if (StringUtils.isNotBlank((CharSequence)this.player.getName())) {
            list.add((Component)Component.literal((String)this.player.getName()));
        }
        return list;
    }

    private static final class PlayerHeadSupplier
    implements Supplier<ItemStack> {
        private final GameProfile profile;

        PlayerHeadSupplier(GameProfile gameProfile) {
            this.profile = gameProfile;
        }

        @Override
        public ItemStack get() {
            CompletableFuture<java.util.Optional<GameProfile>> completableFuture = SkullBlockEntity.fetchGameProfile(this.profile.getId());
            try {
                return IMAGE_MAKER.computeIfAbsent(completableFuture.get().orElse(null), gameProfile -> { ItemStack itemStack = new ItemStack(Items.PLAYER_HEAD); CompoundTag profileTag = new CompoundTag(); profileTag.putUUID("Id", gameProfile.getId()); profileTag.putString("Name", gameProfile.getName()); StackUtil.getOrCreateNbtData(itemStack).put("SkullOwner", profileTag); return itemStack; });
            }
            catch (InterruptedException | ExecutionException exception) {
                throw new RuntimeException(exception);
            }
        }

    }
}

