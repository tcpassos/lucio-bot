package com.discord.bot.dto.youtube;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class YoutubeResponse {
    private List<ItemDto> items;
}