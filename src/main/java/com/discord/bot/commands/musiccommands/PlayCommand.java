package com.discord.bot.commands.musiccommands;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.dto.MusicDto;
import com.discord.bot.service.MessageService;
import com.discord.bot.service.SpotifyService;
import com.discord.bot.service.YoutubeService;
import com.discord.bot.service.audioplayer.PlayerManagerService;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@AllArgsConstructor
public class PlayCommand implements ISlashCommand {
    MessageService messageService;
    YoutubeService youtubeService;
    SpotifyService spotifyService;
    PlayerManagerService playerManagerService;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        var queryOption = event.getOption("query");
        event.deferReply().queue();
    
        String query = queryOption.getAsString().trim();
        var music = getMusicFromUrl(query);
    
        if (music == null) {
            event.getHook().sendMessageEmbeds(messageService.getEmbedError("api.youtube.limit").build())
                           .setEphemeral(true)
                           .queue();
            return;
        }
    
        if (playerManagerService.joinAudioChannel(event)) {
            playerManagerService.loadAndPlayMusic(event, music);
        }
    }

    private MusicDto getMusicFromUrl(String query) {
        if (query.contains("https://www.youtube.com/shorts/")) {
            // Convert shorts link to watch link
            query = query.replace("shorts/", "watch?v=");
        }
        if (isSupportedUrl(query)) {
            return new MusicDto(null, query);
        }
        String url = youtubeService.searchVideoUrl(query);
        if (url != null) {
            return new MusicDto(null, url);
        }
        return null;
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
}