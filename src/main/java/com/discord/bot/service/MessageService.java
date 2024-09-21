package com.discord.bot.service;

import java.util.Locale;
import java.util.ResourceBundle;

import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private ResourceBundle bundle;

    public MessageService() {
        Locale defaultLocale = Locale.ENGLISH;
        bundle = ResourceBundle.getBundle("messages", defaultLocale);
    }

    public String getMessage(String key, Object... params) {
        String message = bundle.getString(key);
        if (params != null && params.length > 0) {
            return java.text.MessageFormat.format(message, params);
        }
        return message;
    }

    public void changeLanguage(Locale newLocale) {
        bundle = ResourceBundle.getBundle("messages", newLocale);
    }
}