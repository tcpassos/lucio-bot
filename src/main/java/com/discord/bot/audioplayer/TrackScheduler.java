package com.discord.bot.audioplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {
    private final static Logger logger = LoggerFactory.getLogger(TrackScheduler.class);
    public final AudioPlayer player;
    public final BlockingQueue<AudioTrack> queue;
    private final GuildAudioManager guildAudioManager;
    private final Guild guild;
    public boolean repeating = false;

    public TrackScheduler(AudioPlayer player, GuildAudioManager guildAudioManager, Guild guild) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
        this.guildAudioManager = guildAudioManager;
        this.guild = guild;
    }

    public void queue(AudioTrack track) {
        if (this.player.startTrack(track, true)) {
            this.guild.getJDA().getPresence().setActivity(Activity.listening(track.getInfo().title));
        } else {
            boolean offerSuccess = this.queue.offer(track);
            if (!offerSuccess) {
                logger.error("Queue is full, could not add track: " + track.getInfo().title);
            }
        }
    }

    public void queueAll(List<AudioTrack> tracks) {
        for (AudioTrack track : tracks) {
            if (!this.player.startTrack(track, true)) {
                boolean offerSuccess = this.queue.offer(track);

                if (!offerSuccess)
                    logger.error("Queue is full, could not add track and tracks after: " + track.getInfo().title);
            }
        }
    }

    public void nextTrack() {
        this.player.startTrack(this.queue.poll(), false);
        if (!checkToDisconnect()) {
            this.guild.getJDA().getPresence().setActivity(Activity.listening(this.player.getPlayingTrack().getInfo().title));
        } else {
            this.guild.getJDA().getPresence().setActivity(null);
        }
    }

    private boolean checkToDisconnect() {
        if (guildAudioManager.getMusicPlayer().getPlayingTrack() == null &&
            guildAudioManager.getSfxPlayer().getPlayingTrack() == null) {
            logger.info("No tracks are playing. Disconnecting from the voice channel.");
            guild.getAudioManager().closeAudioConnection();
            return true;
        }
        return false;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            if (this.repeating) {
                this.player.startTrack(track.makeClone(), false);
            } else {
                nextTrack();
            }
        } else {
            checkToDisconnect();
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        logger.error("Error occurred while playing track: {}", track.getInfo().title, exception);
        nextTrack();
    }
}