/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.client.sounds.SoundManager
 *  net.minecraft.core.BlockPos
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package ic2.core.sound;

import ic2.core.IHitSoundOverride;
import ic2.core.proxy.SideProxyClient;
import ic2.core.sound.EntityTrackingSoundInstance;
import ic2.core.sound.Sound;
import ic2.core.sound.SoundClient;
import ic2.core.sound.SoundManager;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SoundManagerClient
extends SoundManager {
    public net.minecraft.client.sounds.SoundManager vanillaManager;
    private final Map<WeakObject, List<SoundClient>> objectToSoundMap = new HashMap<WeakObject, List<SoundClient>>();

    public SoundManagerClient() {
        // 1.21 迁移修复：本构造器在 IC2/SideProxyClient 静态初始化期执行，Minecraft 实例
        // 可能尚未创建（getInstance() 返回 null）→ 缓存 null 会导致后续全部音频调用 NPE。
        // 改为实时获取 + 各处判空。
    }

    private net.minecraft.client.sounds.SoundManager vanilla() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft == null ? null : minecraft.getSoundManager();
    }

    @Override
    public Sound createSound(Object object, SoundEvent soundEvent, SoundSource soundSource, LivingEntity livingEntity, float f, float f2) {
        super.createSound(object, soundEvent, soundSource, livingEntity, f, f2);
        SoundClient soundClient = new SoundClient(soundEvent, soundSource, livingEntity, f, f2);
        this.objectToSoundMap.computeIfAbsent(new WeakObject(object), weakObject -> new ArrayList()).add(soundClient);
        return soundClient;
    }

    @Override
    public Sound createSound(Object object, SoundEvent soundEvent, SoundSource soundSource, BlockPos blockPos, float f, float f2) {
        super.createSound(object, soundEvent, soundSource, blockPos, f, f2);
        SoundClient soundClient = new SoundClient(soundEvent, soundSource, blockPos, f, f2);
        this.objectToSoundMap.computeIfAbsent(new WeakObject(object), weakObject -> new ArrayList()).add(soundClient);
        return soundClient;
    }

    @Override
    public void playOnce(SoundEvent soundEvent, SoundSource soundSource, float f, float f2, LivingEntity livingEntity) {
        super.playOnce(soundEvent, soundSource, f, f2, livingEntity);
        new EntityTrackingSoundInstance(soundEvent, soundSource, f, f2, (Entity)livingEntity).playOnce();
    }

    @Override
    public void pauseAll() {
        super.pauseAll();
        net.minecraft.client.sounds.SoundManager sm = vanilla();
        if (sm != null) {
            this.vanillaManager = sm;
            sm.pause();
        }
    }

    @Override
    public void resumeAll() {
        super.resumeAll();
        net.minecraft.client.sounds.SoundManager sm = vanilla();
        if (sm != null) {
            this.vanillaManager = sm;
            sm.resume();
        }
    }

    @Override
    public void stopAll() {
        super.stopAll();
        net.minecraft.client.sounds.SoundManager sm = vanilla();
        if (sm != null) {
            this.vanillaManager = sm;
            sm.stop();
        }
    }

    @Override
    public WeakObject stopAll(Object object) {
        super.stopAll(object);
        WeakObject weakObject = new WeakObject(object);
        if (!this.objectToSoundMap.containsKey(weakObject)) {
            return null;
        }
        this.objectToSoundMap.get(weakObject).forEach((Consumer<SoundClient>)SoundClient::stop);
        return weakObject;
    }

    @Override
    public void removeAllSound(Object object) {
        super.removeAllSound(object);
        WeakObject weakObject = this.stopAll(object);
        if (weakObject == null) {
            return;
        }
        this.objectToSoundMap.remove(weakObject);
    }

    @Override
    public void removeSound(Object object, Sound sound) {
        super.removeSound(object, sound);
        sound.stop();
        WeakObject weakObject = new WeakObject(object);
        if (!this.objectToSoundMap.containsKey(weakObject)) {
            return;
        }
        this.objectToSoundMap.get(weakObject).remove(sound);
    }

    @Override
    public void tick() {
        super.tick();
        this.objectToSoundMap.forEach((weakObject, list) -> list.forEach(SoundClient::tick));
    }

    public static SoundInstance onSoundPlayed(SoundInstance soundInstance) {
        SoundSource soundSource = soundInstance.getSource();
        String string = soundInstance.getLocation().getPath();
        if (soundSource == SoundSource.BLOCKS && string.endsWith(".hit") || soundSource == SoundSource.BLOCKS && string.endsWith(".break")) {
            Item item;
            LocalPlayer localPlayer = Minecraft.getInstance().player;
            ItemStack itemStack = null;
            if (localPlayer != null) {
                itemStack = localPlayer.getInventory().getSelected();
            }
            if (itemStack != null && (item = itemStack.getItem()) instanceof IHitSoundOverride) {
                SoundEvent soundEvent;
                IHitSoundOverride iHitSoundOverride = (IHitSoundOverride)item;
                Level level = localPlayer.getCommandSenderWorld();
                BlockHitResult blockHitResult = SoundManagerClient.getMovingObjectPositionFromPlayer(level, localPlayer, false);
                BlockPos blockPos = BlockPos.containing(soundInstance.getX(), soundInstance.getY(), soundInstance.getZ());
                if (blockHitResult != null && blockHitResult.getType() == HitResult.Type.BLOCK && blockPos.equals((Object)blockHitResult.getBlockPos()) && (soundEvent = string.endsWith(".hit") ? iHitSoundOverride.getHitSoundForBlock(localPlayer, level, blockPos, itemStack) : iHitSoundOverride.getBreakSoundForBlock(localPlayer, level, blockPos, itemStack)) != null) {
                    soundInstance = null;
                    level.playSound(localPlayer, blockPos, soundEvent, soundSource, 1.0f, 1.0f);
                }
            }
        }
        return soundInstance;
    }

    private static BlockHitResult getMovingObjectPositionFromPlayer(Level level, Player player, boolean bl) {
        float f = player.getXRot();
        float f2 = player.getYRot();
        double d = player.getX();
        double d2 = player.getY() + (double)player.getEyeHeight(player.getPose());
        double d3 = player.getZ();
        Vec3 vec3 = new Vec3(d, d2, d3);
        float f3 = Mth.cos((float)(-f2 * ((float)Math.PI / 180) - (float)Math.PI));
        float f4 = Mth.sin((float)(-f2 * ((float)Math.PI / 180) - (float)Math.PI));
        float f5 = -Mth.cos((float)(-f * ((float)Math.PI / 180)));
        float f6 = Mth.sin((float)(-f * ((float)Math.PI / 180)));
        float f7 = f4 * f5;
        float f8 = f3 * f5;
        double d4 = 5.0;
        Vec3 vec32 = vec3.add((double)f7 * d4, (double)f6 * d4, (double)f8 * d4);
        return level.clip(new ClipContext(vec3, vec32, ClipContext.Block.OUTLINE, bl ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, (Entity)player));
    }


    public static class WeakObject
    extends WeakReference<Object> {
        public WeakObject(Object object) {
            super(object);
        }

        public boolean equals(Object object) {
            if (object instanceof WeakObject) {
                return ((WeakObject)object).get() == this.get();
            }
            return this.get() == object;
        }

        public int hashCode() {
            Object t = this.get();
            if (t == null) {
                return 0;
            }
            return t.hashCode();
        }
    }
}

