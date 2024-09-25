package com.discord.bot.service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.discord.bot.dto.MultipleMusicDto;
import com.discord.bot.dto.MusicDto;
import com.discord.bot.entity.Music;
import com.discord.bot.repository.MusicRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RestService {
    private final MusicRepository musicRepository;
    private final InvidiousService invidiousService;
    private final YouTubeApiService youTubeApiService;

    public MultipleMusicDto getYoutubeUrl(MusicDto musicDto, Long guildId) {
        int count = 0;
        int failCount = 0;

        Music music = musicRepository.findFirstByTitle(musicDto.getTitle());

        if (music != null) {
            musicDto.setReference(music.getReference());
            count++;
        } else {
            String videoId = invidiousService.searchVideoId(musicDto.getTitle());
            if (videoId == null) {
                videoId = youTubeApiService.searchVideoId(musicDto.getTitle(), guildId);
            }

            if (videoId != null) {
                String youtubeUrl = "https://www.youtube.com/watch?v=" + videoId;
                musicDto.setReference(youtubeUrl);

                Music newMusic = new Music();
                newMusic.setTitle(musicDto.getTitle());
                newMusic.setReference(youtubeUrl);
                musicRepository.save(newMusic);

                count++;
            } else {
                failCount++;
            }
        }

        return new MultipleMusicDto(count, Collections.singletonList(musicDto), failCount);
    }

    public MultipleMusicDto getYoutubeUrl(List<MusicDto> musicDtos, Long guildId) {
        AtomicInteger count = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        List<MusicDto> updatedMusicDtos = musicDtos.parallelStream().peek(musicDto -> {
            Music music = musicRepository.findFirstByTitle(musicDto.getTitle());

            if (music != null) {
                musicDto.setReference(music.getReference());
                count.incrementAndGet();
            } else {
                String videoId = invidiousService.searchVideoId(musicDto.getTitle());
                if (videoId == null) {
                    videoId = youTubeApiService.searchVideoId(musicDto.getTitle(), guildId);
                }

                if (videoId != null) {
                    String youtubeUrl = "https://www.youtube.com/watch?v=" + videoId;
                    musicDto.setReference(youtubeUrl);

                    Music newMusic = new Music();
                    newMusic.setTitle(musicDto.getTitle());
                    newMusic.setReference(youtubeUrl);
                    musicRepository.save(newMusic);

                    count.incrementAndGet();
                } else {
                    failCount.incrementAndGet();
                }
            }
        }).collect(Collectors.toList());

        return new MultipleMusicDto(count.get(), updatedMusicDtos, failCount.get());
    }
}