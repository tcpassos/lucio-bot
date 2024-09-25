package com.discord.bot;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;

import com.discord.bot.commands.AdminCommands;
import com.discord.bot.commands.CommandManager;
import com.discord.bot.commands.JdaCommands;
import com.discord.bot.listeners.ActivityListener;
import com.discord.bot.listeners.EntitySelectInteractionListener;
import com.discord.bot.listeners.ModalInteractionListener;
import com.discord.bot.repository.GuildConfigRepository;
import com.discord.bot.service.MessageService;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.MusicService;
import com.discord.bot.service.RestService;
import com.discord.bot.service.SpotifyService;
import com.discord.bot.service.SpotifyTokenService;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.discord.bot.service.audioplayer.SfxService;

import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

@Configuration
@EnableScheduling
public class LucioBot {
    private final static Logger logger = LoggerFactory.getLogger(LucioBot.class);
    final RestService restService;
    final SpotifyService spotifyService;
    final PlayerManagerService playerManagerService;
    final MusicService musicService;
    final MusicCommandUtils musicCommandUtils;
    final SpotifyTokenService spotifyTokenService;
    final MessageService messageService;
    final SfxService sfxService;
    final GuildConfigRepository guildConfigRepository;

    @Value("${discord.bot.token}")
    private String discordToken;

    @Value("${discord.admin.server.id}")
    private String adminServerId;

    @Value("${discord.admin.user.id}")
    private String adminUserId;

    @Value("${discord.bot.language}")
    private String botLanguage;

    public LucioBot(RestService restService, SpotifyService spotifyService, PlayerManagerService playerManagerService,
                    MusicService musicService, MusicCommandUtils musicCommandUtils, SpotifyTokenService spotifyTokenService,
                    MessageService messageService, SfxService sfxService,
                    GuildConfigRepository guildConfigRepository) {
        this.restService = restService;
        this.spotifyService = spotifyService;
        this.playerManagerService = playerManagerService;
        this.musicService = musicService;
        this.musicCommandUtils = musicCommandUtils;
        this.spotifyTokenService = spotifyTokenService;
        this.messageService = messageService;
        this.sfxService = sfxService;
        this.guildConfigRepository = guildConfigRepository;
    }

    @PostConstruct
    public void startDiscordBot() throws InterruptedException {
        // If specified, change the bot language
        if (StringUtils.hasText(botLanguage)) {
            Locale locale = Locale.forLanguageTag(botLanguage);
            messageService.changeLanguage(locale);
            sfxService.setLocale(locale);
        }

        JDA jda = JDABuilder.createDefault(discordToken)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGES)
                .enableCache(CacheFlag.ACTIVITY)
                .setChunkingFilter(ChunkingFilter.ALL)
                .addEventListeners(
                    new CommandManager(restService, spotifyService, playerManagerService, musicService,
                                       messageService, sfxService, musicCommandUtils, guildConfigRepository, adminUserId),
                    new ActivityListener(messageService, guildConfigRepository),
                    new ModalInteractionListener(messageService, guildConfigRepository),
                    new EntitySelectInteractionListener(messageService, guildConfigRepository)
                )
                .setActivity(Activity.listening("Não para, não para, não para não!"))
                .build();
        jda.awaitReady();
        new JdaCommands(messageService).addJdaCommands(jda);
        new AdminCommands().addAdminCommands(jda, adminServerId);
        logger.info("Starting bot is done!");
    }

    @Scheduled(fixedDelay = 3500000)
    private void refreshSpotifyToken() {
        spotifyTokenService.getAccessToken();
    }
}