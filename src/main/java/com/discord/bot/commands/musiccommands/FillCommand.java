package com.discord.bot.commands.musiccommands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.dto.MusicDto;
import com.discord.bot.service.MessageService;
import com.discord.bot.service.MusicService;
import com.discord.bot.service.RestService;
import com.discord.bot.service.SpotifyService;
import com.discord.bot.service.audioplayer.PlayerManagerService;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@AllArgsConstructor
public class FillCommand implements ISlashCommand {

    MessageService messageService;
    RestService restService;
    SpotifyService spotifyService;
    MusicService musicService;
    PlayerManagerService playerManagerService;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        var amountOption = event.getOption("amount");
        int amount = amountOption != null ? amountOption.getAsInt() : 5;
        event.deferReply().queue();

        var queue = playerManagerService.getAudioManager(event.getGuild()).musicScheduler.queue;
        List<MusicDto> queueTracks = new ArrayList<>();
        queue.forEach(track -> queueTracks.add(new MusicDto(track.getInfo().author + " - " + track.getInfo().title, null)));

        if (queue.isEmpty()) {
            event.getHook().sendMessageEmbeds(messageService.getEmbed("bot.queue.empty").setColor(Color.RED).build())
                 .setEphemeral(true)
                 .queue();
            return;
        }
        
        var recommendations = spotifyService.getRecommendationsForTrackDtos(queueTracks, amount);
        var songs = restService.getYoutubeUrl(recommendations, event.getGuild().getIdLong());
        if (songs.getCount() == 0) {
            event.getHook().sendMessageEmbeds(messageService.getEmbed("api.youtube.limit").setColor(Color.RED).build())
                 .setEphemeral(true)
                 .queue();
            return;
        }

        musicService.playMusic(event, songs);
    }
}
