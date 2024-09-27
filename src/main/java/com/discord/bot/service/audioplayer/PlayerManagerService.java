package com.discord.bot.service.audioplayer;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.discord.bot.audioplayer.GuildPlaybackManager;
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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;

@Service
public class PlayerManagerService {
    private final static Logger logger = LoggerFactory.getLogger(PlayerManagerService.class);
    private final ConcurrentMap<Long, GuildPlaybackManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;
    private final MusicRepository musicRepository;
    private final MessageService messageService;

    @Value("${youtube.api.refresh-token}")
    private String refreshToken;

    public PlayerManagerService(MusicRepository musicRepository, MessageService messageService) {
        this.musicManagers = new ConcurrentHashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();

        // The default implementation of YoutubeAudioSourceManager is no longer supported, I'm using the LavaLink implementation
        YoutubeAudioSourceManager yt = new YoutubeAudioSourceManager(true);
        // yt.useOauth2(refreshToken, true);
        // yt.useOauth2(null, false);
        this.audioPlayerManager.registerSourceManager(yt);

        this.audioPlayerManager.getConfiguration().setOutputFormat(StandardAudioDataFormats.DISCORD_PCM_S16_BE);

        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
        this.musicRepository = musicRepository;
        this.messageService = messageService;
    }

    public GuildPlaybackManager getPlaybackManager(Guild guild) {
        if (guild == null) {
            return null;
        }

        return this.musicManagers.computeIfAbsent(guild.getIdLong(), guildId -> {
            final GuildPlaybackManager guildMusicManager = new GuildPlaybackManager(this.audioPlayerManager, guild);
            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
            return guildMusicManager;
        });
    }

    public void loadAndPlay(SlashCommandInteractionEvent event, MusicDto musicDto) {
        final GuildPlaybackManager musicManager = this.getPlaybackManager(event.getGuild());
        EmbedBuilder embedBuilder = new EmbedBuilder();
        this.audioPlayerManager.loadItemOrdered(musicManager, musicDto.getReference(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                String trackUrl = track.getInfo().uri;
                embedBuilder.setDescription(messageService.getMessage("bot.queue.added.song", "[" + track.getInfo().title + "](" + trackUrl + ")", musicManager.musicScheduler.queue.size() + 1))
                            .setColor(Color.GREEN);
                event.getHook().sendMessageEmbeds(embedBuilder.build())
                        .setEphemeral(true)
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
                        .setEphemeral(true)
                        .queue();
            }

            @Override
            public void noMatches() {
                logger.warn("No match is found for: {}", musicDto.getReference());
                embedBuilder.setDescription(messageService.getMessage("bot.song.nomatches"))
                            .setColor(Color.RED);
                event.getHook().sendMessageEmbeds(embedBuilder.build())
                        .setEphemeral(true)
                        .queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                logger.error("Track load failed.", exception);
                embedBuilder.setDescription(messageService.getMessage("bot.song.loadfailed", exception.getMessage()))
                            .setColor(Color.RED);
                event.getHook().sendMessageEmbeds(embedBuilder.build())
                        .setEphemeral(true)
                        .queue();
            }
        });
    }

    @Async
    public void loadMultipleAndPlay(SlashCommandInteractionEvent event, MultipleMusicDto multipleMusicDto) {
        final GuildPlaybackManager audioManager = this.getPlaybackManager(event.getGuild());

        if (multipleMusicDto.getFailCount() > 0) {
            event.getHook().sendMessageEmbeds(messageService.getEmbed("bot.queue.added.songs.partial", multipleMusicDto.getCount(), multipleMusicDto.getFailCount())
                                                            .setColor(Color.ORANGE)
                                                            .build())
                    .setEphemeral(true)
                    .queue();
        } else {
            event.getHook().sendMessageEmbeds(messageService.getEmbed("bot.queue.added.songs", multipleMusicDto.getCount())
                                                            .setColor(Color.GREEN)
                                                            .build())
                           .setEphemeral(true)
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

    public void loadAndPlaySfx(@NonNull AudioChannel channel, @NonNull String reference) {
        joinVoiceChannel(channel);
        GuildPlaybackManager playbackManager = getPlaybackManager(channel.getGuild());
        audioPlayerManager.loadItem(reference, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                playbackManager.getSfxPlayer().startTrack(track, false);
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

    public boolean joinVoiceChannel(AudioChannel audioChannel) {
        Guild guild = audioChannel.getGuild();
        AudioManager audioManager = guild.getAudioManager();
        var playerbackManager = getPlaybackManager(guild);

        if (!guild.getSelfMember().hasPermission(audioChannel, Permission.VOICE_CONNECT)) {
            logger.warn("The bot does not have permission to connect to the voice channel.");
            return false;
        }

        audioManager.openAudioConnection(audioChannel);
        audioManager.setSendingHandler(playerbackManager.getSendHandler());
        return true;
    }

    private void saveTrack(AudioTrack track, String title) {
        if (title != null) {
            Music music = new Music(0, title, track.getInfo().uri);
            Music dbMusic = musicRepository.findFirstByTitle(music.getTitle());
            if (dbMusic == null) musicRepository.save(music);
        }
    }

    @Scheduled(fixedRate = 10000)
    protected void checkToDisconnectAll() {
        musicManagers.values().forEach(GuildPlaybackManager::checkToDisconnect);
    }

    @Scheduled(fixedRate = 5000)
    protected void updateActivityAll() {
        musicManagers.values().forEach(GuildPlaybackManager::updateActivity);
    }
}