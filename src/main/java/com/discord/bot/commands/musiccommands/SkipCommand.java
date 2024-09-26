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
public class SkipCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;
    MessageService messageService;
    MusicCommandUtils utils;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        var ephemeralOption = event.getOption("ephemeral");
        boolean ephemeral = ephemeralOption == null || ephemeralOption.getAsBoolean();

        if (utils.channelControl(event)) {
            playerManagerService.getPlaybackManager(event.getGuild()).musicScheduler.nextTrack();
            embedBuilder.setDescription(messageService.getMessage("bot.song.skipped")).setColor(Color.GREEN);
        } else embedBuilder.setDescription(messageService.getMessage("bot.user.notinsamevoice")).setColor(Color.RED);

        event.replyEmbeds(embedBuilder.build()).setEphemeral(ephemeral).queue();
    }
}