package com.discord.bot.audioplayer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.discord.bot.service.SpotifyService;
import com.discord.bot.service.YouTubeApiService;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import dev.lavalink.youtube.YoutubeAudioSourceManager;

public class SpotifyToYoutubeSourceManager implements AudioSourceManager {

    private final YoutubeAudioSourceManager youtubeSourceManager;
    private final SpotifyService spotifyService;
    private final YouTubeApiService youtubeService;

    public SpotifyToYoutubeSourceManager(YoutubeAudioSourceManager youtubeSourceManager, SpotifyService spotifyService, YouTubeApiService youtubeService) {
        this.youtubeSourceManager = youtubeSourceManager;
        this.spotifyService = spotifyService;
        this.youtubeService = youtubeService;
    }

    @Override
    public String getSourceName() {
        return "spotify";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        String identifier = reference.identifier;

        if (isSpotifyUrl(identifier)) {
            if (isSpotifyTrackUrl(identifier)) {
                return loadSpotifyTrack(identifier);
            } else if (isSpotifyPlaylistUrl(identifier)) {
                return loadSpotifyPlaylist(identifier);
            } else {
                return null;
            }
        }

        return null;
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException { }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        return new SpotifyAudioTrack(trackInfo, youtubeSourceManager, youtubeService);
    }

    @Override
    public void shutdown() { }

    private boolean isSpotifyUrl(String url) {
        return url != null && url.startsWith("https://open.spotify.com/");
    }

    private boolean isSpotifyTrackUrl(String url) {
        return url.contains("/track/");
    }
    
    private boolean isSpotifyPlaylistUrl(String url) {
        return url.contains("/playlist/");
    }

    private AudioItem loadSpotifyTrack(String url) {
        int idIndex = url.indexOf("track/") + 6;
        String trackId = url.substring(idIndex, idIndex + 22);
        var trackData = spotifyService.getSpotifyTrackData(trackId);

        AudioItem item = null; // TODO
        return item;
    }

    private AudioItem loadSpotifyPlaylist(String url) {
        return null; // TODO
    }
}
