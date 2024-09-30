package com.discord.bot.commands.musiccommands;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.audioplayer.PlayerManagerService;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@AllArgsConstructor
public class PlayCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
    
        if (playerManagerService.joinAudioChannel(event)) {
            playerManagerService.loadAndPlayMusic(event, event.getOption("query").getAsString());
        }
    }
}