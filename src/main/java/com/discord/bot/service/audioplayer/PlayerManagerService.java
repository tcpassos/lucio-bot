package com.discord.bot.service.audioplayer;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.discord.bot.audioplayer.GuildAudioManager;
import com.discord.bot.dto.MultipleMusicDto;
import com.discord.bot.dto.MusicDto;
import com.discord.bot.entity.Music;
import com.discord.bot.repository.MusicRepository;
import com.discord.bot.service.MessageService;
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import dev.lavalink.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;

@Service
public class PlayerManagerService {
    private final static Logger logger = LoggerFactory.getLogger(PlayerManagerService.class);
    private final Map<Long, GuildAudioManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;
    final MusicRepository musicRepository;
    final MessageService messageService;

    public PlayerManagerService(MusicRepository musicRepository, MessageService messageService) {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();

        // The default implementation of YoutubeAudioSourceManager is no longer supported, I'm using the LavaLink implementation
        YoutubeAudioSourceManager yt = new YoutubeAudioSourceManager(true);
        this.audioPlayerManager.registerSourceManager(yt);

        this.audioPlayerManager.getConfiguration().setOutputFormat(StandardAudioDataFormats.DISCORD_PCM_S16_BE);

        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
        this.musicRepository = musicRepository;
        this.messageService = messageService;
    }

    public GuildAudioManager getAudioManager(Guild guild) {
        if (guild != null) {
            return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
                final GuildAudioManager guildMusicManager = new GuildAudioManager(this.audioPlayerManager, guild);

                guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());

                return guildMusicManager;
            });
        }

        return null;
    }

    public void loadAndPlay(SlashCommandInteractionEvent event, MusicDto musicDto, boolean ephemeral) {
        final GuildAudioManager musicManager = this.getAudioManager(event.getGuild());
        EmbedBuilder embedBuilder = new EmbedBuilder();
        this.audioPlayerManager.loadItemOrdered(musicManager, musicDto.getReference(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                event.getHook().sendMessageEmbeds(embedBuilder
                                .setDescription(messageService.getMessage("bot.queue.added.song", track.getInfo().title, musicManager.musicScheduler.queue.size() + 1))
                                .setColor(Color.GREEN)
                                .build())
                        .setEphemeral(ephemeral)
                        .queue();

                musicManager.musicScheduler.queue(track);
                saveTrack(track, musicDto.getTitle());
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                List<AudioTrack> tracks = playlist.getTracks();

                for (AudioTrack track : tracks) {
                    musicManager.musicScheduler.queue(track);
                }
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                                .setDescription(messageService.getMessage("bot.queue.added.playlist", tracks.size()))
                                .setColor(Color.GREEN)
                                .build())
                        .setEphemeral(ephemeral)
                        .queue();
            }

            @Override
            public void noMatches() {
                logger.warn("No match is found.");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                logger.error("Track load failed.", exception);
            }
        });
    }

    @Async
    public void loadMultipleAndPlay(SlashCommandInteractionEvent event, MultipleMusicDto multipleMusicDto, boolean ephemeral) {
        final GuildAudioManager audioManager = this.getAudioManager(event.getGuild());
        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (multipleMusicDto.getFailCount() > 0) {
            event.getHook().sendMessageEmbeds(embedBuilder
                            .setDescription(multipleMusicDto.getCount() + " tracks read and will be queued soon, " +
                                    multipleMusicDto.getFailCount() +
                                    " tracks failed to read because youtube quota exceeded," +
                                    " please use youtube urls to play songs afterwards for today.")
                            .setColor(Color.ORANGE)
                            .build())
                    .setEphemeral(ephemeral)
                    .queue();
        } else {
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                            .setDescription(multipleMusicDto.getCount() + " tracks read and will be queued soon.")
                            .setColor(Color.GREEN)
                            .build())
                    .setEphemeral(ephemeral)
                    .queue();
        }

        for (MusicDto musicDto : multipleMusicDto.getMusicDtoList()) {
            this.audioPlayerManager.loadItemOrdered(audioManager, musicDto.getReference(), new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    audioManager.musicScheduler.queue(track);
                    saveTrack(track, musicDto.getTitle());
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    //
                }

                @Override
                public void noMatches() {
                    logger.warn("No match is found.");
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    logger.error("Track load failed.", exception);
                }
            });
        }
    }

    public void loadAndPlaySfx(Guild guild, String reference) {
        GuildAudioManager guildAudioManager = getAudioManager(guild);
    
        audioPlayerManager.loadItem(reference, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                guildAudioManager.getSfxPlayer().startTrack(track, false);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                logger.warn("Expected a single track but received a playlist. Ignoring playlist.");
            }

            @Override
            public void noMatches() {
                logger.warn("No audio matches found for path: {}", reference);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                logger.error("Failed to load sound from path: {}. Reason: {}", reference, exception.getMessage());
            }
        });
    }

    public void loadAndPlaySfx(@NonNull Member member, @NonNull String reference) {
        Guild guild = member.getGuild();
        GuildVoiceState memberVoiceState = member.getVoiceState();

        if (memberVoiceState == null || !memberVoiceState.inAudioChannel()) {
            return;
        }

        AudioManager audioManager = guild.getAudioManager();
        audioManager.openAudioConnection(memberVoiceState.getChannel());
        audioManager.setSendingHandler(getAudioManager(guild).getSendHandler());

        loadAndPlaySfx(guild, reference);
    }

    private void saveTrack(AudioTrack track, String title) {
        if (title != null) {
            Music music = new Music(0, title, track.getInfo().uri);
            Music dbMusic = musicRepository.findFirstByTitle(music.getTitle());
            if (dbMusic == null) musicRepository.save(music);
        }
    }
}