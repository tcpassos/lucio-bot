package com.discord.bot.service;

import java.awt.Color;

import org.springframework.stereotype.Service;

import com.discord.bot.audioplayer.GuildPlaybackManager;
import com.discord.bot.dto.MultipleMusicDto;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.discord.bot.service.audioplayer.SfxService;
import com.discord.bot.service.audioplayer.SfxType;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Service
@AllArgsConstructor
public class MusicService {

    private final PlayerManagerService playerManagerService;
    private final SfxService sfxService;
    private final MessageService messageService;
    private final MusicCommandUtils utils;

    public void playMusic(SlashCommandInteractionEvent event, MultipleMusicDto multipleMusicDto) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        AudioChannel userChannel = getAudioChannel(event, false);
        AudioChannel botChannel = getAudioChannel(event, true);

        if (userChannel != null) {
            int trackSize = multipleMusicDto.getMusicDtoList().size();
            if (trackSize != 0) {
                if (botChannel == null) {
                    GuildPlaybackManager musicManager = playerManagerService.getPlaybackManager(event.getGuild());
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
}
