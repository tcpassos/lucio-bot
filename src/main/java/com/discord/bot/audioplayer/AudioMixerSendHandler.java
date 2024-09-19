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

        byte[] dataMusic = frameMusic != null ? frameMusic.getData() : new byte[1024];
        byte[] dataSfx = frameSfx != null ? frameSfx.getData() : new byte[1024];

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
        ByteBuffer buffer1 = ByteBuffer.wrap(audio1);
        ByteBuffer buffer2 = ByteBuffer.wrap(audio2);
        ByteBuffer mixedBuffer = ByteBuffer.allocate(1024);

        while (mixedBuffer.remaining() >= 2) {
            short sample1 = buffer1.remaining() >= 2 ? buffer1.getShort() : 0;
            short sample2 = buffer2.remaining() >= 2 ? buffer2.getShort() : 0;

            int mixedSample = sample1 + sample2;

            // Evita saturação
            if (mixedSample > Short.MAX_VALUE) {
                mixedSample = Short.MAX_VALUE;
            } else if (mixedSample < Short.MIN_VALUE) {
                mixedSample = Short.MIN_VALUE;
            }

            mixedBuffer.putShort((short) mixedSample);
        }

        return mixedBuffer.array();
    }
}
