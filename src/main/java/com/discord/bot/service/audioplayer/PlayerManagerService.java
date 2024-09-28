package com.discord.bot.service.audioplayer;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.discord.bot.audioplayer.GuildPlaybackManager;
import com.discord.bot.audioplayer.SpotifyToYoutubeSourceManager;
import com.discord.bot.audioplayer.TrackScheduler;
import com.discord.bot.dto.MusicDto;
import com.discord.bot.entity.Music;
import com.discord.bot.repository.MusicRepository;
import com.discord.bot.service.MessageService;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.SpotifyService;
import com.discord.bot.service.YoutubeService;
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
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;

@Service
public class PlayerManagerService {
    private final static Logger logger = LoggerFactory.getLogger(PlayerManagerService.class);
    private final ConcurrentMap<Long, GuildPlaybackManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;
    
    private final MusicRepository musicRepository;
    private final MessageService messageService;
    private final YoutubeService youtubeService;
    private final SpotifyService spotifyService;
    private final SfxService sfxService;
    private final MusicCommandUtils utils;

    @Value("${youtube.api.refresh-token}")
    private String refreshToken;

    public PlayerManagerService(MusicRepository musicRepository, MessageService messageService, YoutubeService youtubeService,
                                SpotifyService spotifyService, SfxService sfxService, MusicCommandUtils utils) {
        this.musicManagers = new ConcurrentHashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        this.musicRepository = musicRepository;
        this.messageService = messageService;
        this.youtubeService = youtubeService;
        this.spotifyService = spotifyService;
        this.sfxService = sfxService;
        this.utils = utils;


        // The default implementation of YoutubeAudioSourceManager is no longer supported, I'm using the LavaLink implementation
        YoutubeAudioSourceManager youtubeSourceManager = new YoutubeAudioSourceManager(true);
        // youtubeSourceManager.useOauth2(refreshToken, true);
        youtubeSourceManager.useOauth2(null, false);
        SpotifyToYoutubeSourceManager spotifyToYoutubeSourceManager = new SpotifyToYoutubeSourceManager(youtubeSourceManager, this.spotifyService, this.youtubeService);
        
        this.audioPlayerManager.registerSourceManager(youtubeSourceManager);
        this.audioPlayerManager.registerSourceManager(spotifyToYoutubeSourceManager);
        this.audioPlayerManager.getConfiguration().setOutputFormat(StandardAudioDataFormats.DISCORD_PCM_S16_BE);

        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
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

    public void loadAndPlayMusic(SlashCommandInteractionEvent event, MusicDto musicDto) {
        final GuildPlaybackManager musicManager = this.getPlaybackManager(event.getGuild());
        this.audioPlayerManager.loadItemOrdered(musicManager, musicDto.getReference(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                String trackUrl = track.getInfo().uri;
                event.getHook().sendMessage(messageService.getMessage("bot.queue.added.song", track.getInfo().title, trackUrl))
                     .setEphemeral(false)
                     .queue();

                if ((musicManager.musicScheduler.queue(track)).equals(TrackScheduler.QueueEvent.TRACK_STARTED)) {
                    var channel = event.getMember().getVoiceState().getChannel();
                    loadAndPlaySfx(channel, sfxService.getSound(SfxType.MUSIC_START));
                }

                saveTrack(track, musicDto.getTitle());
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                List<AudioTrack> tracks = playlist.getTracks();

                if (musicManager.musicScheduler.queueAll(tracks).equals(TrackScheduler.QueueEvent.TRACK_STARTED)) {
                    var channel = event.getMember().getVoiceState().getChannel();
                    loadAndPlaySfx(channel, sfxService.getSound(SfxType.MUSIC_START));
                }

                event.getHook().sendMessage(messageService.getMessage("bot.queue.added.playlist", playlist.getName()))
                               .setEphemeral(false)
                               .queue();
            }

            @Override
            public void noMatches() {
                logger.warn("No match is found for: {}", musicDto.getReference());
                event.getHook().sendMessageEmbeds(messageService.getEmbedError("bot.song.nomatches").build())
                     .setEphemeral(true)
                     .queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                logger.error("Track load failed.", exception);
                event.getHook().sendMessageEmbeds(messageService.getEmbedError("bot.song.loadfailed", exception.getMessage()).build())
                     .setEphemeral(true)
                     .queue();
            }
        });
    }

    public void loadAndPlaySfx(@NonNull AudioChannel channel, @NonNull String reference) {
        joinAudioChannel(channel);
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

    public boolean joinAudioChannel(SlashCommandInteractionEvent event) {
        try {
            tryJoinAudioChannel(event);
            return true;
        } catch (Exception e) {
            event.getHook().sendMessageEmbeds(new EmbedBuilder().setDescription(e.getMessage()).build())
                 .setEphemeral(true)
                 .queue();
        }
        return false;
    }

    public boolean joinAudioChannel(AudioChannel audioChannel) {
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

    public void tryJoinAudioChannel(SlashCommandInteractionEvent event) throws Exception {
        AudioChannel userChannel = getAudioChannel(event, false);
        AudioChannel botChannel = getAudioChannel(event, true);
    
        // User is not in a voice channel
        if (userChannel == null) {
            throw new Exception(messageService.getMessage("bot.user.notinvoice"));
        }
        // If bot is not in a voice channel, join the user's voice channel
        if (botChannel == null) {
            utils.playerCleaner(getPlaybackManager(event.getGuild()));
            if (!joinAudioChannel(userChannel)) {
                throw new Exception(messageService.getMessage("bot.voice.notallowed"));
            }
            botChannel = userChannel;
        }
        // Bot is in a different voice channel
        if (!botChannel.equals(userChannel)) {
            throw new Exception(messageService.getMessage("bot.user.notinsamevoice"));
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