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
public class TrackDto {
    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("duration_ms")
    private int durationMs;
    @JsonProperty("artists")
    private List<ArtistDto> artistDtoList;
    @JsonProperty("external_urls")
    private ExternalUrlsDto externalUrls;
}