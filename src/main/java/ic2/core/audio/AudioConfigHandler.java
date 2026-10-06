/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.components.AbstractSliderButton
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.OptionsScreen
 *  net.minecraft.client.resources.language.I18n
 *  net.minecraft.network.chat.CommonComponents
 *  net.minecraft.network.chat.Component
 */
package ic2.core.audio;

import ic2.core.IC2;
import ic2.core.audio.AudioManagerClient;
import ic2.core.init.MainConfig;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class AudioConfigHandler {
    public static void onGuiCreate(Screen screen, Consumer<GuiEventListener> consumer) {
        if (!(screen instanceof SoundOptionsScreen)) {
            return;
        }
        final AudioManagerClient audioManagerClient = (AudioManagerClient)IC2.sideProxy.getAudioManager();
        if (!audioManagerClient.enabled) {
            return;
        }
        int n = 11;
        consumer.accept((GuiEventListener)new AbstractSliderButton(screen.width / 2 - 155 + n % 2 * 160, screen.height / 6 - 12 + 24 * (n >> 1), 150, 20, CommonComponents.EMPTY, audioManagerClient.getMasterVolume()){
            @Override
            protected void updateMessage() {
                String string = this.value <= 0.0 ? I18n.get((String)"options.off", (Object[])new Object[0]) : (int)(this.value * 100.0) + "%";
                this.setMessage((Component)Component.translatable((String)"ic2.tooltip.sound", (Object[])new Object[]{string}));
            }

            protected void applyValue() {
                audioManagerClient.masterVolume = (float)this.value;
                MainConfig.get().set("audio/volume", String.format("%.2f", this.value));
                MainConfig.save();
            }
        });
    }
}

