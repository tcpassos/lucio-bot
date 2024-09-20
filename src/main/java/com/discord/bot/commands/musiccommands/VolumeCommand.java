package com.discord.bot.commands.musiccommands;

import java.awt.Color;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@AllArgsConstructor
public class VolumeCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;
    MusicCommandUtils utils;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        
        if (utils.channelControl(event)) {
            AudioPlayer musicPlayer = playerManagerService.getAudioManager(event.getGuild()).musicPlayer;
            var volumeOption = event.getOption("volume");
            int volume = volumeOption == null ? musicPlayer.getVolume() : volumeOption.getAsInt();

            if (volume < 0 || volume > 100) {
                embedBuilder.setDescription("O volume deve estar entre 0 e 100.").setColor(Color.RED);
                event.replyEmbeds(embedBuilder.build())
                     .setEphemeral(true)
                     .queue();
                return;
            }

            if (volumeOption != null) {
                musicPlayer.setVolume(volume);
            }

            embedBuilder.setTitle("Volume")
                    .setDescription(":loud_sound: Volume definido para " + volume + "%.")
                    .setColor(Color.GREEN);
        } else {
            embedBuilder.setDescription("Você precisa estar no mesmo canal de voz que o bot.").setColor(Color.RED);
        }

        event.replyEmbeds(embedBuilder.build())
                .setEphemeral(true)
                .queue();
    }
}