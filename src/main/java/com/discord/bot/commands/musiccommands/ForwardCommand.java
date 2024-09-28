package com.discord.bot.commands.musiccommands;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.MessageService;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

@AllArgsConstructor
public class ForwardCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;
    MessageService messageService;
    MusicCommandUtils utils;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (utils.isSameAudioChannel(event)) {
            var track = playerManagerService.getPlaybackManager(event.getGuild()).musicPlayer.getPlayingTrack();
            var option = event.getOption("sec");
            var seconds = option != null ? option.getAsInt() : 0;
            track.setPosition(track.getPosition() + (seconds * 1000L));

            embedBuilder.setDescription(messageService.getMessage("bot.song.forwarded", seconds)).setColor(Color.GREEN);
        } else embedBuilder.setDescription(messageService.getMessage("bot.user.notinsamevoice")).setColor(Color.RED);

        event.replyEmbeds(embedBuilder.build()).setEphemeral(false).queue();
    }
}