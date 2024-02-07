package com.discord.bot.commands.musiccommands;

import com.discord.bot.audioplayer.GuildMusicManager;
import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;

@AllArgsConstructor
public class RemoveCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;
    MusicCommandUtils utils;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        var ephemeralOption = event.getOption("ephemeral");
        boolean ephemeral = ephemeralOption == null || ephemeralOption.getAsBoolean();

        if (utils.channelControl(event)) {
            GuildMusicManager musicManager = playerManagerService.getMusicManager(event);
            var queue = musicManager.scheduler.queue;

            if (queue.isEmpty()) {
                event.replyEmbeds(new EmbedBuilder()
                                .setDescription("Song queue is empty.")
                                .setColor(Color.RED)
                                .build())
                        .setEphemeral(ephemeral)
                        .queue();
            }

            var command = event.getFullCommandName();

            if (command.contains("single")) {
                int index = Objects.requireNonNull(event.getOption("songnum")).getAsInt() - 1;

                if (index >= 0 && index < queue.size()) {
                    var iterator = queue.iterator();

                    for (int i = 0; i < index; i++) {
                        iterator.next();
                    }

                    var removedSong = iterator.next();
                    //noinspection ResultOfMethodCallIgnored
                    queue.remove(removedSong);

                    event.replyEmbeds(new EmbedBuilder()
                                    .setDescription("Song removed from the queue.")
                                    .setColor(Color.GREEN)
                                    .build())
                            .setEphemeral(ephemeral)
                            .queue();
                } else {
                    event.replyEmbeds(new EmbedBuilder()
                                    .setDescription("Invalid song index. Please provide a valid index.")
                                    .setColor(Color.RED)
                                    .build())
                            .setEphemeral(ephemeral)
                            .queue();
                }
            } else if (command.contains("between")) {
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

                    event.replyEmbeds(new EmbedBuilder()
                                    .setDescription("Removed songs from the queue.")
                                    .setColor(Color.GREEN)
                                    .build())
                            .setEphemeral(ephemeral)
                            .queue();
                } else {
                    event.replyEmbeds(new EmbedBuilder()
                                    .setDescription("Indexes are not valid for the current queue. Please check song numbers again.")
                                    .setColor(Color.RED)
                                    .build())
                            .setEphemeral(ephemeral)
                            .queue();
                }
            }
        } else {
            event.replyEmbeds(new EmbedBuilder()
                            .setDescription("Please be in a same voice channel as bot.")
                            .setColor(Color.RED)
                            .build())
                    .setEphemeral(ephemeral)
                    .queue();
        }
    }
}

