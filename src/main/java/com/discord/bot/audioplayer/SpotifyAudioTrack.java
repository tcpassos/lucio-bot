package com.discord.bot.audioplayer;

import com.discord.bot.service.YouTubeApiService;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;

import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.track.YoutubeAudioTrack;

public class SpotifyAudioTrack extends DelegatedAudioTrack {
    private final YoutubeAudioSourceManager sourceManager;
    private final YouTubeApiService youtubeService;

    public SpotifyAudioTrack(AudioTrackInfo trackInfo, YoutubeAudioSourceManager sourceManager, YouTubeApiService youtubeService) {
        super(trackInfo);
        this.sourceManager = sourceManager;
        this.youtubeService = youtubeService;
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        String youtubeVideoId = "TODO"; // TODO
        AudioTrackInfo youtubeTrackInfo = new AudioTrackInfo(
            trackInfo.title,
            trackInfo.author,
            trackInfo.length,
            youtubeVideoId,
            trackInfo.isStream,
            "https://www.youtube.com/watch?v=" + youtubeVideoId
        );
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
