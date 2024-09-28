package com.discord.bot.listeners;

import com.discord.bot.entity.GuildConfig;
import com.discord.bot.repository.GuildConfigRepository;
import com.discord.bot.service.MessageService;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;

public class ActivityListener extends BaseGuildListener {

    final MessageService messageService;
    final GuildConfigRepository guildConfigRepository;

    public ActivityListener(MessageService messageService, GuildConfigRepository guildConfigRepository) {
        super(guildConfigRepository);
        this.messageService = messageService;
        this.guildConfigRepository = guildConfigRepository;
    }

    @Override
    public void onUserActivityStart(UserActivityStartEvent event) {
        var activity = event.getNewActivity();

        if (activity != null && activity.getName().contains("Overwatch")) {
            var guild = event.getGuild();
            GuildConfig guildConfig = guildConfigRepository.findByGuildId(guild.getIdLong());
            if (guildConfig == null || guildConfig.getGameTextChannelId() == null) {
                return;
            }

            var message = messageService.getMessage("activity.playing.overwatch", event.getUser().getAsMention());
            TextChannel gameChannel = guild.getTextChannelById(guildConfig.getGameTextChannelId());
            if (gameChannel != null) {
                gameChannel.sendMessage(message).queue();
            }
        }
    }
}
