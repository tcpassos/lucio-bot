package com.discord.bot.entity;

import jakarta.persistence.*;
import lombok.*;

@SuppressWarnings("JpaDataSourceORMInspection")
@Table(name = "music")
@Entity
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class Music {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="spotify_id", unique=true)
    private String spotifyId;

    @Column(name="youtube_id")
    private String youtubeId;

    private String title;
    private String artist;
    private long durationMs;

    public Music(String spotifyId, String youtubeId, String title, String artist, long durationMs) {
        this.spotifyId = spotifyId;
        this.youtubeId = youtubeId;
        this.title = title;
        this.artist = artist;
        this.durationMs = durationMs;
    }
}