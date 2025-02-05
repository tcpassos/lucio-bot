package com.discord.bot.commands.othercommands;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import com.discord.bot.service.audioplayer.SfxService;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
@AllArgsConstructor
public class LucrilhosCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;
    SfxService sfxService;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        Guild guild = event.getGuild();

        if (member == null || guild == null) {
            event.reply("Este comando só pode ser usado em um servidor.").setEphemeral(true).queue();
            return;
        }

        GuildVoiceState memberVoiceState = member.getVoiceState();

        if (memberVoiceState == null || !memberVoiceState.inAudioChannel()) {
            event.reply("Você precisa estar em um canal de voz para usar este comando.").setEphemeral(true).queue();
            return;
        }

        playerManagerService.loadAndPlaySfx(memberVoiceState.getChannel(), sfxService.getSound("lucrilhos.ogg"));

        event.reply("Isso é falta de lu-cri-lhos!").queue();
    }

}
