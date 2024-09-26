package com.discord.bot.commands.musiccommands;

import com.discord.bot.service.MessageService;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.discord.bot.commands.ISlashCommand;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

@AllArgsConstructor
public class LoopCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;
    MessageService messageService;
    MusicCommandUtils utils;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (utils.channelControl(event)) {
            boolean repeat = playerManagerService.getPlaybackManager(event.getGuild()).musicScheduler.repeating;
            playerManagerService.getPlaybackManager(event.getGuild()).musicScheduler.repeating = !repeat;

            if (!repeat) embedBuilder.setDescription(messageService.getMessage("bot.song.loop.enabled")).setColor(Color.GREEN);
            else embedBuilder.setDescription(messageService.getMessage("bot.song.loop.disabled")).setColor(Color.RED);
        } else embedBuilder.setDescription(messageService.getMessage("bot.user.notinsamevoice")).setColor(Color.RED);

        event.replyEmbeds(embedBuilder.build()).setEphemeral(false).queue();
    }
}