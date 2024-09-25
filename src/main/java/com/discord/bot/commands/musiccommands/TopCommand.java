package com.discord.bot.commands.musiccommands;

import java.awt.Color;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.MessageService;
import com.discord.bot.service.MusicService;
import com.discord.bot.service.RestService;
import com.discord.bot.service.SpotifyService;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@AllArgsConstructor
public class TopCommand implements ISlashCommand {

    MessageService messageService;
    RestService restService;
    SpotifyService spotifyService;
    MusicService musicService;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        var artistOption = event.getOption("artist");
        var amountOption = event.getOption("amount");

        event.deferReply().queue();

        String artist = artistOption.getAsString().trim();
        int amount = amountOption != null ? amountOption.getAsInt() : 5;
        var topTracks = spotifyService.getTopTracksFromArtist(artist, amount);
        var songs = restService.getYoutubeUrl(topTracks, event.getGuild().getIdLong());

        if (songs.getCount() == 0) {
            event.getHook().sendMessageEmbeds(messageService.getEmbed("api.youtube.limit").setColor(Color.RED).build())
                 .setEphemeral(true)
                 .queue();
            return;
        }

        musicService.playMusic(event, songs);
    }
}
