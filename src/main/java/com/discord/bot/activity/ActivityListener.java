package com.discord.bot.activity;

import com.discord.bot.service.MessageService;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ActivityListener extends ListenerAdapter {

    final MessageService messageService;
    final String gameChannelId;

    public ActivityListener(MessageService messageService, String gameChannelId) {
        this.messageService = messageService;
        this.gameChannelId = gameChannelId;
    }

    @Override
    public void onUserActivityStart(UserActivityStartEvent event) {
        var activity = event.getNewActivity();

        if (activity != null && activity.getName().contains("Overwatch")) {
            var guild = event.getGuild();
            var message = messageService.getMessage("activity.playing.overwatch", event.getUser().getAsMention());

            TextChannel gameChannel = guild.getTextChannelById(gameChannelId);
            if (gameChannel != null) {
                gameChannel.sendMessage(message).queue();
            }
        }
    }
}
