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
public class YoutubeService {
    private final MusicRepository musicRepository;
    private final InvidiousService invidiousService;
    private final YouTubeApiService youTubeApiService;

    public String searchVideoId(String query) {
        String videoId = invidiousService.searchVideoId(query);
        if (videoId == null) {
            videoId = youTubeApiService.searchVideoId(query);
        }
        return videoId;
    }

    public MultipleMusicDto getYoutubeUrl(MusicDto musicDto) {
        AtomicInteger count = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        MusicDto updatedMusicDto = processMusicDto(musicDto, count, failCount);

        return new MultipleMusicDto(count.get(), Collections.singletonList(updatedMusicDto), failCount.get());
    }

    public MultipleMusicDto getYoutubeUrl(List<MusicDto> musicDtos) {
        AtomicInteger count = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        List<MusicDto> updatedMusicDtos = musicDtos.parallelStream()
                .map(musicDto -> processMusicDto(musicDto, count, failCount))
                .collect(Collectors.toList());

        return new MultipleMusicDto(count.get(), updatedMusicDtos, failCount.get());
    }

    private MusicDto processMusicDto(MusicDto musicDto, AtomicInteger count, AtomicInteger failCount) {
        Music music = musicRepository.findFirstByTitle(musicDto.getTitle());

        if (music != null) {
            musicDto.setReference(music.getReference());
            count.incrementAndGet();
        } else {
            String videoId = searchVideoId(musicDto.getTitle());

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

        return musicDto;
    }
}