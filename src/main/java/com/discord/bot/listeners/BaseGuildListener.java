package com.discord.bot.listeners;

import com.discord.bot.context.GuildContext;
import com.discord.bot.context.GuildContextHolder;
import com.discord.bot.entity.GuildConfig;
import com.discord.bot.repository.GuildConfigRepository;

import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public abstract class BaseGuildListener extends ListenerAdapter {
    protected final GuildConfigRepository guildRepository;

    public BaseGuildListener(GuildConfigRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onGenericGuild(GenericGuildEvent event) {
        try {
            Long guildId = event.getGuild().getIdLong();
            GuildConfig guildConfig = guildRepository.findById(guildId)
                .orElseGet(() -> {
                    GuildConfig newConfig = new GuildConfig(guildId);
                    guildRepository.save(newConfig);
                    return newConfig;
                });

            GuildContextHolder.setGuildContext(new GuildContext(guildId, guildConfig));
        } finally {
            GuildContextHolder.clearGuildContext();
        }
    }
}
