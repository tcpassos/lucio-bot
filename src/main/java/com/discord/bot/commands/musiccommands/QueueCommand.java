package com.discord.bot.commands.musiccommands;

import com.discord.bot.service.MessageService;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.discord.bot.commands.ISlashCommand;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.concurrent.BlockingQueue;

@AllArgsConstructor
public class QueueCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;
    MessageService messageService;
    MusicCommandUtils utils;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        BlockingQueue<AudioTrack> queue = playerManagerService.getPlaybackManager(event.getGuild()).musicScheduler.queue;
        var trackList = queue.stream().toList();

        if (queue.isEmpty()) {
            sendEmptyQueueResponse(event, embedBuilder);
            return;
        }

        int totalPages = (int) Math.ceil((double) queue.size() / 20);
        var pageOption = event.getOption("page");
        int page = pageOption == null ? 1 : Math.min(Math.max(pageOption.getAsInt(), 1), totalPages);

        if (page < 1 || page > totalPages) {
            sendInvalidPageResponse(event, embedBuilder, totalPages);
            return;
        }

        embedBuilder = utils.queueBuilder(embedBuilder, page, queue, trackList);

        event.replyEmbeds(embedBuilder.build()).addActionRow(
                        Button.secondary("prev", messageService.getMessage("bot.queue.page.previous"))
                                .withDisabled(page == 1),
                        Button.secondary("next", messageService.getMessage("bot.queue.page.next"))
                                .withDisabled(page == totalPages))
                .setEphemeral(true)
                .queue();
    }

    private void sendEmptyQueueResponse(SlashCommandInteractionEvent event, EmbedBuilder embedBuilder) {
        embedBuilder.setDescription(messageService.getMessage("bot.queue.empty")).setColor(Color.RED);
        event.replyEmbeds(embedBuilder.build())
                .setEphemeral(true)
                .queue();
    }

    private void sendInvalidPageResponse(SlashCommandInteractionEvent event, EmbedBuilder embedBuilder, int totalPages) {
        embedBuilder.setDescription(messageService.getMessage("bot.queue.page.invalid", totalPages)).setColor(Color.RED);
        event.replyEmbeds(embedBuilder.build())
                .setEphemeral(true)
                .queue();
    }
}