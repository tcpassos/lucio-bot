package com.discord.bot.service;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class YoutubeService {
    private final InvidiousService invidiousService;
    private final YouTubeApiService youTubeApiService;

    public String searchVideoId(String query) {
        String videoId = invidiousService.searchVideoId(query);
        if (videoId == null) {
            videoId = youTubeApiService.searchVideoId(query);
        }
        return videoId;
    }

    public String searchVideoUrl(String query) {
        String videoId = searchVideoId(query);
        return videoId != null ? "https://www.youtube.com/watch?v=" + videoId : null;
    }
}