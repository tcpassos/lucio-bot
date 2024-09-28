package com.discord.bot.context;

public class GuildContextHolder {
    private static final ThreadLocal<GuildContext> contextHolder = new ThreadLocal<>();

    public static void setGuildContext(GuildContext context) {
        contextHolder.set(context);
    }

    public static GuildContext getGuildContext() {
        return contextHolder.get();
    }

    public static void clearGuildContext() {
        contextHolder.remove();
    }
}