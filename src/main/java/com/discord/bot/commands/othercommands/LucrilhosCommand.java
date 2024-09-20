package com.discord.bot.commands.othercommands;

import com.discord.bot.audioplayer.GuildAudioManager;
import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.audioplayer.PlayerManagerService;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;
@AllArgsConstructor
public class LucrilhosCommand implements ISlashCommand {
    PlayerManagerService playerManagerService;

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

        AudioChannel voiceChannel = memberVoiceState.getChannel();
        AudioManager audioManager = guild.getAudioManager();

        audioManager.openAudioConnection(voiceChannel);
        GuildAudioManager musicManager = playerManagerService.getAudioManager(guild);
        audioManager.setSendingHandler(musicManager.getSendHandler());

        String soundFile = this.getClass().getClassLoader().getResource("sounds/lucrilhos.ogg").getFile();
        playerManagerService.loadAndPlaySfx(guild, soundFile);

        event.reply("Isso é falta de lu-cri-lhos!").queue();
    }

}
