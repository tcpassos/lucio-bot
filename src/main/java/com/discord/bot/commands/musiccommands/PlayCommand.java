package com.discord.bot.commands.musiccommands;

import java.util.ArrayList;
import java.util.List;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.dto.MultipleMusicDto;
import com.discord.bot.dto.MusicDto;
import com.discord.bot.service.MessageService;
import com.discord.bot.service.MusicService;
import com.discord.bot.service.YoutubeService;
import com.discord.bot.service.SpotifyService;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@AllArgsConstructor
public class PlayCommand implements ISlashCommand {
    MessageService messageService;
    YoutubeService restService;
    SpotifyService spotifyService;
    MusicService musicService;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        var queryOption = event.getOption("query");
        event.deferReply().queue();
    
        if (queryOption == null) {
            event.getHook().sendMessageEmbeds(messageService.getEmbedError("api.youtube.limit").build())
                           .setEphemeral(true)
                           .queue();
            return;
        }
    
        String query = queryOption.getAsString().trim();
        MultipleMusicDto multipleMusicDto = getSongUrl(query);
    
        if (multipleMusicDto.getCount() == 0) {
            event.getHook().sendMessageEmbeds(messageService.getEmbedError("api.youtube.limit").build())
                           .setEphemeral(true)
                           .queue();
            return;
        }
    
        if (multipleMusicDto.getCount() == 1) {
            MusicDto musicDto = multipleMusicDto.getMusicDtoList().get(0);
            String originalUrl = musicDto.getOriginalUrl() != null ? musicDto.getOriginalUrl() : query;
            String message = (musicDto.getTitle() == null) 
                ? String.format("Adicionando à fila :musical_note: %s :musical_note:", originalUrl)
                : String.format("Adicionando à fila :musical_note: [%s](%s) :musical_note:", musicDto.getTitle(), originalUrl);
    
            event.getHook().sendMessage(message).queue();
        } else if (query.startsWith("https://open.spotify.com/playlist/")) {
            String message = String.format("Adicionando à fila :musical_note: %s :musical_note:", query);
            event.getHook().sendMessage(message).queue();
        }
    
        musicService.playMusic(event, multipleMusicDto);
    }

    private MultipleMusicDto getSongUrl(String query) {
        List<MusicDto> musicDtos = new ArrayList<>();

        if (query.contains("https://www.youtube.com/shorts/")) {
            query = youtubeShortsToVideo(query);
        }
        if (isSupportedUrl(query)) {
            musicDtos.add(new MusicDto(null, query, query));
            return new MultipleMusicDto(1, musicDtos, 0);
        }
        return restService.getYoutubeUrl(musicDtos);
    }

    private boolean isSupportedUrl(String url) {
        return (url.contains("https://www.youtube.com/watch?v=")
                || url.contains("https://youtu.be/")
                || url.contains("https://youtube.com/playlist?list=")
                || url.contains("https://open.spotify.com/")
                || url.contains("https://music.youtube.com/watch?v=")
                || url.contains("https://music.youtube.com/playlist?list=")
                || url.contains("https://www.twitch.tv/")
                || url.contains("https://soundcloud.com/")
        );
    }

    private String youtubeShortsToVideo(String url) {
        return url.replace("shorts/", "watch?v=");
    }
}