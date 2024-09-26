package com.discord.bot.commands.musiccommands;

import com.discord.bot.audioplayer.GuildPlaybackManager;
import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.MessageService;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;

@AllArgsConstructor
public class RemoveCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;
    MessageService messageService;
    MusicCommandUtils utils;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (utils.channelControl(event)) {
            GuildPlaybackManager musicManager = playerManagerService.getPlaybackManager(event.getGuild());
            var queue = musicManager.musicScheduler.queue;

            if (!queue.isEmpty()) {
                var command = event.getSubcommandName();

                if (command != null) {
                    switch (command) {
                        case "single" -> handleSingleCommand(event, queue, embedBuilder);
                        case "between" -> handleBetweenCommand(event, queue, embedBuilder);
                        case "all" -> handleAllCommand(queue, embedBuilder);
                    }
                } else embedBuilder.setDescription(messageService.getMessage("command.empty.subcommand")).setColor(Color.RED);
            } else embedBuilder.setDescription(messageService.getMessage("bot.queue.empty")).setColor(Color.RED);
        } else embedBuilder.setDescription(messageService.getMessage("bot.user.notinsamevoice")).setColor(Color.RED);

        event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
    }

    private void handleSingleCommand(SlashCommandInteractionEvent event, BlockingQueue<AudioTrack> queue, EmbedBuilder embedBuilder) {
        int index = Objects.requireNonNull(event.getOption("songnum")).getAsInt() - 1;

        if (index >= 0 && index < queue.size()) {
            var iterator = queue.iterator();

            for (int i = 0; i < index; i++) {
                iterator.next();
            }

            var removedSong = iterator.next();
            //noinspection ResultOfMethodCallIgnored
            queue.remove(removedSong);

            embedBuilder.setDescription(messageService.getMessage("bot.queue.removedsong")).setColor(Color.GREEN);
        } else embedBuilder.setDescription(messageService.getMessage("bot.queue.invalidsongindex")).setColor(Color.RED);
    }

    private void handleBetweenCommand(SlashCommandInteractionEvent event, BlockingQueue<AudioTrack> queue, EmbedBuilder embedBuilder) {
        var firstIndex = Objects.requireNonNull(event.getOption("songnum1")).getAsInt() - 1;
        var lastIndex = Objects.requireNonNull(event.getOption("songnum2")).getAsInt() - 1;

        if (firstIndex >= 0 && lastIndex >= 0 && firstIndex <= lastIndex && lastIndex < queue.size()) {
            var iterator = queue.iterator();
            var songsToRemove = new ArrayList<>();

            for (int i = 0; i <= lastIndex; i++) {
                var song = iterator.next();
                if (i >= firstIndex) {
                    songsToRemove.add(song);
                }
            }
            //noinspection SuspiciousMethodCalls
            queue.removeAll(songsToRemove);

            embedBuilder.setDescription(messageService.getMessage("bot.queue.removedsongs")).setColor(Color.GREEN);
        } else embedBuilder
                .setDescription(messageService.getMessage("bot.queue.invalidsongindex"))
                .setColor(Color.RED);
    }

    private void handleAllCommand(BlockingQueue<AudioTrack> queue, EmbedBuilder embedBuilder) {
        //noinspection SuspiciousMethodCalls
        queue.removeAll(Arrays.asList(queue.toArray()));
        embedBuilder.setDescription(messageService.getMessage("bot.queue.removedsongs")).setColor(Color.GREEN);
    }
}