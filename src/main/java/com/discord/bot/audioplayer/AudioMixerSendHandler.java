package com.discord.bot.audioplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.nio.ByteBuffer;

public class AudioMixerSendHandler implements AudioSendHandler {
    private final AudioPlayer musicPlayer;
    private final AudioPlayer sfxPlayer;
    private final ByteBuffer buffer;

    public AudioMixerSendHandler(AudioPlayer musicPlayer, AudioPlayer sfxPlayer) {
        this.musicPlayer = musicPlayer;
        this.sfxPlayer = sfxPlayer;
        this.buffer = ByteBuffer.allocate(1024);
    }

    @Override
    public boolean canProvide() {
        return true;
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        AudioFrame frameMusic = musicPlayer.provide();
        AudioFrame frameSfx = sfxPlayer.provide();
        
        byte[] dataMusic = frameMusic != null ? frameMusic.getData() : new byte[0];
        byte[] dataSfx = frameSfx != null ? frameSfx.getData() : new byte[0];

        byte[] mixedData = mixAudio(dataMusic, dataSfx);

        buffer.clear();
        buffer.put(mixedData);
        buffer.flip();

        return buffer;
    }

    @Override
    public boolean isOpus() {
        return true;
    }

    private byte[] mixAudio(byte[] audio1, byte[] audio2) {
        // TODO: Implement mixing algorithm

        if (audio2.length == 0) {
            return audio1;
        } else {
            return audio2;
        }
    }
}
