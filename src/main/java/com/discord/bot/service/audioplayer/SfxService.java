package com.discord.bot.service.audioplayer;

import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
public class SfxService {

    private Locale locale;

    public SfxService() {
        Locale defaultLocale = Locale.ENGLISH;
        this.locale = defaultLocale;
    }

    public void setLocale(Locale newLocale) {
        this.locale = newLocale;
    }

    public String getRandomSound(SfxType sfxType) {
        String languageFolder = locale.getLanguage();
        String sound = sfxType.getSfxList().get((int) (Math.random() * sfxType.getSfxList().size()));
        String soundPath = getClass().getClassLoader().getResource("sounds/" + languageFolder + "/" + sound).getFile();
        return soundPath;
    }
}
