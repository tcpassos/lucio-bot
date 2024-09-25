package com.discord.bot.listeners;

import java.awt.Color;

import com.discord.bot.entity.GuildConfig;
import com.discord.bot.repository.GuildConfigRepository;
import com.discord.bot.service.MessageService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ModalInteractionListener extends ListenerAdapter {

    private final MessageService messageService;
    private final GuildConfigRepository guildConfigRepository;

    public ModalInteractionListener(MessageService messageService, GuildConfigRepository guildConfigRepository) {
        this.messageService = messageService;
        this.guildConfigRepository = guildConfigRepository;
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId().equals("configure-modal")) {
            if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
                EmbedBuilder embedBuilder = new EmbedBuilder().setDescription(messageService.getMessage("bot.user.permission.manageserver"))
                                                              .setColor(Color.RED);
                event.getHook().sendMessageEmbeds(embedBuilder.build()).setEphemeral(true).queue();
                return;
            }

            String youtubeApiKey = event.getValue("youtube_api_key") != null ? event.getValue("youtube_api_key").getAsString() : null;
            String spotifyClientId = event.getValue("spotify_client_id") != null ? event.getValue("spotify_client_id").getAsString() : null;
            String spotifyClientSecret = event.getValue("spotify_client_secret") != null ? event.getValue("spotify_client_secret").getAsString() : null;

            if (youtubeApiKey == null && spotifyClientId == null && spotifyClientSecret == null) {
                event.deferReply().queue();
                return;
            }

            // Get guild by ID
            Long guildId = event.getGuild().getIdLong();
            GuildConfig guildConfig = guildConfigRepository.findByGuildId(guildId);
            if (guildConfig == null) {
                guildConfig = new GuildConfig(guildId);
            }

            // Update the API keys
            if (youtubeApiKey != null && !youtubeApiKey.isEmpty()) guildConfig.setYoutubeApiKey(youtubeApiKey);
            if (spotifyClientId != null && !spotifyClientId.isEmpty()) guildConfig.setSpotifyClientId(spotifyClientId);
            if (spotifyClientSecret != null && !spotifyClientSecret.isEmpty()) guildConfig.setSpotifyClientSecret(spotifyClientSecret);
            guildConfigRepository.save(guildConfig);

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle(messageService.getMessage("api.config.updated.title"))
                    .setColor(Color.GREEN)
                    .setDescription(messageService.getMessage("api.config.updated.description"));

            event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
        }
    }
}
