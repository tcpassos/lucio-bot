package com.discord.bot.service.audioplayer;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.discord.bot.audioplayer.GuildAudioManager;
import com.discord.bot.dto.MultipleMusicDto;
import com.discord.bot.dto.MusicDto;
import com.discord.bot.entity.Music;
import com.discord.bot.repository.MusicRepository;
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
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Service
public class PlayerManagerService {
    private final static Logger logger = LoggerFactory.getLogger(PlayerManagerService.class);
    private final Map<Long, GuildAudioManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;
    final MusicRepository musicRepository;

    public PlayerManagerService(MusicRepository musicRepository) {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();

        // A implementação padrão do YoutubeAudioSourceManager não é mais suportada, estou usando a implementação do LavaLink
        YoutubeAudioSourceManager yt = new YoutubeAudioSourceManager(true);
        this.audioPlayerManager.registerSourceManager(yt);

        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
        this.musicRepository = musicRepository;
    }

    public GuildAudioManager getMusicManager(Guild guild) {
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
        final GuildAudioManager musicManager = this.getMusicManager(event.getGuild());
        EmbedBuilder embedBuilder = new EmbedBuilder();
        this.audioPlayerManager.loadItemOrdered(musicManager, musicDto.getYoutubeUri(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                event.getHook().sendMessageEmbeds(embedBuilder
                                .setDescription("Song added to queue: " + track.getInfo().title
                                        + "\n in queue: " + (musicManager.scheduler.queue.size() + 1))
                                .setColor(Color.GREEN)
                                .build())
                        .setEphemeral(ephemeral)
                        .queue();

                musicManager.scheduler.queue(track);
                saveTrack(track, musicDto.getTitle());
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                List<AudioTrack> tracks = playlist.getTracks();

                for (AudioTrack track : tracks) {
                    musicManager.scheduler.queue(track);
                }
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                                .setDescription(tracks.size() + " song added to queue.")
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
        final GuildAudioManager musicManager = this.getMusicManager(event.getGuild());
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
            this.audioPlayerManager.loadItemOrdered(musicManager, musicDto.getYoutubeUri(), new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    musicManager.scheduler.queue(track);
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

    public void playSound(Guild guild, String path) {
        GuildAudioManager guildMusicManager = getMusicManager(guild);
    
        audioPlayerManager.loadItem(path, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                guildMusicManager.getSfxPlayer().startTrack(track, false);
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

    private void saveTrack(AudioTrack track, String title) {
        if (title != null) {
            Music music = new Music(0, title, track.getInfo().uri);
            Music dbMusic = musicRepository.findFirstByTitle(music.getTitle());
            if (dbMusic == null) musicRepository.save(music);
        }
    }
}