package com.discord.bot.activity;

import com.discord.bot.service.MessageService;

import net.dv8tion.jda.api.events.user.UserActivityStartEvent;
import net.dv8tion.jda.api.events.user.update.GenericUserPresenceEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ActivityListener extends ListenerAdapter {

    final MessageService messageService;

    public ActivityListener(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void onGenericUserPresence(GenericUserPresenceEvent event) {
        System.out.println("User presence changed");
    }

    @Override
    public void onUserActivityStart(UserActivityStartEvent event) {
        System.out.println("User activity started");
        var activity = event.getNewActivity();

        if (activity != null && activity.getName().contains("Overwatch")) {
            var textChannel = event.getGuild().getDefaultChannel().asTextChannel();
            var message = messageService.getMessage("activity.playing.overwatch", event.getUser().getAsMention());
            textChannel.sendMessage(message).queue();
        }
    }

}
