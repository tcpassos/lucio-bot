package com.discord.bot.dto.Response.Spotify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class SpotifyItemDto {
    @JsonProperty("track")
    TrackDto trackDtoList;
}