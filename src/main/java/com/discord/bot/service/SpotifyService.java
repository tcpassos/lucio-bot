package com.discord.bot.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.discord.bot.dto.response.spotify.ArtistDto;
import com.discord.bot.dto.response.spotify.SpotifyPlaylistResponse;
import com.discord.bot.dto.response.spotify.SpotifySearchResponse;
import com.discord.bot.dto.response.spotify.SpotifyTrackResponse;
import com.discord.bot.dto.response.spotify.SpotifyTracksResponse;
import com.discord.bot.dto.response.spotify.TrackDto;

@Service
public class SpotifyService {
    private static final int MAX_SEEDS = 5;
    public static String spotifyToken;

    private final RestTemplate restTemplate;

    public SpotifyService() {
        this.restTemplate = new RestTemplateBuilder().build();
    }

    /**
     * Get top tracks from an artist
     *
     * @param artist the artist name
     * @param amount the number of tracks to get
     * @return list of {@link TrackDto} top tracks
     */
    public List<TrackDto> getTopTracksFromArtist(String artist, int amount) {
        Optional<ArtistDto> artistDto = searchArtist(artist);
        if (artistDto.isEmpty()) {
            return new ArrayList<>();
        }

        String artistId = artistDto.get().getId();
        String url = "https://api.spotify.com/v1/artists/" + artistId + "/top-tracks";
        SpotifyTracksResponse spotifyTracksResponse = getSpotifyData(url, SpotifyTracksResponse.class);

        amount = Math.min(amount, spotifyTracksResponse.getTracks().size());
        return spotifyTracksResponse.getTracks().subList(0, amount);
    }

    /**
     * Get recommendations based on artists
     *
     * @param artistIds list of artist IDs
     * @param amount the number of recommendations to get
     * @return list of {@link TrackDto} recommendations
     */
    public List<TrackDto> getRecommendationsForArtists(List<String> artistIds, int amount) {
        Set<String> artistSet = new HashSet<>(artistIds);
        return getRecommendations(new ArrayList<>(artistSet), SeedType.ARTIST, amount);
    }

    /**
     * Get recommendations based on genres
     *
     * @param genres list of genres
     * @param amount the number of recommendations to get
     * @return list of {@link TrackDto} recommendations
     */
    public List<TrackDto> getRecommendationsForGenres(List<String> genres, int amount) {
        Set<String> genreSet = new HashSet<>(genres);
        return getRecommendations(new ArrayList<>(genreSet), SeedType.GENRE, amount);
    }

    /**
     * Get recommendations based on track IDs
     *
     * @param trackIds list of track IDs
     * @param amount the number of recommendations to get
     * @return list of {@link TrackDto} recommendations
     */
    public List<TrackDto> getRecommendationsForTracks(List<String> trackIds, int amount) {
        Set<String> trackSet = new HashSet<>(trackIds);
        return getRecommendations(new ArrayList<>(trackSet), SeedType.TRACK, amount);
    }

    /**
     * Get recommendations based on track names
     *
     * @param trackNames list of track names
     * @param amount the number of recommendations to get
     * @return list of {@link TrackDto} recommendations
     */
    public List<TrackDto> getRecommendationsForTrackNames(List<String> trackNames, int amount) {
        Set<String> trackIds = trackNames.stream()
            .map(this::searchTrack)
            .flatMap(Optional::stream)
            .map(TrackDto::getId)
            .limit(MAX_SEEDS)
            .collect(Collectors.toSet());
    
        return getRecommendations(new ArrayList<>(trackIds), SeedType.TRACK, amount);
    }
    
    /**
     * Get recommendations based on seeds
     *
     * @param seeds list of seeds (artist, genre, or track)
     * @param type the type of seed
     * @param amount the number of recommendations to get
     * @return list of {@link TrackDto} recommendations
     */
    private List<TrackDto> getRecommendations(Collection<String> seeds, SeedType type, int amount) {
        String seedsStr = seeds.stream()
                               .limit(MAX_SEEDS)
                               .collect(Collectors.joining(","));
    
        String url = "https://api.spotify.com/v1/recommendations?limit=" + amount + "&" + type.getSeedParam() + "=" + seedsStr;
        SpotifyTracksResponse spotifyTracksResponse = getSpotifyData(url, SpotifyTracksResponse.class);
    
        if (spotifyTracksResponse.getTracks() != null) {
            return spotifyTracksResponse.getTracks();
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Search for an artist by name
     *
     * @param query the artist name
     * @return {@link ArtistDto} if found, empty otherwise
     */
    public Optional<ArtistDto> searchArtist(String query) {
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

    /**
     * Search for a track by name
     *
     * @param query the track name
     * @return {@link TrackDto} if found, empty otherwise
     */
    public Optional<TrackDto> searchTrack(String query) {
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

    public SpotifyTrackResponse getTrackById(String trackId) {
        String url = "https://api.spotify.com/v1/tracks/" + trackId;
        return getSpotifyData(url, SpotifyTrackResponse.class);
    }

    public SpotifyPlaylistResponse getPlaylistById(String playlistId) {
        String url = "https://api.spotify.com/v1/playlists/" + playlistId + "?fields=name,external_urls,tracks.items";
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

    private enum SeedType {
        ARTIST("seed_artists"),
        TRACK("seed_tracks"),
        GENRE("seed_genres");

        private final String seedParam;

        SeedType(String value) {
            this.seedParam = value;
        }

        public String getSeedParam() {
            return seedParam;            
        }
    }
}
