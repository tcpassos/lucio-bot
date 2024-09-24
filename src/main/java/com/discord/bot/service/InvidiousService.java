package com.discord.bot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Service
public class InvidiousService {
    private final static Logger logger = LoggerFactory.getLogger(InvidiousService.class);
    private final RestTemplate restTemplate;
    private final List<String> mirrors = Arrays.asList(
            "https://invidious.nerdvpn.de",
            "https://inv.nadeko.net",
            "https://invidious.privacyredirect.com",
            "https://invidious.jing.rocks"
    );

    public InvidiousService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public String searchVideoId(String query) {
        for (String mirror : mirrors) {
            try {
                String url = mirror + "/api/v1/search?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
                InvidiousSearchResult[] results = restTemplate.getForObject(url, InvidiousSearchResult[].class);

                if (results != null && results.length > 0) {
                    return results[0].getVideoId();
                }
            } catch (Exception e) {
                logger.error("Failed to get result from " + mirror + ": " + e.getMessage());
            }
        }
        return null;
    }

    public static class InvidiousSearchResult {
        private String title;
        private String videoId;

        public String getTitle() {
            return title;
        }

        public String getVideoId() {
            return videoId;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setVideoId(String videoId) {
            this.videoId = videoId;
        }
    }
}
