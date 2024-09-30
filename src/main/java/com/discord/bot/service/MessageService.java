package com.discord.bot.service;

import java.awt.Color;
import java.util.Locale;
import java.util.ResourceBundle;

import org.springframework.stereotype.Service;

import net.dv8tion.jda.api.EmbedBuilder;

@Service
public class MessageService {

    private ResourceBundle bundle;

    public MessageService() {
        Locale defaultLocale = Locale.ENGLISH;
        bundle = ResourceBundle.getBundle("messages", defaultLocale);
    }

    public void changeLanguage(Locale newLocale) {
        bundle = ResourceBundle.getBundle("messages", newLocale);
    }

    public String getMessage(String key, Object... params) {
        String message = bundle.getString(key);
        if (params != null && params.length > 0) {
            return java.text.MessageFormat.format(message, params);
        }
        return message;
    }

    public EmbedBuilder getEmbed(String key, Object... params) {
        return new EmbedBuilder().setDescription(getMessage(key, params));
    }

    public EmbedBuilder getEmbedSuccess(String key, Object... params) {
        return new EmbedBuilder().setDescription(getMessage(key, params)).setColor(Color.GREEN);
    }

    public EmbedBuilder getEmbedWarning(String key, Object... params) {
        return new EmbedBuilder().setDescription(getMessage(key, params)).setColor(Color.YELLOW);
    }

    public EmbedBuilder getEmbedError(String key, Object... params) {
        return new EmbedBuilder().setDescription(getMessage(key, params)).setColor(Color.RED);
    }
}