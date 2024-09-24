package com.discord.bot.commands;

import com.discord.bot.service.MessageService;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class JdaCommands {

    private final MessageService messageService;

    public JdaCommands(MessageService messageService) {
        this.messageService = messageService;
    }

    public void addJdaCommands(JDA jda) {
        CommandListUpdateAction globalCommands = jda.updateCommands();
        String ephemeralString = "Bot reply will only visible to you if set as TRUE, default value is TRUE.";

        globalCommands.addCommands(
                Commands.slash("play", messageService.getMessage("command.play.description"))
                        .addOption(OptionType.STRING, "query", messageService.getMessage("command.play.param.query"), true)
                        .setGuildOnly(true),
                Commands.slash("skip", messageService.getMessage("command.skip.description"))
                        .setGuildOnly(true),
                Commands.slash("forward", messageService.getMessage("command.forward.description"))
                        .addOptions(new OptionData(OptionType.INTEGER, "sec", messageService.getMessage("unit.seconds"))
                                        .setMinValue(1)
                                        .setRequired(true))
                        .setGuildOnly(true),
                Commands.slash("rewind", messageService.getMessage("command.rewind.description"))
                        .addOptions(new OptionData(OptionType.INTEGER, "sec", messageService.getMessage("unit.seconds"))
                                        .setMinValue(1)
                                        .setRequired(true))
                        .setGuildOnly(true),
                Commands.slash("pause", messageService.getMessage("command.pause.description"))
                        .setGuildOnly(true),
                Commands.slash("resume", messageService.getMessage("command.resume.description"))
                        .setGuildOnly(true),
                Commands.slash("leave", messageService.getMessage("command.leave.description"))
                        .setGuildOnly(true),
                Commands.slash("queue", messageService.getMessage("command.queue.description"))
                        .addOptions(new OptionData(OptionType.INTEGER, "page", messageService.getMessage("command.queue.param.page"))
                                        .setMinValue(1)
                                        .setRequired(false))
                        .setGuildOnly(true),
                Commands.slash("swap", messageService.getMessage("command.swap.description"))
                        .addOptions(new OptionData(OptionType.INTEGER, "songnum1",
                                        messageService.getMessage("command.swap.param.song"))
                                        .setMinValue(1)
                                        .setRequired(true),
                                new OptionData(OptionType.INTEGER, "songnum2",
                                        messageService.getMessage("command.swap.param.song"))
                                        .setMinValue(1)
                                        .setRequired(true))
                        .setGuildOnly(true),
                Commands.slash("shuffle", messageService.getMessage("command.shuffle.description"))
                        .setGuildOnly(true),
                Commands.slash("loop", messageService.getMessage("command.loop.description"))
                        .setGuildOnly(true),
                Commands.slash("remove", messageService.getMessage("command.remove.description"))
                        .addSubcommands(new SubcommandData("single", messageService.getMessage("command.remove.param.single"))
                                        .addOptions(new OptionData(OptionType.INTEGER, "songnum", messageService.getMessage("command.remove.param.songnum"))
                                                        .setMinValue(1)
                                                        .setRequired(false)),
                                new SubcommandData("between", messageService.getMessage("command.remove.param.between"))
                                        .addOptions(new OptionData(OptionType.INTEGER, "songnum1", messageService.getMessage("command.remove.param.songnum1"))
                                                        .setMinValue(1)
                                                        .setRequired(true),
                                                new OptionData(OptionType.INTEGER, "songnum2", messageService.getMessage("command.remove.param.songnum2"))
                                                        .setMinValue(1)
                                                        .setRequired(true)),
                                new SubcommandData("all", messageService.getMessage("command.remove.param.all")))
                        .setGuildOnly(true),
                Commands.slash("nowplaying", messageService.getMessage("command.nowplaying.description"))
                        .setGuildOnly(true),
                Commands.slash("volume", messageService.getMessage("command.volume.description"))
                        .addOptions(new OptionData(OptionType.INTEGER, "volume", "Volume.").setRequired(false))
                        .setGuildOnly(true),
                Commands.slash("mhelp", messageService.getMessage("command.mhelp.description"))
                        .addOptions(new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString).setRequired(false))
                        .setGuildOnly(true),
                Commands.slash("configure", messageService.getMessage("command.configure.description"))
                        .setGuildOnly(true),
                Commands.slash("lucrilhos", messageService.getMessage("command.lucrilhos.description"))
                        .setGuildOnly(true),
                Commands.slash("bup", messageService.getMessage("command.bup.description"))
                        .addOption(OptionType.USER, "user", messageService.getMessage("command.bup.param.user"), false)
                        .setGuildOnly(true)
                ).queue();
    }
}