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
    public QueueEvent queue(AudioTrack track) {
        if (this.player.startTrack(track, true)) {
            return QueueEvent.TRACK_STARTED;
        }
        if (!this.queue.offer(track)) {
            logger.error("Queue is full, could not add track: " + track.getInfo().title);
            return QueueEvent.QUEUE_FULL;
        }
        return QueueEvent.TRACK_ADDED;
    }

    /**
     * Queues a list of tracks to be played.
     *
     * @param tracks The list of tracks to be queued.
     */
    public QueueEvent queueAll(List<AudioTrack> tracks) {
        boolean trackStarted = false;

        for (AudioTrack track : tracks) {
            if (this.player.startTrack(track, true)) {
                trackStarted = true;
            } else {
                if (!this.queue.offer(track)) {
                    logger.error("Queue is full, could not add track and tracks after: " + track.getInfo().title);
                    return QueueEvent.QUEUE_FULL;
                }
            }
        }

        return trackStarted ? QueueEvent.TRACK_STARTED : QueueEvent.TRACK_ADDED;
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

    public enum QueueEvent {
        TRACK_ADDED,
        TRACK_STARTED,
        PLAYLIST_ADDED,
        QUEUE_FULL
    }
}