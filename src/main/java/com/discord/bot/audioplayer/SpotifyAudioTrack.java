package com.discord.bot.audioplayer;

import com.discord.bot.service.YoutubeService;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;

import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.track.YoutubeAudioTrack;

public class SpotifyAudioTrack extends DelegatedAudioTrack {
    private final YoutubeAudioSourceManager sourceManager;
    private final YoutubeService youtubeService;

    public SpotifyAudioTrack(AudioTrackInfo trackInfo, YoutubeAudioSourceManager sourceManager, YoutubeService youtubeService) {
        super(trackInfo);
        this.sourceManager = sourceManager;
        this.youtubeService = youtubeService;
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        // Search for the Spotify track on YouTube
        String query = trackInfo.author + " - " + trackInfo.title;
        String youtubeVideoId = youtubeService.searchVideoId(query);
        if (youtubeVideoId == null) {
            throw new FriendlyException("Failed to find a YouTube video for the Spotify track.", FriendlyException.Severity.COMMON, null);
        }

        // Load the YouTube track info
        AudioTrackInfo youtubeTrackInfo = new AudioTrackInfo(
            trackInfo.title,
            trackInfo.author,
            trackInfo.length,
            youtubeVideoId,
            trackInfo.isStream,
            "https://www.youtube.com/watch?v=" + youtubeVideoId
        );

        // Delegate processing to the YouTube track
        YoutubeAudioTrack delegate = new YoutubeAudioTrack(youtubeTrackInfo, sourceManager);
        processDelegate(delegate, executor);
    }

    @Override
    protected AudioTrack makeShallowClone() {
        return new SpotifyAudioTrack(trackInfo, sourceManager, youtubeService);
    }

    @Override
    public AudioSourceManager getSourceManager() {
        return sourceManager;
    }
}
