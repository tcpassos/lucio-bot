package com.discord.bot.commands;

import com.discord.bot.commands.admincommands.GuildsCommand;
import com.discord.bot.commands.admincommands.LogsCommand;
import com.discord.bot.commands.musiccommands.*;
import com.discord.bot.commands.othercommands.BupCommand;
import com.discord.bot.commands.othercommands.LucrilhosCommand;
import com.discord.bot.service.MessageService;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.RestService;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.discord.bot.service.audioplayer.SfxService;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandManager extends ListenerAdapter {
    final RestService restService;
    final PlayerManagerService playerManagerService;
    final MessageService messageService;
    final SfxService sfxService;
    final MusicCommandUtils musicCommandUtils;
    private final String adminUserId;
    private Map<String, ISlashCommand> commandsMap;

    public CommandManager(RestService restService, PlayerManagerService playerManagerService,
                          MessageService messageService, SfxService sfxService,
                          MusicCommandUtils musicCommandUtils, String adminUserId) {
        this.restService = restService;
        this.playerManagerService = playerManagerService;
        this.messageService = messageService;
        this.sfxService = sfxService;
        this.musicCommandUtils = musicCommandUtils;
        this.adminUserId = adminUserId;
        commandMapper();
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

    private void commandMapper() {
        commandsMap = new ConcurrentHashMap<>();
        // Admin commands
        commandsMap.put("guilds", new GuildsCommand(adminUserId));
        commandsMap.put("logs", new LogsCommand(adminUserId));
        // Music commands
        commandsMap.put("play", new PlayCommand(restService, playerManagerService, messageService, sfxService, musicCommandUtils));
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
        commandsMap.put("nowplaying", new NowPlayingCommand(playerManagerService, messageService, musicCommandUtils));
        commandsMap.put("volume", new VolumeCommand(playerManagerService, messageService, sfxService, musicCommandUtils));
        commandsMap.put("mhelp", new MusicHelpCommand());
        // Other commands
        commandsMap.put("lucrilhos", new LucrilhosCommand(playerManagerService));
        commandsMap.put("bup", new BupCommand(playerManagerService, messageService, musicCommandUtils));
    }
}