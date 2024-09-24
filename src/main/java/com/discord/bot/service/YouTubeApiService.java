package com.discord.bot.service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.discord.bot.dto.response.youtube.YoutubeResponse;
import com.discord.bot.entity.GuildConfig;
import com.discord.bot.repository.GuildConfigRepository;

@Service
public class YouTubeApiService {
    private final static Logger logger = LoggerFactory.getLogger(YouTubeApiService.class);
    private final RestTemplate restTemplate;
    private final GuildConfigRepository guildConfigRepository;

    public YouTubeApiService(RestTemplateBuilder restTemplateBuilder, GuildConfigRepository guildConfigRepository) {
        this.restTemplate = restTemplateBuilder.build();
        this.guildConfigRepository = guildConfigRepository;
    }

    public String searchVideoId(String songTitle, Long guildId) {
        try {
            GuildConfig guildConfig = guildConfigRepository.findByGuildId(guildId);

            String apiKey = (guildConfig != null && guildConfig.getYoutubeApiKey() != null)
                    ? guildConfig.getYoutubeApiKey()
                    : null;

            if (apiKey == null) {
                logger.error("YouTube API key is missing for guild ID: " + guildId);
                return null;
            }

            String encodedMusicName = URLEncoder.encode(songTitle, StandardCharsets.UTF_8);
            String youtubeUrl = "https://youtube.googleapis.com/youtube/v3/search?part=id&fields=items(id(videoId))" +
                    "&maxResults=1&q=" +
                    encodedMusicName +
                    "&key=" +
                    apiKey;

            URI youtubeUri = new URI(youtubeUrl);
            YoutubeResponse youtubeResponse = restTemplate.getForObject(youtubeUri, YoutubeResponse.class);

            if (youtubeResponse != null && !youtubeResponse.getItems().isEmpty()) {
                return youtubeResponse.getItems().get(0).getId().getVideoId();
            }
        } catch (Exception e) {
            logger.error("Failed to get result from YouTube API: " + e.getMessage());
        }
        return null;
    }
}
