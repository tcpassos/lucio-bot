package com.discord.bot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class MusicDto {
    private String title;
    private String reference;
    private String originalUrl;

    public MusicDto(String title, String reference) {
        this.title = title;
        this.reference = reference;
    }
}