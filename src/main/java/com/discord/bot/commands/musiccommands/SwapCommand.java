package com.discord.bot.commands.musiccommands;

import com.discord.bot.audioplayer.GuildPlaybackManager;
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
import java.util.List;

@AllArgsConstructor
public class SwapCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;
    MessageService messageService;
    MusicCommandUtils utils;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (utils.isSameAudioChannel(event)) {
            GuildPlaybackManager musicManager = playerManagerService.getPlaybackManager(event.getGuild());
            List<AudioTrack> trackList = new ArrayList<>(musicManager.musicScheduler.queue);
            var firstOption = event.getOption("songnum1");
            var secondOption = event.getOption("songnum2");
            assert firstOption != null;
            int first = firstOption.getAsInt() - 1;
            assert secondOption != null;
            int second = secondOption.getAsInt() - 1;
            int size = musicManager.musicScheduler.queue.size();

            if (first >= size || second >= size) {
                embedBuilder.setDescription(messageService.getMessage("bot.song.swap.invalid")).setColor(Color.RED);
            } else {
                if (trackList.size() > 1) {
                    AudioTrack temp = trackList.get(first);
                    trackList.set(first, trackList.get(second));
                    trackList.set(second, temp);

                    musicManager.musicScheduler.queue.clear();
                    musicManager.musicScheduler.queueAll(trackList);

                    embedBuilder.setDescription(messageService.getMessage("bot.song.swap.success")).setColor(Color.GREEN);
                } else if (trackList.size() == 1) {
                    embedBuilder.setDescription(messageService.getMessage("bot.queue.onesong")).setColor(Color.RED);
                } else embedBuilder.setDescription(messageService.getMessage("bot.queue.empty")).setColor(Color.RED);
            }
        } else embedBuilder.setDescription(messageService.getMessage("bot.user.notinsamevoice")).setColor(Color.RED);

        event.replyEmbeds(embedBuilder.build()).setEphemeral(false).queue();
    }
}