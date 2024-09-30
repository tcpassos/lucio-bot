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
public class SpotifyTracksResponse {
    @JsonProperty("tracks")
    private List<TrackDto> tracks;
}