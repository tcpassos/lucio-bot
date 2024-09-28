package com.discord.bot.audioplayer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.discord.bot.dto.response.spotify.TrackDto;
import com.discord.bot.service.SpotifyService;
import com.discord.bot.service.YoutubeService;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;

import dev.lavalink.youtube.YoutubeAudioSourceManager;

public class SpotifyToYoutubeSourceManager implements AudioSourceManager {

    private final YoutubeAudioSourceManager youtubeSourceManager;
    private final SpotifyService spotifyService;
    private final YoutubeService youtubeService;

    public SpotifyToYoutubeSourceManager(YoutubeAudioSourceManager youtubeSourceManager, SpotifyService spotifyService, YoutubeService youtubeService) {
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

        AudioTrackInfo spotifyTrackInfo = new AudioTrackInfo(
            trackData.getSongName(),
            trackData.getArtistDtoList().get(0).getName(),
            trackData.getDurationMs(),
            trackId,
            false,
            url
        );
        return new SpotifyAudioTrack(spotifyTrackInfo, youtubeSourceManager, youtubeService);
    }

    private AudioItem loadSpotifyPlaylist(String url) {
        int idIndex = url.indexOf("playlist/") + 9;
        String playlistId = url.substring(idIndex, idIndex + 22);
        var playlistData = spotifyService.getSpotifyPlaylistData(playlistId);
        String playlistName = playlistData.getName();
        var playlistItems = playlistData.getTracks().getItems();
        List<AudioTrack> tracks = new ArrayList<>();

        for (var playListItem : playlistItems) {
            TrackDto track = playListItem.getTrack();
            AudioTrackInfo spotifyTrackInfo = new AudioTrackInfo(
                track.getName(),
                track.getArtistDtoList().get(0).getName(),
                track.getDurationMs(),
                track.getId(),
                false,
                "https://open.spotify.com/track/" + track.getId()
            );
            tracks.add(new SpotifyAudioTrack(spotifyTrackInfo, youtubeSourceManager, youtubeService));
        }

        return new BasicAudioPlaylist(playlistName, tracks, null, false);
    }
}
