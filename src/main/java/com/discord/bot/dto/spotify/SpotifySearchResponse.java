package com.discord.bot.dto.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class SpotifySearchResponse {
    @JsonProperty("artists")
    private ArtistSearchDto artistSearchDto;
    @JsonProperty("tracks")
    private TrackSearchDto trackSearchDto;
}