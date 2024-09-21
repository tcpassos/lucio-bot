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
                        .addOption(OptionType.STRING, "query", "Song url or name.", true)
                        .setGuildOnly(true),
                Commands.slash("skip", messageService.getMessage("command.skip.description"))
                        .setGuildOnly(true),
                Commands.slash("forward", messageService.getMessage("command.forward.description"))
                        .addOptions(new OptionData(OptionType.INTEGER, "sec", "seconds")
                                        .setMinValue(1)
                                        .setRequired(true),
                                new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString)
                                        .setRequired(false))
                        .setGuildOnly(true),
                Commands.slash("rewind", messageService.getMessage("command.rewind.description"))
                        .addOptions(new OptionData(OptionType.INTEGER, "sec", "seconds")
                                        .setMinValue(1)
                                        .setRequired(true),
                                new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString)
                                        .setRequired(false))
                        .setGuildOnly(true),
                Commands.slash("pause", messageService.getMessage("command.pause.description"))
                        .addOptions(new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString)
                                .setRequired(false))
                        .setGuildOnly(true),
                Commands.slash("resume", messageService.getMessage("command.resume.description"))
                        .addOptions(new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString)
                                .setRequired(false))
                        .setGuildOnly(true),
                Commands.slash("leave", messageService.getMessage("command.leave.description"))
                        .addOptions(new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString)
                                .setRequired(false))
                        .setGuildOnly(true),
                Commands.slash("queue", messageService.getMessage("command.queue.description"))
                        .addOptions(new OptionData(OptionType.INTEGER, "page", "Displayed page of the queue.")
                                        .setMinValue(1)
                                        .setRequired(false),
                                new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString)
                                        .setRequired(false))
                        .setGuildOnly(true),
                Commands.slash("swap", messageService.getMessage("command.swap.description"))
                        .addOptions(new OptionData(OptionType.INTEGER, "songnum1",
                                        "Song number in the queue to be changed.")
                                        .setMinValue(1)
                                        .setRequired(true),
                                new OptionData(OptionType.INTEGER, "songnum2",
                                        "Song number in the queue to be changed.")
                                        .setMinValue(1)
                                        .setRequired(true),
                                new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString)
                                        .setRequired(false))
                        .setGuildOnly(true),
                Commands.slash("shuffle", messageService.getMessage("command.shuffle.description"))
                        .addOptions(new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString)
                                .setRequired(false))
                        .setGuildOnly(true),
                Commands.slash("loop", messageService.getMessage("command.loop.description"))
                        .addOptions(new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString)
                                .setRequired(false))
                        .setGuildOnly(true),
                Commands.slash("remove", messageService.getMessage("command.remove.description"))
                        .addSubcommands(new SubcommandData("single", "Remove a song from the queue.")
                                        .addOptions(new OptionData(OptionType.INTEGER, "songnum", "Song number to be removed from queue")
                                                        .setMinValue(1)
                                                        .setRequired(false),
                                                new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString)
                                                        .setRequired(false)),
                                new SubcommandData("between", "Removes songs at the specified indexes as " +
                                        "well as the songs located between those indexes")
                                        .addOptions(new OptionData(OptionType.INTEGER, "songnum1",
                                                        "The song number in the queue to be at the head of the removed list.")
                                                        .setMinValue(1)
                                                        .setRequired(true),
                                                new OptionData(OptionType.INTEGER, "songnum2",
                                                        "The song number in the queue to be at the tail of the removed list.")
                                                        .setMinValue(1)
                                                        .setRequired(true),
                                                new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString)
                                                        .setRequired(false)),
                                new SubcommandData("all", "Clear the queue."))
                        .setGuildOnly(true),
                Commands.slash("nowplaying", messageService.getMessage("command.nowplaying.description"))
                        .addOptions(new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString)
                                .setRequired(false))
                        .setGuildOnly(true),
                Commands.slash("volume", messageService.getMessage("command.volume.description"))
                        .addOptions(new OptionData(OptionType.INTEGER, "volume", "Volume.").setRequired(false))
                        .setGuildOnly(true),
                Commands.slash("mhelp", messageService.getMessage("command.mhelp.description")).addOptions(new OptionData(OptionType.BOOLEAN, "ephemeral", ephemeralString).setRequired(false)).setGuildOnly(true),
                Commands.slash("lucrilhos", messageService.getMessage("command.lucrilhos.description")).setGuildOnly(true),
                Commands.slash("bup", messageService.getMessage("command.bup.description"))
                        .addOption(OptionType.USER, "user", "User to bup.", false)
                        .setGuildOnly(true)
                ).queue();
    }
}