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
        this.buffer = ByteBuffer.allocate(3840);
    }

    @Override
    public boolean canProvide() {
        return true;
    }
    

    public ByteBuffer provide20MsAudio() {
        AudioFrame frameMusic = musicPlayer.provide();
        AudioFrame frameSfx = sfxPlayer.provide();
    
        byte[] dataMusic = frameMusic != null ? frameMusic.getData() : null;
        byte[] dataSfx = frameSfx != null ? frameSfx.getData() : null;
    
        if (dataMusic == null && dataSfx == null) {
            return buffer.clear().flip();  // Empty buffer
        }
    
        byte[] mixedData;
        if (dataMusic == null) {
            mixedData = dataSfx;
        } else if (dataSfx == null) {
            mixedData = dataMusic;
        } else {
            mixedData = mixAudio(dataMusic, dataSfx);
        }
    
        buffer.clear();
        buffer.put(mixedData);
        buffer.flip();
    
        return buffer;
    }    

    @Override
    public boolean isOpus() {
        return false;
    }

    private byte[] mixAudio(byte[] audio1, byte[] audio2) {
        int maxLength = Math.max(audio1.length, audio2.length);
        // Ensure the length is a multiple of 4 (2 bytes per channel in stereo)
        int length = (maxLength + 3) & ~3;
        byte[] mixedData = new byte[length];
    
        // Reduce the volume of audio1 by 20% when audio2 is playing
        float volumeReduction = 0.2f; 
    
        for (int i = 0; i < length; i += 4) {
            short sampleL1 = 0;
            short sampleR1 = 0;
            short sampleL2 = 0;
            short sampleR2 = 0;
    
            // Left channel
            if (i + 1 < audio1.length) {
                sampleL1 = (short) (((audio1[i] & 0xFF) << 8) | (audio1[i + 1] & 0xFF));
            }
    
            if (i + 1 < audio2.length) {
                sampleL2 = (short) (((audio2[i] & 0xFF) << 8) | (audio2[i + 1] & 0xFF));
            }
    
            float sampleL1Float = sampleL1;
            float sampleL2Float = sampleL2;
    
            // Apply volume reduction to audio1 if audio2 is playing
            if (sampleL2 != 0) {
                sampleL1Float *= volumeReduction;
            }
    
            float mixedSampleL = sampleL1Float + sampleL2Float;
    
            // Clipping
            if (mixedSampleL > Short.MAX_VALUE) {
                mixedSampleL = Short.MAX_VALUE;
            } else if (mixedSampleL < Short.MIN_VALUE) {
                mixedSampleL = Short.MIN_VALUE;
            }
    
            // Right channel
            if (i + 3 < audio1.length) {
                sampleR1 = (short) (((audio1[i + 2] & 0xFF) << 8) | (audio1[i + 3] & 0xFF));
            }
    
            if (i + 3 < audio2.length) {
                sampleR2 = (short) (((audio2[i + 2] & 0xFF) << 8) | (audio2[i + 3] & 0xFF));
            }
    
            float sampleR1Float = sampleR1;
            float sampleR2Float = sampleR2;
    
            // Apply volume reduction to audio1 if audio2 is playing
            if (sampleR2 != 0) {
                sampleR1Float *= volumeReduction;
            }
    
            float mixedSampleR = sampleR1Float + sampleR2Float;
    
            // Clipping
            if (mixedSampleR > Short.MAX_VALUE) {
                mixedSampleR = Short.MAX_VALUE;
            } else if (mixedSampleR < Short.MIN_VALUE) {
                mixedSampleR = Short.MIN_VALUE;
            }
    
            short finalSampleL = (short) mixedSampleL;
            short finalSampleR = (short) mixedSampleR;
    
            // Store the final sample in the mixed data
            if (i + 1 < mixedData.length) {
                mixedData[i] = (byte) ((finalSampleL >> 8) & 0xFF);
                mixedData[i + 1] = (byte) (finalSampleL & 0xFF);
            }
    
            if (i + 3 < mixedData.length) {
                mixedData[i + 2] = (byte) ((finalSampleR >> 8) & 0xFF);
                mixedData[i + 3] = (byte) (finalSampleR & 0xFF);
            }
        }
    
        return mixedData;
    }    
}
