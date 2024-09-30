package com.discord.bot.commands.musiccommands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.MessageService;
import com.discord.bot.service.SpotifyService;
import com.discord.bot.service.audioplayer.PlayerManagerService;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@AllArgsConstructor
public class FillCommand implements ISlashCommand {

    MessageService messageService;
    SpotifyService spotifyService;
    PlayerManagerService playerManagerService;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        var amountOption = event.getOption("amount");
        int amount = amountOption != null ? amountOption.getAsInt() : 5;
        event.deferReply().queue();

        var queue = playerManagerService.getPlaybackManager(event.getGuild()).musicScheduler.queue;
        if (queue.isEmpty()) {
            event.getHook().sendMessageEmbeds(messageService.getEmbed("bot.queue.empty").setColor(Color.RED).build())
                 .setEphemeral(true)
                 .queue();
            return;
        }

        List<String> queries = new ArrayList<>();
        queue.forEach(track -> queries.add(track.getInfo().author + " - " + track.getInfo().title));
        
        var recommendations = spotifyService.getRecommendationsForTrackNames(queries, amount);
        var songs = recommendations.stream()
                                   .map(track -> track.getExternalUrls().getSpotify())
                                   .toList();

        if (songs.isEmpty()) {
            event.getHook().sendMessageEmbeds(messageService.getEmbedError("bot.song.nomatches").build())
                 .setEphemeral(true)
                 .queue();
            return;
        }

        if (playerManagerService.joinAudioChannel(event)) {
            for (String song : songs) {
                playerManagerService.loadAndPlayMusic(event, song);
            }
        }
    }
}
