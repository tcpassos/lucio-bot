package com.discord.bot.commands.othercommands;

import java.awt.Color;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.MessageService;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.discord.bot.service.audioplayer.SfxService;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
@AllArgsConstructor
public class BupCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;
    MessageService messageService;
    SfxService sfxService;
    MusicCommandUtils utils;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        var memberToBupOption = event.getOption("user");
        Member memberToBup = memberToBupOption == null ? event.getMember() : memberToBupOption.getAsMember();
        GuildVoiceState memberVoiceState = memberToBup.getVoiceState();

        if (memberVoiceState == null || !memberVoiceState.inAudioChannel()) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setDescription(messageService.getMessage("bot.user.notinvoice", memberToBup.getAsMention())).setColor(Color.RED);
            event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
            return;
        }

        playerManagerService.loadAndPlaySfx(memberVoiceState.getChannel(), sfxService.getSound("bup.ogg"));

        event.reply("Bup!").queue();
    }

}
