package com.discord.bot.service;

import org.springframework.stereotype.Service;

import com.discord.bot.dto.MultipleMusicDto;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.discord.bot.service.audioplayer.SfxService;
import com.discord.bot.service.audioplayer.SfxType;

import lombok.AllArgsConstructor;
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
        AudioChannel userChannel = getAudioChannel(event, false);
        AudioChannel botChannel = getAudioChannel(event, true);
    
        // User is not in a voice channel
        if (userChannel == null) {
            event.getHook().sendMessageEmbeds(messageService.getEmbedError("bot.user.notinvoice").build()).setEphemeral(true).queue();
            return;
        }
    
        // No track found
        int trackSize = multipleMusicDto.getMusicDtoList().size();
        if (trackSize == 0) {
            event.getHook().sendMessageEmbeds(messageService.getEmbedError("bot.notrackfound").build()).setEphemeral(true).queue();
            return;
        }
    
        // If bot is not in a voice channel, join the user's voice channel
        if (botChannel == null) {
            utils.playerCleaner(playerManagerService.getPlaybackManager(event.getGuild()));
            if (!playerManagerService.joinVoiceChannel(userChannel)) {
                event.getHook().sendMessageEmbeds(messageService.getEmbedError("bot.voice.notallowed").build()).setEphemeral(true).queue();
                return;
            }
            botChannel = userChannel;
            playerManagerService.loadAndPlaySfx(botChannel, sfxService.getSound(SfxType.MUSIC_START));
        }
    
        // Bot is in a different voice channel
        if (!botChannel.equals(userChannel)) {
            event.getHook().sendMessageEmbeds(messageService.getEmbedError("bot.user.notinsamevoice").build()).setEphemeral(true).queue();
            return;
        }
    
        // Play the track
        if (trackSize == 1) {
            playerManagerService.loadAndPlay(event, multipleMusicDto.getMusicDtoList().get(0));
        } else {
            playerManagerService.loadMultipleAndPlay(event, multipleMusicDto);
        }
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
