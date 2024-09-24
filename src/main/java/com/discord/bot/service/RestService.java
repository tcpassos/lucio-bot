package com.discord.bot.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.discord.bot.dto.MultipleMusicDto;
import com.discord.bot.dto.MusicDto;
import com.discord.bot.dto.response.spotify.SpotifyItemDto;
import com.discord.bot.dto.response.spotify.SpotifyPlaylistResponse;
import com.discord.bot.dto.response.spotify.SpotifyTrackResponse;
import com.discord.bot.dto.response.spotify.TrackDto;
import com.discord.bot.entity.Music;
import com.discord.bot.repository.MusicRepository;

@Service
public class RestService {
    public static String spotifyToken;

    private final MusicRepository musicRepository;
    private final InvidiousService invidiousService;
    private final YouTubeApiService youTubeApiService;
    private final RestTemplate restTemplate;

    @Value("${youtube.api.key}")
    private String youtubeApiKey;

    public RestService(MusicRepository musicRepository, InvidiousService invidiousService, YouTubeApiService youTubeApiService) {
        this.musicRepository = musicRepository;
        this.invidiousService = invidiousService;
        this.youTubeApiService = youTubeApiService;
        this.restTemplate = new RestTemplateBuilder().build();
    }

    public List<MusicDto> getTracksFromSpotify(String spotifyUrl) {
        String id;
        List<MusicDto> musicDtos = new ArrayList<>();

        if (spotifyUrl.contains("https://open.spotify.com/playlist/")) {
            id = spotifyUrl.substring(34, 56);
            spotifyUrl = "https://api.spotify.com/v1/playlists/" + id + "/tracks?fields=items(track(name,artists(name)))";
            SpotifyPlaylistResponse spotifyPlaylistResponse = getSpotifyPlaylistData(spotifyUrl);
            List<SpotifyItemDto> items = spotifyPlaylistResponse.getSpotifyItemDtoList();

            for (SpotifyItemDto item : items) {
                TrackDto trackDtoList = item.getTrackDtoList();
                String musicName = trackDtoList.getArtistDtoList().get(0).getName() + " - " + trackDtoList.getName();
                musicDtos.add(new MusicDto(musicName, null));
            }
        } else if (spotifyUrl.contains("https://open.spotify.com/track/")) {
            id = spotifyUrl.substring(31, 53);
            spotifyUrl = "https://api.spotify.com/v1/tracks/" + id;
            SpotifyTrackResponse spotifyTrackResponse = getSpotifyTrackData(spotifyUrl);
            String musicName = spotifyTrackResponse.getArtistDtoList().get(0).getName() +
                    " - " + spotifyTrackResponse.getSongName();
            musicDtos.add(new MusicDto(musicName, null));
        }

        return musicDtos;
    }

    public MultipleMusicDto getYoutubeUrl(MusicDto musicDto) {
        int count = 0;
        int failCount = 0;

        Music music = musicRepository.findFirstByTitle(musicDto.getTitle());

        if (music != null) {
            musicDto.setReference(music.getReference());
            count++;
        } else {
            String videoId = invidiousService.searchVideoId(musicDto.getTitle());
            if (videoId == null) {
                videoId = youTubeApiService.searchVideoId(musicDto.getTitle());
            }

            if (videoId != null) {
                String youtubeUrl = "https://www.youtube.com/watch?v=" + videoId;
                musicDto.setReference(youtubeUrl);

                Music newMusic = new Music();
                newMusic.setTitle(musicDto.getTitle());
                newMusic.setReference(youtubeUrl);
                musicRepository.save(newMusic);

                count++;
            } else {
                failCount++;
            }
        }

        return new MultipleMusicDto(count, Collections.singletonList(musicDto), failCount);
    }

    public MultipleMusicDto getYoutubeUrl(List<MusicDto> musicDtos) {
        AtomicInteger count = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        List<MusicDto> updatedMusicDtos = musicDtos.parallelStream().peek(musicDto -> {
            Music music = musicRepository.findFirstByTitle(musicDto.getTitle());

            if (music != null) {
                musicDto.setReference(music.getReference());
                count.incrementAndGet();
            } else {
                String videoId = invidiousService.searchVideoId(musicDto.getTitle());
                if (videoId == null) {
                    videoId = youTubeApiService.searchVideoId(musicDto.getTitle());
                }

                if (videoId != null) {
                    String youtubeUrl = "https://www.youtube.com/watch?v=" + videoId;
                    musicDto.setReference(youtubeUrl);

                    Music newMusic = new Music();
                    newMusic.setTitle(musicDto.getTitle());
                    newMusic.setReference(youtubeUrl);
                    musicRepository.save(newMusic);

                    count.incrementAndGet();
                } else {
                    failCount.incrementAndGet();
                }
            }
        }).collect(Collectors.toList());

        return new MultipleMusicDto(count.get(), updatedMusicDtos, failCount.get());
    }

    private SpotifyPlaylistResponse getSpotifyPlaylistData(String spotifyUrl) {
        URI spotifyUri = createUri(spotifyUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(spotifyToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(spotifyUri, HttpMethod.GET, entity, SpotifyPlaylistResponse.class).getBody();
    }

    private SpotifyTrackResponse getSpotifyTrackData(String spotifyUrl) {
        URI spotifyUri = createUri(spotifyUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(spotifyToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(spotifyUri, HttpMethod.GET, entity, SpotifyTrackResponse.class).getBody();
    }

    private URI createUri(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
    }
}