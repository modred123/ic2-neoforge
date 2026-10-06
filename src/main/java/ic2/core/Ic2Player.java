/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Charsets
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.ServerLevel
 *  net.minecraftforge.common.util.FakePlayerFactory
 */
package ic2.core;

import com.google.common.base.Charsets;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

public class Ic2Player {
    public static Player get(Level world) {
        if (world instanceof ServerLevel) {
            return FakePlayerFactory.get((ServerLevel)world, Ic2Player.getGameProfile(world.dimension()));
        }
        return null;
    }

    private static GameProfile getGameProfile(ResourceKey<Level> dimension) {
        String name = "[IC2 " + dimension.location() + "]";
        UUID uuid = UUID.nameUUIDFromBytes(name.getBytes(Charsets.UTF_8));
        return new GameProfile(uuid, name);
    }
}

