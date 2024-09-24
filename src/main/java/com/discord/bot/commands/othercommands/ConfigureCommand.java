package com.discord.bot.commands.othercommands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.entity.GuildConfig;
import com.discord.bot.repository.GuildConfigRepository;
import com.discord.bot.service.MessageService;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.DefaultValue;

@AllArgsConstructor
public class ConfigureCommand implements ISlashCommand {

    MessageService messageService;
    GuildConfigRepository guildConfigRepository;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle(messageService.getMessage("bot.user.permission.denied"))
                    .setColor(Color.RED)
                    .setDescription(messageService.getMessage("bot.user.permission.manageserver"));
            event.getHook().sendMessageEmbeds(embedBuilder.build()).setEphemeral(true).queue();
            return;
        }

        GuildConfig guildConfig = guildConfigRepository.findByGuildId(event.getGuild().getIdLong());
        if (guildConfig == null) {
            guildConfig = new GuildConfig(event.getGuild().getIdLong());
        }
        List<DefaultValue> defaultValues = new ArrayList<>();
        if (guildConfig.getGameTextChannelId() != null) {
            defaultValues.add(DefaultValue.channel(guildConfig.getGameTextChannelId()));
        }

        EntitySelectMenu channelSelectMenu = EntitySelectMenu.create("configure-game-text-channel", EntitySelectMenu.SelectTarget.CHANNEL)
                .setPlaceholder(messageService.getMessage("ui.gametextchannel.select.placeholder"))
                .setRequiredRange(1, 1)
                .setChannelTypes(ChannelType.TEXT)
                .setDefaultValues(defaultValues)
                .build();

        event.reply(messageService.getMessage("ui.gametextchannel.select.title"))
                .addActionRow(channelSelectMenu)
                .setEphemeral(true)
                .queue();
    }
}
