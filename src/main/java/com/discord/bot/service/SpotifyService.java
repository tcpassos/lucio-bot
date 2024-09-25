package com.discord.bot.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.discord.bot.dto.MusicDto;
import com.discord.bot.dto.response.spotify.ArtistDto;
import com.discord.bot.dto.response.spotify.SpotifyItemDto;
import com.discord.bot.dto.response.spotify.SpotifyPlaylistResponse;
import com.discord.bot.dto.response.spotify.SpotifySearchResponse;
import com.discord.bot.dto.response.spotify.SpotifyTrackResponse;
import com.discord.bot.dto.response.spotify.SpotifyTracksResponse;
import com.discord.bot.dto.response.spotify.TrackDto;

@Service
public class SpotifyService {
    public static String spotifyToken;

    private final RestTemplate restTemplate;

    public SpotifyService() {
        this.restTemplate = new RestTemplateBuilder().build();
    }

    public List<MusicDto> getTracksFromSpotify(String spotifyUrl) {
        List<MusicDto> musicDtos = new ArrayList<>();

        // Playlist URL
        if (spotifyUrl.contains("https://open.spotify.com/playlist/")) {
            String playlistId = spotifyUrl.substring(34, 56);
            List<SpotifyItemDto> items = getSpotifyPlaylistData(playlistId).getSpotifyItemDtoList();

            for (SpotifyItemDto item : items) {
                TrackDto trackDtoList = item.getTrackDtoList();
                String musicName = trackDtoList.getArtistDtoList().get(0).getName() + " - " + trackDtoList.getName();
                musicDtos.add(new MusicDto(musicName, null));
            }
        // Track URL
        } else if (spotifyUrl.matches("https://open\\.spotify\\.com.*/track/.*")) {
            int idIndex = spotifyUrl.indexOf("track/") + 6;
            String trackId = spotifyUrl.substring(idIndex, idIndex + 22);
            SpotifyTrackResponse spotifyTrackResponse = getSpotifyTrackData(trackId);
            String musicName = spotifyTrackResponse.getArtistDtoList().get(0).getName() + " - " + spotifyTrackResponse.getSongName();
            musicDtos.add(new MusicDto(musicName, null));
        }

        return musicDtos;
    }

    public List<MusicDto> getTopTracksFromArtist(String artist, int amount) {
        List<MusicDto> musicDtos = new ArrayList<>();
        Optional<ArtistDto> artistDto = getArtist(artist);
        if (artistDto.isEmpty()) {
            return musicDtos;
        }

        String artistId = artistDto.get().getId();
        String url = "https://api.spotify.com/v1/artists/" + artistId + "/top-tracks";
        SpotifyTracksResponse spotifyTracksResponse = getSpotifyData(url, SpotifyTracksResponse.class);

        amount = Math.min(amount, spotifyTracksResponse.getTracks().size());
    
        for (int i = 0; i < amount; i++) {
            TrackDto trackDto = spotifyTracksResponse.getTracks().get(i);
            String musicName = artist + " - " + trackDto.getName();
            musicDtos.add(new MusicDto(musicName, null));
        }

        return musicDtos;
    }

    public List<MusicDto> getRecommendations(List<MusicDto> musicDtos, int amount) {
        List<MusicDto> recommendations = new ArrayList<>();
        Set<String> trackIds = new HashSet<>();

        // Get track IDs from the first 5 musicDtos
        final int MAX_RECOMMENDATIONS = 5;
        for (MusicDto musicDto : musicDtos.stream().limit(MAX_RECOMMENDATIONS).toList()) {
            Optional<TrackDto> trackDto = getTrack(musicDto.getTitle());
            if (trackDto.isEmpty()) {
                continue;
            }
            trackIds.add(trackDto.get().getId());
        }

        // Get recommendations based on the track IDs
        String trackIdsStr = String.join(",", trackIds);
        String url = "https://api.spotify.com/v1/recommendations?limit=" + amount + "&seed_tracks=" + trackIdsStr;
        SpotifyTracksResponse spotifyTracksResponse = getSpotifyData(url, SpotifyTracksResponse.class);
        if (spotifyTracksResponse.getTracks() == null) {
            return recommendations;
        }

        // Add recommendations to the list
        spotifyTracksResponse.getTracks().forEach(trackDto -> {
            String musicName = trackDto.getArtistDtoList().get(0).getName() + " - " + trackDto.getName();
            recommendations.add(new MusicDto(musicName, null));
        });
        return recommendations;
    }

    private Optional<ArtistDto> getArtist(String query) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://api.spotify.com/v1/search?q=" + encodedQuery + "&type=artist&limit=1";
        var response = getSpotifyData(url, SpotifySearchResponse.class);
        if (response.getArtistSearchDto() == null) {
            return Optional.empty();
        }
        var artists = response.getArtistSearchDto().getItems();
        if (artists == null || artists.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(artists.get(0));
    }

    private Optional<TrackDto> getTrack(String query) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://api.spotify.com/v1/search?q=" + encodedQuery + "&type=track&limit=1";
        var response = getSpotifyData(url, SpotifySearchResponse.class);
        if (response.getTrackSearchDto() == null) {
            return Optional.empty();
        }
        var tracks = response.getTrackSearchDto().getItems();
        if (tracks == null || tracks.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(tracks.get(0));
    }

    private SpotifyTrackResponse getSpotifyTrackData(String trackId) {
        String url = "https://api.spotify.com/v1/tracks/" + trackId;
        return getSpotifyData(url, SpotifyTrackResponse.class);
    }

    private SpotifyPlaylistResponse getSpotifyPlaylistData(String playlistId) {
        String url = "https://api.spotify.com/v1/playlists/" + playlistId + "/tracks?fields=items(track(name,artists(name)))";
        return getSpotifyData(url, SpotifyPlaylistResponse.class);
    }

    private <T> T getSpotifyData(String spotifyUrl, Class<T> responseType) {
        URI spotifyUri = createUri(spotifyUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(spotifyToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(spotifyUri, HttpMethod.GET, entity, responseType).getBody();
    }

    private URI createUri(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
    }
}
