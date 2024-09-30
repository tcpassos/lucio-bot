package com.discord.bot.audioplayer;

import java.util.Optional;

import com.discord.bot.entity.Music;
import com.discord.bot.repository.MusicRepository;
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
    private final MusicRepository musicRepository;

    public SpotifyAudioTrack(AudioTrackInfo trackInfo, YoutubeAudioSourceManager sourceManager, YoutubeService youtubeService, MusicRepository musicRepository) {
        super(trackInfo);
        this.sourceManager = sourceManager;
        this.youtubeService = youtubeService;
        this.musicRepository = musicRepository;
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        String spotifyTrackId = trackInfo.identifier;
    
        // Check if the mapping between the Spotify track and the YouTube video is cached
        Optional<Music> optionalMusic = musicRepository.findBySpotifyId(spotifyTrackId);
        String youtubeVideoId = optionalMusic.map(Music::getYoutubeId)
                                             .orElseGet(() -> fetchAndCacheYoutubeVideoId(spotifyTrackId));
    
        // Create a new track info with the YouTube video ID
        AudioTrackInfo youtubeTrackInfo = new AudioTrackInfo(
            trackInfo.title,
            trackInfo.author,
            trackInfo.length,
            youtubeVideoId,
            trackInfo.isStream,
            "https://www.youtube.com/watch?v=" + youtubeVideoId
        );
    
        // Delegate to the YouTube audio track
        YoutubeAudioTrack delegate = new YoutubeAudioTrack(youtubeTrackInfo, sourceManager);
        processDelegate(delegate, executor);
    }

    @Override
    protected AudioTrack makeShallowClone() {
        return new SpotifyAudioTrack(trackInfo, sourceManager, youtubeService, musicRepository);
    }

    @Override
    public AudioSourceManager getSourceManager() {
        return sourceManager;
    }

    /**
     * Fetches the YouTube video ID for the Spotify track and caches the mapping.
     *
     * @param spotifyTrackId The Spotify track ID
     * @return The YouTube video ID
     */
    private String fetchAndCacheYoutubeVideoId(String spotifyTrackId) {
        String youtubeVideoId = youtubeService.searchVideoId(trackInfo.author + " - " + trackInfo.title);
        if (youtubeVideoId == null) {
            throw new FriendlyException("Failed to find a YouTube video for the Spotify track.", FriendlyException.Severity.COMMON, null);
        }
    
        Music music = new Music(
            spotifyTrackId,
            youtubeVideoId,
            trackInfo.title,
            trackInfo.author,
            trackInfo.length
        );
        musicRepository.save(music);
    
        return youtubeVideoId;
    }
}
