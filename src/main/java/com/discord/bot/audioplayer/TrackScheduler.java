package com.discord.bot.audioplayer;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

/**
 * Class that manages the queue of tracks to be played by the bot.
 */
public class TrackScheduler extends AudioEventAdapter {
    private final static Logger logger = LoggerFactory.getLogger(TrackScheduler.class);
    public final AudioPlayer player;
    public final BlockingQueue<AudioTrack> queue;
    public boolean repeating = false;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    /**
     * Queues a track to be played.
     *
     * @param track The track to be queued.
     */
    public void queue(AudioTrack track) {
        if (this.player.startTrack(track, true)) {
            return; // Track started immediately
        }
        boolean offerSuccess = this.queue.offer(track);
        if (!offerSuccess) {
            logger.error("Queue is full, could not add track: " + track.getInfo().title);
        }
    }

    /**
     * Queues a list of tracks to be played.
     *
     * @param tracks The list of tracks to be queued.
     */
    public void queueAll(List<AudioTrack> tracks) {
        for (AudioTrack track : tracks) {
            if (!this.player.startTrack(track, true)) {
                boolean offerSuccess = this.queue.offer(track);

                if (!offerSuccess) {
                    logger.error("Queue is full, could not add track and tracks after: " + track.getInfo().title);
                    break;
                }
            }
        }
    }

    /**
     * Skips the current track and starts the next one in the queue.
     */
    public void nextTrack() {
        this.player.startTrack(this.queue.poll(), false);
    }

    /**
     * Event fired when the current track ends.
     */
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (!endReason.mayStartNext) {
            return;
        }
        if (this.repeating) {
            this.player.startTrack(track.makeClone(), false);
        } else {
            nextTrack();
        }
    }

    /**
     * Logs an error if an exception occurs while playing a track and proceeds to the next track.
     */
    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        logger.error("Error occurred while playing track: {}", track.getInfo().title, exception);
        nextTrack();
    }
}