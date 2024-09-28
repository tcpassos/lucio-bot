package com.discord.bot.context;

import com.discord.bot.entity.GuildConfig;

public class GuildContext {
    private Long guildId;
    private GuildConfig guildConfig;

    public GuildContext(Long guildId, GuildConfig guildConfig) {
        this.guildId = guildId;
        this.guildConfig = guildConfig;
    }

    public Long getGuildId() {
        return guildId;
    }

    public GuildConfig getGuildConfig() {
        return guildConfig;
    }
}