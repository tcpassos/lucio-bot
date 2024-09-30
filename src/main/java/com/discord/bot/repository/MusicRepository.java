package com.discord.bot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.discord.bot.entity.Music;

public interface MusicRepository extends JpaRepository<Music, Long> {

    Optional<Music> findBySpotifyId(String spotifyId);

    Optional<Music> findByYoutubeId(String youtubeId);
}

