package com.discord.bot.audioplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;

@Getter
@Setter
public class GuildAudioManager {
    public final AudioPlayer musicPlayer;
    public final AudioPlayer sfxPlayer;
    private final AudioSendHandler sendHandler;
    public TrackScheduler scheduler;

    public GuildAudioManager(AudioPlayerManager manager, Guild guild) {
        this.sfxPlayer = manager.createPlayer();
        this.musicPlayer = manager.createPlayer();
        this.scheduler = new TrackScheduler(this.musicPlayer, guild);
        this.musicPlayer.addListener(this.scheduler);

        this.sendHandler = new AudioMixerSendHandler(this.musicPlayer, this.sfxPlayer);
    }
}