package com.discord.bot.listeners;

import com.discord.bot.entity.GuildConfig;
import com.discord.bot.repository.GuildConfigRepository;
import com.discord.bot.service.MessageService;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import org.jetbrains.annotations.NotNull;

import java.awt.*;

@AllArgsConstructor
public class EntitySelectInteractionListener extends ListenerAdapter {

    private final MessageService messageService;
    private final GuildConfigRepository guildConfigRepository;

    @Override
    public void onEntitySelectInteraction(@NotNull EntitySelectInteractionEvent event) {
        if (event.getComponentId().equals("configure-game-text-channel")) {
            if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTitle(messageService.getMessage("bot.user.permission.denied"))
                        .setColor(Color.RED)
                        .setDescription(messageService.getMessage("bot.user.permission.manageserver"));
                event.getHook().sendMessageEmbeds(embedBuilder.build()).setEphemeral(true).queue();
                return;
            }

            // Save the selected game text channel
            TextChannel selectedChannel = (TextChannel) event.getValues().get(0);
            Long guildId = event.getGuild().getIdLong();
            GuildConfig guildConfig = guildConfigRepository.findByGuildId(guildId);
            if (guildConfig == null) {
                guildConfig = new GuildConfig(guildId);
            }
            guildConfig.setGameTextChannelId(selectedChannel.getIdLong());
            guildConfigRepository.save(guildConfig);

            // Prompt the user to configure the API keys
            TextInput youtubeApiKeyInput = TextInput.create("youtube_api_key", "YouTube API Key", TextInputStyle.SHORT)
                                                    .setPlaceholder(messageService.getMessage("ui.youtubeapi.key.placeholder"))
                                                    .setRequired(false)
                                                    .setValue(guildConfig.getYoutubeApiKey())
                                                    .build();
            Modal modal = Modal.create("configure-modal", "APIs")
                               .addActionRow(youtubeApiKeyInput)
                               .build();

            event.replyModal(modal).queue();
        }
    }
}
