package com.discord.bot.commands.musiccommands;

import com.discord.bot.audioplayer.GuildAudioManager;
import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.dto.MultipleMusicDto;
import com.discord.bot.dto.MusicDto;
import com.discord.bot.service.MessageService;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.RestService;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.discord.bot.service.audioplayer.SfxService;
import com.discord.bot.service.audioplayer.SfxType;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class PlayCommand implements ISlashCommand {
    RestService restService;
    PlayerManagerService playerManagerService;
    MessageService messageService;
    SfxService sfxService;
    MusicCommandUtils utils;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        var queryOption = event.getOption("query");
        event.deferReply().queue();

        assert queryOption != null;
        String query = queryOption.getAsString().trim();
        MultipleMusicDto multipleMusicDto = getSongUrl(query, event.getGuild().getIdLong());
        if (multipleMusicDto.getCount() == 0) {
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                            .setDescription(messageService.getMessage("api.youtube.limit"))
                            .setColor(Color.RED)
                            .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }
        playMusic(event, multipleMusicDto);
    }

    private void playMusic(SlashCommandInteractionEvent event, MultipleMusicDto multipleMusicDto) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        AudioChannel userChannel = getAudioChannel(event, false);
        AudioChannel botChannel = getAudioChannel(event, true);

        if (userChannel != null) {
            int trackSize = multipleMusicDto.getMusicDtoList().size();
            if (trackSize != 0) {
                if (botChannel == null) {
                    GuildAudioManager musicManager = playerManagerService.getAudioManager(event.getGuild());
                    utils.playerCleaner(musicManager);

                    if (!userChannel.getGuild().getSelfMember().hasPermission(userChannel, Permission.VOICE_CONNECT)) {
                        event.getHook().sendMessageEmbeds(new EmbedBuilder()
                                        .setDescription(messageService.getMessage("bot.voice.notallowed"))
                                        .setColor(Color.RED)
                                        .build())
                                .setEphemeral(true)
                                .queue();
                        return;
                    }
                    userChannel.getGuild().getAudioManager().openAudioConnection(userChannel);
                    botChannel = userChannel;
                    
                    // Play a sound when the track starts playing
                    playerManagerService.loadAndPlaySfx(event.getMember(), sfxService.getSound(SfxType.MUSIC_START));
                }
                if (botChannel.equals(userChannel)) {
                    if (trackSize == 1) playerManagerService.loadAndPlay(event, multipleMusicDto.getMusicDtoList().get(0));
                    else playerManagerService.loadMultipleAndPlay(event, multipleMusicDto);
                } else
                    embedBuilder.setDescription(messageService.getMessage("bot.user.notinsamevoice")).setColor(Color.RED);
            } else embedBuilder.setDescription(messageService.getMessage("bot.notrackfound")).setColor(Color.RED);
        } else embedBuilder.setDescription(messageService.getMessage("bot.user.notinvoice")).setColor(Color.RED);

        if (!embedBuilder.isEmpty())
            event.getHook().sendMessageEmbeds(embedBuilder.build()).setEphemeral(true).queue();
    }

    private AudioChannel getAudioChannel(SlashCommandInteractionEvent event, boolean self) {
        AudioChannelUnion audioChannel = null;

        var member = event.getMember();
        if (member != null) {
            if (self) {
                member = member.getGuild().getSelfMember();
            }
            var voiceState = member.getVoiceState();
            if (voiceState != null) {
                audioChannel = voiceState.getChannel();
            }
        }

        return audioChannel;
    }

    private MultipleMusicDto getSongUrl(String query, Long guildId) {
        List<MusicDto> musicDtos = new ArrayList<>();
        if (query.contains("https://www.youtube.com/shorts/")) query = youtubeShortsToVideo(query);
        if (isSupportedUrl(query)) {
            musicDtos.add(new MusicDto(null, query));
            return new MultipleMusicDto(1, musicDtos, 0);
        } else if (query.contains("https://open.spotify.com/")) {
            musicDtos = restService.getTracksFromSpotify(query);
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