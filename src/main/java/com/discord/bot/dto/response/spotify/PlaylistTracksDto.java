package com.discord.bot.dto.response.spotify;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class PlaylistTracksDto {
    @JsonProperty("items")
    private List<PlaylistTrackDto> items;
}