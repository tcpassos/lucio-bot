package com.discord.bot.commands.musiccommands;

import com.discord.bot.audioplayer.GuildPlaybackManager;
import com.discord.bot.service.MessageService;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.discord.bot.commands.ISlashCommand;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.awt.*;

@AllArgsConstructor
public class LeaveCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;
    MessageService messageService;
    MusicCommandUtils utils;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (utils.isSameAudioChannel(event)) {
            GuildPlaybackManager musicManager = playerManagerService.getPlaybackManager(event.getGuild());
            @SuppressWarnings("DataFlowIssue")
            AudioManager audioManager = event.getGuild().getAudioManager();
            utils.playerCleaner(musicManager);
            audioManager.closeAudioConnection();

            embedBuilder.setDescription(messageService.getMessage("bot.bye")).setColor(Color.GREEN);
        } else embedBuilder.setDescription(messageService.getMessage("bot.user.notinsamevoice")).setColor(Color.RED);

        event.replyEmbeds(embedBuilder.build()).setEphemeral(false).queue();
    }
}