package com.discord.bot.commands;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.lang.NonNull;

import com.discord.bot.commands.admincommands.GuildsCommand;
import com.discord.bot.commands.admincommands.LogsCommand;
import com.discord.bot.commands.musiccommands.FillCommand;
import com.discord.bot.commands.musiccommands.ForwardCommand;
import com.discord.bot.commands.musiccommands.LeaveCommand;
import com.discord.bot.commands.musiccommands.LoopCommand;
import com.discord.bot.commands.musiccommands.MixCommand;
import com.discord.bot.commands.musiccommands.MusicHelpCommand;
import com.discord.bot.commands.musiccommands.NowPlayingCommand;
import com.discord.bot.commands.musiccommands.PauseCommand;
import com.discord.bot.commands.musiccommands.PlayCommand;
import com.discord.bot.commands.musiccommands.QueueButton;
import com.discord.bot.commands.musiccommands.QueueCommand;
import com.discord.bot.commands.musiccommands.RemoveCommand;
import com.discord.bot.commands.musiccommands.ResumeCommand;
import com.discord.bot.commands.musiccommands.RewindCommand;
import com.discord.bot.commands.musiccommands.ShuffleCommand;
import com.discord.bot.commands.musiccommands.SkipCommand;
import com.discord.bot.commands.musiccommands.SwapCommand;
import com.discord.bot.commands.musiccommands.TopCommand;
import com.discord.bot.commands.musiccommands.VolumeCommand;
import com.discord.bot.commands.othercommands.BupCommand;
import com.discord.bot.commands.othercommands.ConfigureCommand;
import com.discord.bot.commands.othercommands.LucrilhosCommand;
import com.discord.bot.context.GuildContext;
import com.discord.bot.context.GuildContextHolder;
import com.discord.bot.entity.GuildConfig;
import com.discord.bot.listeners.BaseGuildListener;
import com.discord.bot.repository.GuildConfigRepository;
import com.discord.bot.service.MessageService;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.MusicService;
import com.discord.bot.service.SpotifyService;
import com.discord.bot.service.YoutubeService;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.discord.bot.service.audioplayer.SfxService;

import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

public class CommandManager extends BaseGuildListener {
    final YoutubeService restService;
    final SpotifyService spotifyService;
    final PlayerManagerService playerManagerService;
    final MusicService musicService;
    final MessageService messageService;
    final SfxService sfxService;
    final MusicCommandUtils musicCommandUtils;
    final GuildConfigRepository guildConfigRepository;
    private final String adminUserId;
    private Map<String, ISlashCommand> commandsMap;

    public CommandManager(YoutubeService restService, SpotifyService spotifyService, PlayerManagerService playerManagerService,
                          MusicService musicService, MessageService messageService, SfxService sfxService,
                          MusicCommandUtils musicCommandUtils, GuildConfigRepository guildConfigRepository, String adminUserId) {
        super(guildConfigRepository);
        this.restService = restService;
        this.spotifyService = spotifyService;
        this.playerManagerService = playerManagerService;
        this.musicService = musicService;
        this.messageService = messageService;
        this.sfxService = sfxService;
        this.musicCommandUtils = musicCommandUtils;
        this.guildConfigRepository = guildConfigRepository;
        this.adminUserId = adminUserId;
        commandMapper();
    }

    @Override
    public void onGenericGuild(GenericGuildEvent event) {
        Long guildId = event.getGuild().getIdLong();
        GuildConfig guildConfig = guildRepository.findById(guildId)
            .orElseGet(() -> {
                GuildConfig newConfig = new GuildConfig(guildId);
                guildRepository.save(newConfig);
                return newConfig;
            });
        GuildContextHolder.setGuildContext(new GuildContext(guildId, guildConfig));
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();

        ISlashCommand command;
        if ((command = commandsMap.get(commandName)) != null) {
            command.execute(event);
        }
    }

    @Override
    public void onButtonInteraction(@NonNull ButtonInteractionEvent event) {
        IButtonInteraction interaction = new QueueButton(playerManagerService, musicCommandUtils);
        interaction.click(event);
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals("mix") && event.getFocusedOption().getName().startsWith("genre")) {
            replyAutoCompleteChoices(event, MixCommand.genres);
        }
    }

    private void commandMapper() {
        commandsMap = new ConcurrentHashMap<>();
        // Admin commands
        commandsMap.put("guilds", new GuildsCommand(adminUserId));
        commandsMap.put("logs", new LogsCommand(adminUserId));
        // Music commands
        commandsMap.put("play", new PlayCommand(messageService, restService, spotifyService, musicService));
        commandsMap.put("skip", new SkipCommand(playerManagerService, messageService, musicCommandUtils));
        commandsMap.put("forward", new ForwardCommand(playerManagerService, messageService, musicCommandUtils));
        commandsMap.put("rewind", new RewindCommand(playerManagerService, messageService, musicCommandUtils));
        commandsMap.put("pause", new PauseCommand(playerManagerService, messageService, musicCommandUtils));
        commandsMap.put("resume", new ResumeCommand(playerManagerService, messageService, musicCommandUtils));
        commandsMap.put("leave", new LeaveCommand(playerManagerService, messageService, musicCommandUtils));
        commandsMap.put("queue", new QueueCommand(playerManagerService, messageService, musicCommandUtils));
        commandsMap.put("swap", new SwapCommand(playerManagerService, messageService, musicCommandUtils));
        commandsMap.put("shuffle", new ShuffleCommand(playerManagerService, messageService, musicCommandUtils));
        commandsMap.put("loop", new LoopCommand(playerManagerService, messageService, musicCommandUtils));
        commandsMap.put("remove", new RemoveCommand(playerManagerService, messageService, musicCommandUtils));
        commandsMap.put("top", new TopCommand(messageService, restService, spotifyService, musicService));
        commandsMap.put("fill", new FillCommand(messageService, restService, spotifyService, musicService, playerManagerService));
        commandsMap.put("mix", new MixCommand(messageService, restService, spotifyService, musicService));
        commandsMap.put("nowplaying", new NowPlayingCommand(playerManagerService, messageService, musicCommandUtils));
        commandsMap.put("volume", new VolumeCommand(playerManagerService, messageService, sfxService, musicCommandUtils));
        commandsMap.put("mhelp", new MusicHelpCommand());
        // Other commands
        commandsMap.put("configure", new ConfigureCommand(messageService, guildConfigRepository));
        commandsMap.put("lucrilhos", new LucrilhosCommand(playerManagerService, sfxService));
        commandsMap.put("bup", new BupCommand(playerManagerService, messageService, sfxService, musicCommandUtils));
    }

    private void replyAutoCompleteChoices(CommandAutoCompleteInteractionEvent event, String[] choices) {
        List<Command.Choice> options = Stream.of(choices)
                .filter(word -> word.startsWith(event.getFocusedOption().getValue()) || event.getFocusedOption().getValue().isEmpty())
                .limit(25)
                .map(word -> new Command.Choice(word, word))
                .collect(Collectors.toList());
        event.replyChoices(options).queue();
    }
}