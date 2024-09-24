package com.discord.bot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "guild_config")
public class GuildConfig {

    @Id
    private Long guildId;

    @Column(name = "youtube_api_key")
    private String youtubeApiKey;

    @Column(name = "spotify_client_id")
    private String spotifyClientId;

    @Column(name = "spotify_client_secret")
    private String spotifyClientSecret;

    @Column(name = "game_text_channel_id")
    private Long gameTextChannelId;

    public GuildConfig() {}

    public GuildConfig(Long guildId) {
        this.guildId = guildId;
    }

    public Long getGuildId() {
        return guildId;
    }

    public String getYoutubeApiKey() {
        return youtubeApiKey;
    }

    public void setYoutubeApiKey(String youtubeApiKey) {
        this.youtubeApiKey = youtubeApiKey;
    }

    public String getSpotifyClientId() {
        return spotifyClientId;
    }

    public void setSpotifyClientId(String spotifyClientId) {
        this.spotifyClientId = spotifyClientId;
    }

    public String getSpotifyClientSecret() {
        return spotifyClientSecret;
    }

    public void setSpotifyClientSecret(String spotifyClientSecret) {
        this.spotifyClientSecret = spotifyClientSecret;
    }

    public Long getGameTextChannelId() {
        return gameTextChannelId;
    }

    public void setGameTextChannelId(Long gameTextChannelId) {
        this.gameTextChannelId = gameTextChannelId;
    }
}
