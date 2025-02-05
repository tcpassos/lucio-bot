package com.discord.bot.service;

import com.discord.bot.audioplayer.GuildPlaybackManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.BlockingQueue;

@Component
public class MusicCommandUtils {

    public boolean isSameAudioChannel(SlashCommandInteractionEvent event) {
        var guild = event.getGuild();
        var member = event.getMember();
    
        if (guild == null || member == null) {
            return false;
        }
    
        GuildVoiceState selfVoiceState = guild.getSelfMember().getVoiceState();
        GuildVoiceState memberVoiceState = member.getVoiceState();
    
        if (selfVoiceState == null || memberVoiceState == null) {
            return false;
        }
    
        return selfVoiceState.inAudioChannel() && memberVoiceState.inAudioChannel() &&
               selfVoiceState.getChannel().equals(memberVoiceState.getChannel());
    }

    public EmbedBuilder queueBuilder(EmbedBuilder embedBuilder, int page, BlockingQueue<AudioTrack> queue, List<AudioTrack> trackList) {
        embedBuilder.setTitle("Queue - Page " + page);
        int startIndex = (page - 1) * 20;
        int endIndex = Math.min(startIndex + 20, queue.size());

        for (int i = startIndex; i < endIndex; i++) {
            AudioTrack track = trackList.get(i);
            AudioTrackInfo info = track.getInfo();
            embedBuilder.appendDescription((i + 1) + ". " + info.title + "\n");
        }

        return embedBuilder;
    }

    public void playerCleaner(GuildPlaybackManager musicManager) {
        musicManager.musicScheduler.repeating = false;
        musicManager.musicScheduler.player.setPaused(false);
        musicManager.musicScheduler.player.stopTrack();
        musicManager.musicScheduler.queue.clear();
    }
}