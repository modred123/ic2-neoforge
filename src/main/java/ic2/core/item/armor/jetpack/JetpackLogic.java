/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.level.Level
 */
package ic2.core.item.armor.jetpack;

import ic2.core.IC2;
import ic2.core.audio.AudioSource;
import ic2.core.audio.PositionSpec;
import ic2.core.item.armor.jetpack.IBoostingJetpack;
import ic2.core.item.armor.jetpack.IJetpack;
import ic2.core.util.StackUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class JetpackLogic {
    private static boolean lastJetpackUsed;
    private static AudioSource audioSource;

    public static boolean useJetpack(Player player, boolean hoverMode, IJetpack jetpack, ItemStack stack) {
        int worldHeight;
        int maxFlightHeight;
        double y;
        if (jetpack.getChargeLevel(stack) <= 0.0) {
            return false;
        }
        IBoostingJetpack bjetpack = jetpack instanceof IBoostingJetpack ? (IBoostingJetpack)jetpack : null;
        float power = jetpack.getPower(stack);
        float dropPercentage = jetpack.getDropPercentage(stack);
        if (jetpack.getChargeLevel(stack) <= (double)dropPercentage) {
            power = (float)((double)power * (jetpack.getChargeLevel(stack) / (double)dropPercentage));
        }
        if (IC2.keyboard.isForwardKeyDown(player)) {
            float boost;
            float retruster;
            if (bjetpack != null) {
                retruster = bjetpack.getBaseThrust(stack, hoverMode);
                boost = bjetpack.getBoostThrust(player, stack, hoverMode);
            } else {
                retruster = hoverMode ? 1.0f : 0.15f;
                boost = 0.0f;
            }
            float forwardpower = power * retruster * 2.0f;
            if (forwardpower > 0.0f) {
                player.moveRelative(0.02f + boost, new net.minecraft.world.phys.Vec3(0.0, 0.0, 0.4f * forwardpower + boost));
                if (boost != 0.0f && !player.onGround()) {
                    bjetpack.useBoostPower(stack, boost);
                }
            }
        }
        if ((y = player.getY()) > (double)((maxFlightHeight = (int)((float)(worldHeight = player.level().getHeight()) / jetpack.getWorldHeightDivisor(stack))) - 25)) {
            if (y > (double)maxFlightHeight) {
                y = maxFlightHeight;
            }
            power = (float)((double)power * (((double)maxFlightHeight - y) / 25.0));
        }
        double prevmotion = player.getDeltaMovement().y;
        player.setDeltaMovement(player.getDeltaMovement().x, Math.min(player.getDeltaMovement().y + (double)(power * 0.2f), 0.6), player.getDeltaMovement().z);
        if (hoverMode) {
            float maxHoverY = 0.0f;
            if (IC2.keyboard.isJumpKeyDown(player)) {
                maxHoverY += jetpack.getHoverMultiplier(stack, true);
                if (bjetpack != null) {
                    maxHoverY *= bjetpack.getHoverBoost(player, stack, true);
                }
            }
            if (IC2.keyboard.isSneakKeyDown(player)) {
                maxHoverY += -jetpack.getHoverMultiplier(stack, false);
                if (bjetpack != null) {
                    maxHoverY *= bjetpack.getHoverBoost(player, stack, false);
                }
            }
            if (player.getDeltaMovement().y > (double)maxHoverY) {
                double newY = Math.min(maxHoverY, prevmotion);
                player.setDeltaMovement(player.getDeltaMovement().x, newY, player.getDeltaMovement().z);
            }
        }
        int consume = 2;
        if (hoverMode) {
            consume = 1;
        }
        if (!player.onGround()) {
            jetpack.drainEnergy(stack, consume);
        }
        player.resetFallDistance();
        // player.distanceWalkedModified 已移除
        IC2.platform.resetPlayerInAirTime(player);
        return true;
    }

    public static void onArmorTick(Level world, Player player, ItemStack stack, IJetpack jetpack) {
        if (stack == null || !jetpack.isJetpackActive(stack)) {
            return;
        }
        CompoundTag nbtData = JetpackLogic.getJetpackCompound(stack);
        boolean hoverMode = JetpackLogic.getHoverMode(nbtData);
        byte toggleTimer = nbtData.getByte("toggleTimer");
        boolean jetpackUsed = false;
        if (IC2.keyboard.isJumpKeyDown(player) && IC2.keyboard.isModeSwitchKeyDown(player) && toggleTimer == 0) {
            toggleTimer = 10;
            boolean bl = hoverMode = !hoverMode;
            if (IC2.platform.isSimulating()) {
                nbtData.putBoolean("hoverMode", hoverMode);
                if (hoverMode) {
                    IC2.platform.messagePlayer(player, "Hover Mode enabled.", new Object[0]);
                } else {
                    IC2.platform.messagePlayer(player, "Hover Mode disabled.", new Object[0]);
                }
            }
        }
        if (IC2.keyboard.isJumpKeyDown(player) || hoverMode) {
            jetpackUsed = JetpackLogic.useJetpack(player, hoverMode, jetpack, stack);
            if (player.onGround() && hoverMode && IC2.platform.isSimulating()) {
                JetpackLogic.setHoverMode(nbtData, false);
                IC2.platform.messagePlayer(player, "Hover Mode disabled.", new Object[0]);
            }
        }
        if (IC2.platform.isSimulating() && toggleTimer > 0) {
            toggleTimer = (byte)(toggleTimer - 1);
            nbtData.putByte("toggleTimer", toggleTimer);
        }
        if (IC2.platform.isRendering() && player == IC2.platform.getPlayerInstance()) {
            if (lastJetpackUsed != jetpackUsed) {
                if (jetpackUsed) {
                    if (audioSource == null) {
                        audioSource = IC2.audioManager.createSource(player, PositionSpec.Backpack, "Tools/Jetpack/JetpackLoop.ogg", true, false, IC2.audioManager.getDefaultVolume());
                    }
                    if (audioSource != null) {
                        audioSource.play();
                    }
                } else if (audioSource != null) {
                    audioSource.remove();
                    audioSource = null;
                }
                lastJetpackUsed = jetpackUsed;
            }
            if (audioSource != null) {
                audioSource.updatePosition();
            }
        }
        if (jetpackUsed) {
            player.inventoryMenu.broadcastChanges();
        }
    }

    private static void setHoverMode(CompoundTag nbt, boolean value) {
        nbt.putBoolean("hoverMode", value);
    }

    private static boolean getHoverMode(CompoundTag nbt) {
        return nbt.getBoolean("hoverMode");
    }

    private static CompoundTag getJetpackCompound(ItemStack stack) {
        return StackUtil.getOrCreateNbtData(stack);
    }
}

