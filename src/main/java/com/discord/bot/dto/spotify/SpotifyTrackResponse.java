package com.discord.bot.dto.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class SpotifyTrackResponse {
    @JsonProperty("artists")
    private List<ArtistDto> artistDtoList;
    @JsonProperty("name")
    private String songName;
    @JsonProperty("duration_ms")
    private int durationMs;
    @JsonProperty("external_urls")
    private ExternalUrlsDto externalUrls;
}