/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.resources.sounds.EntityBoundSoundInstance
 *  net.minecraft.client.sounds.SoundManager
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 */
package ic2.core.sound;

import ic2.core.IC2;
import ic2.core.sound.ListenableSoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public class EntityTrackingSoundInstance
extends EntityBoundSoundInstance
implements ListenableSoundInstance {
    protected Entity entity;
    protected SoundEvent soundEvent;
    protected SoundManager vanillaManager = Minecraft.getInstance().getSoundManager();
    private Runnable onFinish = null;

    public EntityTrackingSoundInstance(SoundEvent soundEvent, SoundSource soundSource, float f, float f2, Entity entity) {
        super(soundEvent, soundSource, f, f2, entity, IC2.random.nextLong());
        this.looping = true;
        this.entity = entity;
        this.soundEvent = soundEvent;
    }

    public void playOnce() {
        this.entity.playSound(this.soundEvent, this.volume, this.pitch);
    }

    @Override
    public void onFinish(Runnable runnable) {
        this.onFinish = this.onFinish == null ? runnable : () -> this.runOnFinish(runnable);
    }

    @Override
    public void finish() {
        if (this.onFinish != null) {
            this.onFinish.run();
        }
    }

    private void runOnFinish(Runnable runnable) {
        this.onFinish.run();
        runnable.run();
    }
}

