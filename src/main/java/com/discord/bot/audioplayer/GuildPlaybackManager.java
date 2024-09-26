package com.discord.bot.audioplayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;

@Getter
@Setter
public class GuildPlaybackManager {
    private final static Logger logger = LoggerFactory.getLogger(GuildPlaybackManager.class);

    public final Guild guild;
    public final AudioPlayer musicPlayer;
    public final AudioPlayer sfxPlayer;
    private final AudioSendHandler sendHandler;
    public TrackScheduler musicScheduler;
    public TrackScheduler sfxScheduler;

    public GuildPlaybackManager(AudioPlayerManager manager, Guild guild) {
        this.guild = guild;

        this.musicPlayer = manager.createPlayer();
        this.musicScheduler = new TrackScheduler(this.musicPlayer);
        this.musicPlayer.addListener(this.musicScheduler);

        this.sfxPlayer = manager.createPlayer();
        this.sfxScheduler = new TrackScheduler(this.sfxPlayer);
        this.sfxPlayer.addListener(this.sfxScheduler);

        this.sendHandler = new AudioMixerSendHandler(this.musicPlayer, this.sfxPlayer);
    }

    /**
     * Disconnects the bot from the voice channel if no tracks are playing.
     */
    public void checkToDisconnect() {
        if (!guild.getAudioManager().isConnected()) {
            return;
        }
        if (musicPlayer.isPaused() || musicPlayer.getPlayingTrack() != null) {
            return;
        }
        if (sfxPlayer.getPlayingTrack() != null) {
            return;
        }
        logger.info("No tracks are playing. Disconnecting from the voice channel.");
        guild.getAudioManager().closeAudioConnection();
    }

    /**
     * Updates the bot's activity to the currently playing track.
     */
    public void updateActivity() {
        var presence = guild.getJDA().getPresence();
        var track = musicPlayer.getPlayingTrack();
        var activity = (track != null) ? Activity.listening(track.getInfo().title) : null;
        if (presence.getActivity() == null || !presence.getActivity().equals(activity)) {
            presence.setActivity(activity);
        }
    }
}