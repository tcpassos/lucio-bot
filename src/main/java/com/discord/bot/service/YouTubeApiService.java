package com.discord.bot.service;

import com.discord.bot.dto.response.youtube.YoutubeResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class YouTubeApiService {
    private final static Logger logger = LoggerFactory.getLogger(YouTubeApiService.class);
    private final RestTemplate restTemplate;

    @Value("${youtube.api.key}")
    private String youtubeApiKey;

    public YouTubeApiService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public String searchVideoId(String songTitle) {
        try {
            String encodedMusicName = URLEncoder.encode(songTitle, StandardCharsets.UTF_8);
            String youtubeUrl = "https://youtube.googleapis.com/youtube/v3/search?part=id&fields=items(id(videoId))" +
                    "&maxResults=1&q=" +
                    encodedMusicName +
                    "&key=" +
                    youtubeApiKey;

            URI youtubeUri = new URI(youtubeUrl);
            YoutubeResponse youtubeResponse = restTemplate.getForObject(youtubeUri, YoutubeResponse.class);

            if (youtubeResponse != null && !youtubeResponse.getItems().isEmpty()) {
                return youtubeResponse.getItems().get(0).getId().getVideoId();
            }
        } catch (Exception e) {
            logger.error("YouTube API error: " + e.getMessage());
        }
        return null;
    }
}
