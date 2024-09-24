package com.discord.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.discord.bot.entity.GuildConfig;

@Repository
public interface GuildConfigRepository extends JpaRepository<GuildConfig, Long> {
    GuildConfig findByGuildId(Long guildId);
}