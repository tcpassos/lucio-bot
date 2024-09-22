package com.discord.bot.commands.musiccommands;

import java.awt.Color;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.MessageService;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.discord.bot.service.audioplayer.SfxService;
import com.discord.bot.service.audioplayer.SfxType;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@AllArgsConstructor
public class VolumeCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;
    MessageService messageService;
    SfxService sfxService;
    MusicCommandUtils utils;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        
        if (utils.channelControl(event)) {
            AudioPlayer musicPlayer = playerManagerService.getAudioManager(event.getGuild()).musicPlayer;
            var volumeOption = event.getOption("volume");

            int previousVolume = musicPlayer.getVolume();
            int volume = volumeOption == null ? previousVolume : volumeOption.getAsInt();

            if (volume < 0 || volume > 100) {
                embedBuilder.setDescription(messageService.getMessage("bot.song.volume.invalid")).setColor(Color.RED);
                event.replyEmbeds(embedBuilder.build())
                     .setEphemeral(true)
                     .queue();
                return;
            }

            if (volumeOption != null) {
                musicPlayer.setVolume(volume);
            }

            // Play a sound when the volume is increased or is too low
            if (previousVolume < volume) {
                playerManagerService.loadAndPlaySfx(event.getGuild(), sfxService.getRandomSound(SfxType.VOLUME_UP));
            }
            if (previousVolume > volume && volume < 30) {
                playerManagerService.loadAndPlaySfx(event.getGuild(), sfxService.getRandomSound(SfxType.VOLUME_LOW));
            }

            embedBuilder.setTitle("Volume")
                    .setDescription(messageService.getMessage("bot.song.volume.set", volume))
                    .setColor(Color.GREEN);
        } else {
            embedBuilder.setDescription(messageService.getMessage("bot.user.notinsamevoice")).setColor(Color.RED);
        }

        event.replyEmbeds(embedBuilder.build())
                .setEphemeral(true)
                .queue();
    }
}