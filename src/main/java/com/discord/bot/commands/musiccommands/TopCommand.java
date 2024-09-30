package com.discord.bot.commands.musiccommands;

import java.util.Objects;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.MessageService;
import com.discord.bot.service.SpotifyService;
import com.discord.bot.service.audioplayer.PlayerManagerService;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@AllArgsConstructor
public class TopCommand implements ISlashCommand {

    MessageService messageService;
    SpotifyService spotifyService;
    PlayerManagerService playerManagerService;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        var artistOption = event.getOption("artist");
        var amountOption = event.getOption("amount");

        event.deferReply().queue();

        String artist = artistOption.getAsString().trim();
        int amount = amountOption != null ? amountOption.getAsInt() : 5;
        var topTracks = spotifyService.getTopTracksFromArtist(artist, amount);
        var songs = topTracks.stream()
                             .map(track -> track.getExternalUrls().getSpotify())
                             .filter(Objects::nonNull)
                             .toList();

        if (songs.isEmpty()) {
            event.getHook().sendMessageEmbeds(messageService.getEmbedError("bot.song.nomatches").build())
                 .setEphemeral(true)
                 .queue();
            return;
        }

        playerManagerService.loadAndPlayMusic(event, songs);
    }
}
