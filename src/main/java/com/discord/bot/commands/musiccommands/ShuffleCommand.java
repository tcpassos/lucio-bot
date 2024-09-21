package com.discord.bot.commands.musiccommands;

import com.discord.bot.audioplayer.GuildAudioManager;
import com.discord.bot.service.MessageService;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.discord.bot.commands.ISlashCommand;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class ShuffleCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;
    MessageService messageService;
    MusicCommandUtils utils;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (utils.channelControl(event)) {
            GuildAudioManager musicManager = playerManagerService.getAudioManager(event.getGuild());
            List<AudioTrack> trackList = new ArrayList<>(musicManager.musicScheduler.queue);

            if (trackList.size() > 1) {
                Collections.shuffle(trackList);
                musicManager.musicScheduler.queue.clear();
                musicManager.musicScheduler.queueAll(trackList);

                embedBuilder.setDescription(messageService.getMessage("bot.queue.shuffled")).setColor(Color.GREEN);
            } else embedBuilder.setDescription(messageService.getMessage("bot.queue.onesong")).setColor(Color.RED);
        } else embedBuilder.setDescription(messageService.getMessage("bot.user.notinsamevoice")).setColor(Color.RED);

        event.replyEmbeds(embedBuilder.build()).setEphemeral(false).queue();
    }
}