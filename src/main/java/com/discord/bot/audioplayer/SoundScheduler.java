package com.discord.bot.audioplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import net.dv8tion.jda.api.entities.Guild;

public class SoundScheduler extends AudioEventAdapter {
    public final AudioPlayer player;
    public boolean repeating = false;
    private final Guild guild;

    public SoundScheduler(AudioPlayer player, Guild guild) {
        this.player = player;
        this.guild = guild;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (guild.getSelfMember().getVoiceState() != null && guild.getSelfMember().getVoiceState().getChannel() != null) {
            // If bot is alone in the voice
            if (guild.getSelfMember().getVoiceState().getChannel().getMembers().size() == 1) {
                guild.getAudioManager().closeAudioConnection();
                return;
            }
        }
    }
}