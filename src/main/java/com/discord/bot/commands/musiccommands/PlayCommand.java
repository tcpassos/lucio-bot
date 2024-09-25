package com.discord.bot.commands.musiccommands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.dto.MultipleMusicDto;
import com.discord.bot.dto.MusicDto;
import com.discord.bot.service.MessageService;
import com.discord.bot.service.MusicService;
import com.discord.bot.service.RestService;
import com.discord.bot.service.SpotifyService;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@AllArgsConstructor
public class PlayCommand implements ISlashCommand {
    MessageService messageService;
    RestService restService;
    SpotifyService spotifyService;
    MusicService musicService;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        var queryOption = event.getOption("query");
        event.deferReply().queue();

        assert queryOption != null;
        String query = queryOption.getAsString().trim();
        MultipleMusicDto multipleMusicDto = getSongUrl(query, event.getGuild().getIdLong());
        if (multipleMusicDto.getCount() == 0) {
            event.getHook().sendMessageEmbeds(messageService.getEmbed("api.youtube.limit").setColor(Color.RED).build())
                           .setEphemeral(true)
                           .queue();
            return;
        }
        musicService.playMusic(event, multipleMusicDto);
    }

    private MultipleMusicDto getSongUrl(String query, Long guildId) {
        List<MusicDto> musicDtos = new ArrayList<>();
        if (query.contains("https://www.youtube.com/shorts/")) query = youtubeShortsToVideo(query);
        if (isSupportedUrl(query)) {
            musicDtos.add(new MusicDto(null, query));
            return new MultipleMusicDto(1, musicDtos, 0);
        } else if (query.contains("https://open.spotify.com/")) {
            musicDtos = spotifyService.getTracksFromSpotify(query);
            return restService.getYoutubeUrl(musicDtos, guildId);
        } else {
            return restService.getYoutubeUrl(new MusicDto(query, null), guildId);
        }
    }

    private boolean isSupportedUrl(String url) {
        return (url.contains("https://www.youtube.com/watch?v=")
                || url.contains("https://youtu.be/")
                || url.contains("https://youtube.com/playlist?list=")
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