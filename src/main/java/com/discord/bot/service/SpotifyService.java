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

import com.discord.bot.dto.MusicDto;
import com.discord.bot.dto.response.spotify.ArtistDto;
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

    public List<MusicDto> getTopTracksFromArtist(String artist, int amount) {
        List<MusicDto> musicDtos = new ArrayList<>();
        Optional<ArtistDto> artistDto = searchArtist(artist);
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

    public List<MusicDto> getRecommendationsForArtists(List<String> artistIds, int amount) {
        Set<String> artistSet = new HashSet<>(artistIds);
        return getRecommendations(new ArrayList<>(artistSet), SeedType.ARTIST, amount);
    }

    public List<MusicDto> getRecommendationsForGenres(List<String> genres, int amount) {
        Set<String> genreSet = new HashSet<>(genres);
        return getRecommendations(new ArrayList<>(genreSet), SeedType.GENRE, amount);
    }

    public List<MusicDto> getRecommendationsForTracks(List<String> trackIds, int amount) {
        Set<String> trackSet = new HashSet<>(trackIds);
        return getRecommendations(new ArrayList<>(trackSet), SeedType.TRACK, amount);
    }

    public List<MusicDto> getRecommendationsForTrackDtos(List<MusicDto> musicDtos, int amount) {
        Set<String> trackIds = musicDtos.stream()
            .limit(5 /* Max seeds */)
            .map(musicDto -> searchTrack(musicDto.getTitle()))
            .flatMap(Optional::stream)
            .map(TrackDto::getId)
            .collect(Collectors.toSet());
    
        return getRecommendations(new ArrayList<>(trackIds), SeedType.TRACK, amount);
    }

    private List<MusicDto> getRecommendations(Collection<String> seeds, SeedType type, int amount) {
        List<MusicDto> recommendations = new ArrayList<>();
        final int MAX_SEEDS = 5;
        String seedsStr = seeds.stream()
                               .limit(MAX_SEEDS)
                               .collect(StringBuilder::new, (sb, s) -> sb.append(s).append(","), StringBuilder::append)
                               .toString();

        String url = "https://api.spotify.com/v1/recommendations?limit=" + amount + "&" + type.getSeedParam() + "=" + seedsStr;
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

    public SpotifyTrackResponse getSpotifyTrackData(String trackId) {
        String url = "https://api.spotify.com/v1/tracks/" + trackId;
        return getSpotifyData(url, SpotifyTrackResponse.class);
    }

    public SpotifyPlaylistResponse getSpotifyPlaylistData(String playlistId) {
        String url = "https://api.spotify.com/v1/playlists/" + playlistId + "?fields=name,tracks.items";
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
