package com.discord.bot.service.audioplayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SfxService {

    private static final Logger logger = LoggerFactory.getLogger(SfxService.class);

    private Locale locale;
    private final Map<String, String> soundCache = new HashMap<>();

    public SfxService() {
        Locale defaultLocale = Locale.ENGLISH;
        this.locale = defaultLocale;
    }

    public void setLocale(Locale newLocale) {
        this.locale = newLocale;
    }

    public String getSound(SfxType sfxType) {
        String sound = sfxType.getSfxList().get((int) (Math.random() * sfxType.getSfxList().size()));
        return getSound(sound);
    }

    public String getSound(String soundFile) {
        if (soundCache.containsKey(soundFile)) {
            return soundCache.get(soundFile);
        }
        
        String languageFolder = locale.getLanguage();
        String soundPath = "sounds/" + languageFolder + "/" + soundFile;
        InputStream soundStream = getClass().getClassLoader().getResourceAsStream(soundPath);
        if (soundStream == null) {
            throw new RuntimeException("Sound file not found: " + soundPath);
        }

        try {
            File tempFile = File.createTempFile("sound", ".ogg");
            tempFile.deleteOnExit();

            try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;

                // Copy the sound file to a temporary file
                while ((bytesRead = soundStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            String tempFilePath = tempFile.getAbsolutePath();
            soundCache.put(soundFile, tempFilePath);

            return tempFilePath;
        } catch (IOException e) {
            logger.error("Failed to create temporary file for sound: {}", soundFile, e);
            throw new RuntimeException("Failed to load sound file", e);
        }
    }
}
