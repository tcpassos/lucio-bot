package com.discord.bot.dto.response.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class SpotifyPlaylistResponse {
    @JsonProperty("name")
    private String name;
    @JsonProperty("external_urls")
    private ExternalUrlsDto externalUrls;
    @JsonProperty("tracks")
    private PlaylistTracksDto tracks;
}