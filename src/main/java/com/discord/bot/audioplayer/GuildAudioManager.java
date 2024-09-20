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
    public TrackScheduler musicScheduler;
    public TrackScheduler sfxScheduler;

    public GuildAudioManager(AudioPlayerManager manager, Guild guild) {
        this.musicPlayer = manager.createPlayer();
        this.musicScheduler = new TrackScheduler(this.musicPlayer, this, guild);
        this.musicPlayer.addListener(this.musicScheduler);

        this.sfxPlayer = manager.createPlayer();
        this.sfxScheduler = new TrackScheduler(this.sfxPlayer, this, guild);
        this.sfxPlayer.addListener(this.sfxScheduler);

        this.sendHandler = new AudioMixerSendHandler(this.musicPlayer, this.sfxPlayer);
    }
}