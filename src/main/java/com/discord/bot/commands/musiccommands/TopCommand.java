package com.discord.bot.commands.musiccommands;

import java.util.Objects;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.dto.MusicDto;
import com.discord.bot.service.MessageService;
import com.discord.bot.service.SpotifyService;
import com.discord.bot.service.YoutubeService;
import com.discord.bot.service.audioplayer.PlayerManagerService;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@AllArgsConstructor
public class TopCommand implements ISlashCommand {

    MessageService messageService;
    YoutubeService youtubeService;
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
                             .map(music -> music.getTitle())
                             .map(youtubeService::searchVideoUrl)
                             .filter(Objects::nonNull)
                             .toList();

        if (songs.isEmpty()) {
            event.getHook().sendMessageEmbeds(messageService.getEmbedError("api.youtube.limit").build())
                 .setEphemeral(true)
                 .queue();
            return;
        }

        if (playerManagerService.joinAudioChannel(event)) {
            for (String song : songs) {
                playerManagerService.loadAndPlayMusic(event, new MusicDto(null, song));
            }
        }
    }
}
