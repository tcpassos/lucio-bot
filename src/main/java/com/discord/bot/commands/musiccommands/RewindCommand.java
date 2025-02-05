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
public class RewindCommand implements ISlashCommand {
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
            var songPosition = track.getPosition();
            if (songPosition - (seconds * 1000L) <= 0) {
                track.setPosition(0);
                embedBuilder.setDescription(messageService.getMessage("bot.song.rewinded", seconds)).setColor(Color.GREEN);
            } else {
                track.setPosition(songPosition - (seconds * 1000L));
                embedBuilder.setDescription(messageService.getMessage("bot.song.rewinded.start")).setColor(Color.GREEN);
            }
        } else embedBuilder.setDescription(messageService.getMessage("bot.user.notinsamevoice")).setColor(Color.RED);

        event.replyEmbeds(embedBuilder.build()).setEphemeral(false).queue();
    }
}